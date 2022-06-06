package ru.isg.invest.helper.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.utils.DateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.tinkoff.piapi.contract.v1.OperationState.OPERATION_STATE_CANCELED;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BROKER_FEE;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BUY;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;

/**
 * Created by s.ivanov on 04.06.2022.
 */
//@SpringBootTest
@Slf4j
public class OperationsServiceTest {

    private static final String FIGI_FEATURE_USD = "BBG00VHGV1J0";
    private static final String FIGI_STOCK_CHMF = "BBG00475K6C3";

    private InvestApi api;

    @Value("${tinkoff.api.token}")
    private String token;

//    @Test
    public void test() {

        api = InvestApi.createSandbox(token);

        OperationsService operationsService = api.getOperationsService();
//        UsersService usersService = api.getUserService();

//        List<Account> accounts = usersService.getAccountsSync();
//        for (Account a : accounts) {
//            log.info("Account id = {}, name = {}", a.getId(), a.getName());
//        }

//        Positions positions = operationsService.getPositionsSync("2146755117");
//        positions.getMoney().forEach(m -> {
//            log.info("currency {}: {}", m.getCurrency().getCurrencyCode(), m.getValue());
//        });
//        positions.getSecurities().forEach(s -> {
//            log.info("security {}: {}, blocked: {}", s.getFigi(), s.getBalance(), s.getBlocked());
//        });
//        positions.getFutures().forEach(f -> {
//            log.info("future {}: {}, blocked: {}", f.getFigi(), f.getBalance(), f.getBlocked());
//        });

//        Portfolio pf = operationsService.getPortfolioSync("2146755117");
//        log.info("{}, {}, {}, {}, {}, {}", pf.getExpectedYield(), moneyToString(pf.getTotalAmountBonds()),
//                moneyToString(pf.getTotalAmountEtfs()), moneyToString(pf.getTotalAmountCurrencies()),
//                moneyToString(pf.getTotalAmountFutures()), moneyToString(pf.getTotalAmountShares()));
//        pf.getPositions().forEach(p -> {
//            log.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}", p.getFigi(), p.getExpectedYield(), p.getAveragePositionPricePt(),
//                    moneyToString(p.getAveragePositionPrice()), moneyToString(p.getAveragePositionPriceFifo()),
//                    moneyToString(p.getCurrentNkd()), moneyToString(p.getCurrentPrice()), p.getInstrumentType(),
//                    p.getQuantity(), p.getQuantityLots());
//        });

        List<Operation> ops = operationsService
                .getAllOperationsSync("2146755117", Instant.now().minusSeconds(1 * 365 * 24 * 60 * 60),
                        Instant.now(), FIGI_STOCK_CHMF);

//        ops.forEach(op -> {
//            log.info("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
//                    op.getFigi(), op.getCurrency(), op.getId(), op.getDate(), op.getInstrumentType(),
//                    op.getOperationType(),
//                    op.getParentOperationId(), op.getPayment().getCurrency(), op.getPayment().getNano(),
//                    op.getPayment().getUnits(),
//                    op.getPrice().getCurrency(), op.getPrice().getNano(), op.getPrice().getUnits(),
//                    op.getQuantity(), op.getQuantityRest(), op.getState(), op.getTradesCount(), op.getType());
//        });

        BigDecimal instrumentResult = BigDecimal.ZERO;
        BigDecimal feeResult = BigDecimal.ZERO;

        int i = 1;

        List<Operation> opsToUse = ops.stream()
                .filter(op -> op.getState() != OPERATION_STATE_CANCELED)
                .sorted(Comparator.comparing(op -> DateUtils.timestampToInstant(op.getDate())))
                .collect(Collectors.toList());
        for (Operation op : opsToUse) {

            BigDecimal payment = mergePriceValue(op.getPayment().getUnits(), op.getPayment().getNano());

            if (op.getOperationType() == OPERATION_TYPE_BROKER_FEE) {
                feeResult = feeResult.add(payment);
            } else if (op.getOperationType() == OPERATION_TYPE_BUY || op.getOperationType() == OPERATION_TYPE_SELL) {
                instrumentResult = instrumentResult.add(payment);
            } else {
                throw new IllegalStateException("Operation not supported: " + op.getOperationType());
            }

            if (!op.getPayment().getCurrency().equals("rub")) {
                throw new IllegalStateException("Currency not expected: " + op.getCurrency());
            }

            log.info("{}. {} \t {} \t {} \t {} \t {} {}", i, DateUtils.timestampToString(op.getDate()), op.getFigi(),
                    op.getOperationType(), op.getQuantity(), payment.setScale(8, RoundingMode.HALF_UP),
                    op.getPayment().getCurrency());

            i++;
        }

        log.info("instrumentResult: {}", instrumentResult);
        log.info("feeResult: {}", feeResult);

        // расчет итога по инструменту
        // расчет безубытка по инструменту

        // итог по фьючу: расход 201535 р.
        // текущая стоимость, потенциальный доход: 61146 * 3 = 183438 р.
        // уровень безубытка по инструменту: 67178 р.
        // разница: 18097 р.
        // плюс расход на комиссию: 2178 р.
        // для покрытия убытка требуется заработать: 20275 р.


        // итог по chmf: расход 5559 р.
        // текущая стоимость, потенциальный доход: 711 * 7 = 4977 р.
        // уровень безубытка по инструменту: 794,14 р.
        // разница: 582 р.
        // плюс расход на комиссию: 2,23 р.
        // для покрытия убытка требуется заработать: 584,23 р.

    }

    private BigDecimal mergePriceValue(long units, int nanos) {
        if (units != 0) return new BigDecimal(units + "." + Math.abs(nanos));
        else {
            if (nanos > 0) return new BigDecimal("0." + nanos);
            else if (nanos < 0) return new BigDecimal("-0." + Math.abs(nanos));
            else return new BigDecimal("0.0");
        }
    }

    private String moneyToString(Money m) {
        return m.getValue() + " " + m.getCurrency().getCurrencyCode();
    }
}
