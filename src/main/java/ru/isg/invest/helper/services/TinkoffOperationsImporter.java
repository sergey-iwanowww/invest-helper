package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.model.Currencies;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.model.Operation;
import ru.isg.invest.helper.repositories.InstrumentRepository;
import ru.isg.invest.helper.repositories.OperationRepository;
import ru.tinkoff.piapi.core.utils.DateUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static java.time.ZoneOffset.UTC;
import static ru.tinkoff.piapi.contract.v1.OperationState.OPERATION_STATE_CANCELED;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BROKER_FEE;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BUY;
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

    @Value("${tinkoff.account.speculative.id}")
    private String accountSpeculativeId;

    @Transactional
    public void importOperations() {

        LocalDateTime dateFrom;

        Page<Operation> opsPage = operationRepository.findAll(PageRequest.of(0, 1, Sort.Direction.DESC, "date"));
        if (!opsPage.isEmpty()) {
            dateFrom = opsPage.getContent().get(0).getDate().plus(1, ChronoUnit.MILLIS);
        } else {
            dateFrom = LocalDateTime.of(2021, JANUARY, 1, 0, 0);
        }

        Instant instantFrom = dateFrom.toInstant(UTC);
        Instant instantTo = LocalDateTime.now().toInstant(UTC);

        log.info("Operations importer started for period: {} - {}", instantFrom, instantTo);

        List<ru.tinkoff.piapi.contract.v1.Operation> tinkoffOps = tinkoffApiClient.getOperationsService()
                .getAllOperationsSync(accountSpeculativeId, instantFrom, instantTo);

        log.info("Getted {} operations from Tinkoff API", tinkoffOps.size());

        tinkoffOps.stream()
                .filter(top -> top.getState() != OPERATION_STATE_CANCELED)
                .forEach(top -> {

                    Optional<Instrument> instrumentOpt;
                    if (top.getOperationType() == OPERATION_TYPE_BROKER_FEE
                            || top.getOperationType() == OPERATION_TYPE_SELL
                            || top.getOperationType() == OPERATION_TYPE_BUY) {
                        instrumentOpt = instrumentRepository.findByFigi(top.getFigi());
                    } else {
                        instrumentOpt = Optional.empty();
                    }

                    Currencies currency = Currencies.valueOf(top.getCurrency().toUpperCase());
                    BigDecimal payment = mergePriceValue(top.getPayment().getUnits(), top.getPayment().getNano());
                    LocalDateTime date = LocalDateTime.ofInstant(DateUtils.timestampToInstant(top.getDate()), UTC);
                    long count = top.getOperationType() == OPERATION_TYPE_SELL ? -1 * top.getQuantity() : top.getQuantity();

                    saveOperation(instrumentOpt.orElse(null), count, payment, currency,
                            top.getOperationType().name(), date, top.getId());
                });
    }

    private Operation saveOperation(Instrument instrument, long count, BigDecimal payment, Currencies currency,
            String type, LocalDateTime date, String externalId) {

        return operationRepository.findByExternalId(externalId)
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
                    return dbOp;
                })
                .orElseGet(() -> operationRepository
                        .save(new Operation(instrument, type, date, count, payment, currency, externalId)));
    }

    private BigDecimal mergePriceValue(long units, int nanos) {
        if (units != 0) return new BigDecimal(units + "." + Math.abs(nanos));
        else {
            if (nanos > 0) return new BigDecimal("0." + nanos);
            else if (nanos < 0) return new BigDecimal("-0." + Math.abs(nanos));
            else return new BigDecimal("0.0");
        }
    }
}