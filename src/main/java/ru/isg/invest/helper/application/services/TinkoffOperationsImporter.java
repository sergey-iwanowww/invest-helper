package ru.isg.invest.helper.application.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.isg.invest.helper.domain.model.Currencies;
import ru.isg.invest.helper.domain.model.Instrument;
import ru.isg.invest.helper.domain.model.Operation;
import ru.isg.invest.helper.domain.model.Portfolio;
import ru.isg.invest.helper.domain.model.Position;
import ru.isg.invest.helper.infrastructure.repositories.InstrumentRepository;
import ru.isg.invest.helper.infrastructure.repositories.OperationRepository;
import ru.isg.invest.helper.infrastructure.repositories.PortfolioRepository;
import ru.isg.invest.helper.infrastructure.repositories.PositionRepository;
import ru.isg.invest.helper.infrastructure.tinkoff.TinkoffApiClient;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.core.utils.DateUtils;
import ru.tinkoff.piapi.core.utils.MapperUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.time.Month.JANUARY;
import static java.time.ZoneOffset.UTC;
import static ru.tinkoff.piapi.contract.v1.OperationState.OPERATION_STATE_CANCELED;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BOND_TAX;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BROKER_FEE;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BUY;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_COUPON;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_DIVIDEND;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_DIVIDEND_TAX;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;

/**
 * Created by s.ivanov on 26.05.2022.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TinkoffOperationsImporter {

    private final OperationRepository operationRepository;
    private final TinkoffApiClient tinkoffApiClient;
    private final InstrumentRepository instrumentRepository;
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;

    @Transactional
    public void importOperations() {
        portfolioRepository.findAll().forEach(this::importPortfolioOperations);
    }

    private void importPortfolioOperations(Portfolio portfolio) {

        LocalDateTime dateFrom;

        Page<Operation> opsPage = operationRepository.findByPortfolioId(portfolio.getId(),
                PageRequest.of(0, 1, Sort.Direction.DESC, "date"));
        if (!opsPage.isEmpty()) {
            dateFrom = opsPage.getContent().get(0).getDate().plus(1, ChronoUnit.MILLIS);
        } else {
            dateFrom = LocalDateTime.of(2021, JANUARY, 1, 0, 0);
        }

        Instant instantFrom = dateFrom.toInstant(UTC);
        Instant instantTo = LocalDateTime.now(UTC).toInstant(UTC);

        log.info("Operations importer started for portfolio {} and period from {} to {}", portfolio.getId(),
                instantFrom, instantTo);

        List<ru.tinkoff.piapi.contract.v1.Operation> tinkoffOps = tinkoffApiClient.getOperationsService()
                .getAllOperationsSync(portfolio.getExternalId(), instantFrom, instantTo);

        log.info("Getted {} operations from Tinkoff API", tinkoffOps.size());

        Set<UUID> updatedInstrumentIds = new HashSet<>();

        Map<OperationType, Boolean> m = new HashMap<>();

        tinkoffOps.stream()
                .filter(top -> top.getState() != OPERATION_STATE_CANCELED)
                .forEach(top -> {

                    Currencies currency = Currencies.valueOf(top.getCurrency().toUpperCase());
                    BigDecimal payment = MapperUtils.moneyValueToBigDecimal(top.getPayment());
                    LocalDateTime date = LocalDateTime.ofInstant(DateUtils.timestampToInstant(top.getDate()), UTC);
                    long count = top.getOperationType() == OPERATION_TYPE_SELL ? -1 * top.getQuantity() : top
                            .getQuantity();

                    if (top.getOperationType() == OPERATION_TYPE_BROKER_FEE
                            || top.getOperationType() == OPERATION_TYPE_SELL
                            || top.getOperationType() == OPERATION_TYPE_BUY
                            || top.getOperationType() == OPERATION_TYPE_DIVIDEND
                            || top.getOperationType() == OPERATION_TYPE_DIVIDEND_TAX
                            || top.getOperationType() == OPERATION_TYPE_COUPON
                            || top.getOperationType() == OPERATION_TYPE_BOND_TAX) {

                        Instrument instrument = instrumentRepository.findByFigi(top.getFigi()).get();

                        OperationSavingResult result = saveOperation(portfolio, instrument, count, payment, currency,
                                top.getOperationType().name(), date, top.getId());
                        if (result.isSaved()) {
                            updatedInstrumentIds.add(instrument.getId());
                        }
                    } else {
                        if (StringUtils.hasText(top.getFigi())) {
                            throw new IllegalStateException(
                                    "Operation with id = " + top.getId() + " has figi, but figi not read");
                        }
                        saveOperation(portfolio, null, count, payment, currency, top.getOperationType().name(),
                                date, top.getId());
                    }
                });

        if (updatedInstrumentIds.size() > 0) {
            recalcPositions(portfolio, updatedInstrumentIds);
        }
    }

    private OperationSavingResult saveOperation(Portfolio portfolio, Instrument instrument, long count,
            BigDecimal payment, Currencies currency, String type, LocalDateTime date, String externalId) {

        return operationRepository.findByPortfolioIdAndExternalId(portfolio.getId(), externalId)
                .map(dbOp -> {
                    if (!dbOp.getCurrency().equals(currency)
                            || !dbOp.getExternalId().equals(externalId)
                            || !dbOp.getInstrument().getId().equals(instrument.getId())
                            || dbOp.getPayment().compareTo(payment) != 0
                            || !dbOp.getType().equals(type)
                            || !dbOp.getDate().equals(date)
                            || dbOp.getCount() != count) {

                        throw new IllegalStateException(
                                "Операция в БД не соответствует операции, полученной от Тинькофф API");
                    }
                    return new OperationSavingResult(dbOp, false);
                })
                .orElseGet(() -> {
                    Operation dbOp = operationRepository
                            .save(new Operation(portfolio, instrument, type, date, count, payment, currency,
                                    externalId));
                    return new OperationSavingResult(dbOp, true);
                });
    }

    @Value
    static class OperationSavingResult {
        Operation operation;
        boolean saved;
    }

    @Data
    @AllArgsConstructor
    static class BalanceItem {
        long count;
        BigDecimal price;
    }

    public void recalcPositions(Portfolio portfolio, Set<UUID> updatedInstrumentIds) {

        updatedInstrumentIds.forEach(i -> {

            Instrument instrument = instrumentRepository.findById(i).get();

            List<Operation> operations = operationRepository.findByPortfolioIdAndInstrumentIdOrderByDate(
                    portfolio.getId(), i);

            BigDecimal result = ZERO;
            BigDecimal commission = ZERO;
            BigDecimal dividends = ZERO;
            BigDecimal dividendsTax = ZERO;
            BigDecimal coupons = ZERO;
            BigDecimal couponsTax = ZERO;

            Queue<BalanceItem> items = new LinkedList<>();
            for (Operation operation : operations) {

                if (operation.getType().equals("OPERATION_TYPE_SELL")
                        || operation.getType().equals("OPERATION_TYPE_BUY")) {

                    if (operation.getCount() == 0) {
                        throw new IllegalStateException("Operation count equals to 0");
                    }

                    result = result.add(operation.getPayment());

                    BigDecimal operationPrice = operation.getPayment()
                            .divide(BigDecimal.valueOf(operation.getCount()), 4, HALF_UP);
                    long operationCount = operation.getCount();

                    BalanceItem item = items.peek();

                    if (item != null) {

                        if (item.getCount() == 0) {
                            throw new IllegalStateException("Item count equals to 0");
                        }

                        if (item.getCount() > 0 && operationCount < 0
                            || item.getCount() < 0 && operationCount > 0) {

                            while (item != null && operationCount != 0) {

                                long potentialNewItemCount = item.getCount() + operationCount;

                                long count;
                                if (potentialNewItemCount == 0
                                        || potentialNewItemCount > 0 && item.getCount() > 0
                                        || potentialNewItemCount < 0 && item.getCount() < 0) {
                                    count = operationCount;
                                } else {
                                    count = -1 * item.getCount();
                                }

                                item.setCount(item.getCount() + count);

                                if (item.getCount() == 0) {
                                    items.remove();
                                }

                                item = items.peek();

                                operationCount = operationCount - count;
                            }

                            if (item == null && operationCount != 0) {
                                items.add(new BalanceItem(operationCount, operationPrice));
                            }
                        } else{
                            items.add(new BalanceItem(operationCount, operationPrice));
                        }
                    } else {
                        items.add(new BalanceItem(operationCount, operationPrice));
                    }
                } else if (operation.getType().equals("OPERATION_TYPE_BROKER_FEE")) {
                    commission = commission.add(operation.getPayment());
                } else if (operation.getType().equals("OPERATION_TYPE_DIVIDEND")) {
                    dividends = dividends.add(operation.getPayment());
                } else if (operation.getType().equals("OPERATION_TYPE_COUPON")) {
                    coupons = coupons.add(operation.getPayment());
                } else if (operation.getType().equals("OPERATION_TYPE_DIVIDEND_TAX")) {
                    dividendsTax = dividendsTax.add(operation.getPayment());
                } else if (operation.getType().equals("OPERATION_TYPE_BOND_TAX")) {
                    couponsTax = couponsTax.add(operation.getPayment());
                } else {
                    throw new IllegalStateException("Unsupported operation type: " + operation.getType());
                }
            }

            long balanceCount = 0;
            BigDecimal balanceAverage = ZERO;

            if (items.peek() != null) {

                BigDecimal balancePayment = ZERO;

                BalanceItem item;
                while ((item = items.poll()) != null) {
                    balanceCount = balanceCount + item.getCount();
                    balancePayment = balancePayment.add(item.getPrice().multiply(BigDecimal.valueOf(item.getCount())));
                }

                balanceAverage = balancePayment.divide(BigDecimal.valueOf(balanceCount), 4, HALF_UP);
            }

            savePosition(portfolio, instrument, balanceCount, result, commission, balanceAverage, dividends,
                    dividendsTax, coupons, couponsTax);
        });
    }

    private Position savePosition(Portfolio portfolio, Instrument instrument, long balanceCount, BigDecimal result,
            BigDecimal commission, BigDecimal balanceAverage, BigDecimal dividends, BigDecimal dividendsTax,
            BigDecimal coupons, BigDecimal couponsTax) {

        return positionRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())
                .map(dbPos -> {
                    dbPos
                            .setBalanceCount(balanceCount)
                            .setBalanceAverage(balanceAverage)
                            .setResult(result)
                            .setCommission(commission)
                            .setDividends(dividends)
                            .setDividendsTax(dividendsTax)
                            .setCoupons(coupons)
                            .setCouponsTax(couponsTax);
                    return dbPos;
                })
                .orElseGet(() -> positionRepository
                        .save(new Position(instrument, portfolio, balanceCount, balanceAverage, result, commission,
                                dividends, dividendsTax, coupons, couponsTax)));
    }
}