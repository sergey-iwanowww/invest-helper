package ru.isg.invest.helper.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.model.Candle;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.repositories.CandleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.isg.invest.helper.model.TimeFrames.ONE_HOUR;

/**
 * Created by s.ivanov on 30.05.2022.
 */
@SpringBootTest
public class CandlesAnalyzerTest {

    public static final BigDecimal TWO = BigDecimal.valueOf(2);
    @Autowired
    private CandleRepository candleRepository;
    @Autowired
    private CandlesAnalyzer candlesAnalyzer;
    @Autowired
    private TestHelper testHelper;

    @Test
    @Transactional
    public void testCheckRisePriceCrossedTheValue() {

        Instrument instrument = testHelper.getRandomInstrument();

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).truncatedTo(MINUTES);

        // 7     8     9     10    11    12    13    14    15
        // 200 - 202 - 204 - 206 - 208 - 210 - 212 - 214 - 216

        generateCandles(instrument, yesterday, 200, 202, 204, 206, 208, 210, 212, 214, 216);

        // проверяемое значение = 300, свечей, преодолевающих такую цену - нет

        PriceCrossedTheValueCheckingResults results = candlesAnalyzer
                .checkPriceCrossedTheValue(instrument, ONE_HOUR, yesterday.withHour(1).withMinute(0),
                        LocalDateTime.now(), true, BigDecimal.valueOf(300));

        assertNull(results.getCrossCandle());

        // проверяемое значение = 100, должна быть найдена свеча, цена ее закрытия == 202, дата = 7:59

        results = candlesAnalyzer
                .checkPriceCrossedTheValue(instrument, ONE_HOUR, yesterday.withHour(1).withMinute(0),
                        LocalDateTime.now(), true, BigDecimal.valueOf(100));

        assertEquals(7, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());

        // проверяемое значение = 206, должна быть найдена свеча, цена ее закрытия == 206, дата = 9:59

        results = candlesAnalyzer
                .checkPriceCrossedTheValue(instrument, ONE_HOUR, yesterday.withHour(1).withMinute(0),
                        LocalDateTime.now(), true, BigDecimal.valueOf(206));

        assertEquals(9, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
    }

    @Test
    @Transactional
    public void testCheckFallPriceCrossedTheValue() {

        Instrument instrument = testHelper.getRandomInstrument();

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).truncatedTo(MINUTES);

        generateCandles(instrument, yesterday, 200, 198, 196, 194, 192, 190, 188, 186, 184);

        // 7     8     9     10    11    12    13    14    15
        // 200 - 198 - 196 - 194 - 192 - 190 - 188 - 186 - 184

        // проверяемое значение = 100, свечей, преодолевающих такую цену - нет

        PriceCrossedTheValueCheckingResults results = candlesAnalyzer
                .checkPriceCrossedTheValue(instrument, ONE_HOUR, yesterday.withHour(1).withMinute(0),
                        LocalDateTime.now(), false, BigDecimal.valueOf(100));

        assertNull(results.getCrossCandle());

        // проверяемое значение = 300, должна быть найдена свеча, цена ее закрытия == 202, дата = 7:59

        results = candlesAnalyzer
                .checkPriceCrossedTheValue(instrument, ONE_HOUR, yesterday.withHour(1).withMinute(0),
                        LocalDateTime.now(), false, BigDecimal.valueOf(300));

        assertEquals(7, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());

        // проверяемое значение = 194, должна быть найдена свеча, цена ее закрытия == 206, дата = 9:59

        results = candlesAnalyzer
                .checkPriceCrossedTheValue(instrument, ONE_HOUR, yesterday.withHour(1).withMinute(0),
                        LocalDateTime.now(), false, BigDecimal.valueOf(194));

        assertEquals(9, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
    }

    private void generateCandles(Instrument instrument, LocalDateTime beginOfDay, int... vals) {

        // генерируются свечи, начиная с 7:00 по 15:00
        int hour = 7;
        for (int i = 0; i < vals.length - 1; i++) {

            candleRepository.save(new Candle(
                    instrument,
                    ONE_HOUR,
                    beginOfDay.withHour(hour++).withMinute(0),
                    BigDecimal.valueOf(0),
                    BigDecimal.valueOf(0),
                    BigDecimal.valueOf(vals[i]),
                    BigDecimal.valueOf(vals[i + 1]),
                    100000,
                    true));
        }
    }

    @Test
    @Transactional
    public void testCheckRisePriceCrossedTheValueWithRetest() {

        Instrument instrument = testHelper.getRandomInstrument();

        LocalDateTime date = LocalDateTime.now().minusDays(30).truncatedTo(DAYS);

        // проверяемое значение = 300, свечей, преодолевающих такую цену - нет

        generateCandles(instrument, date, 196, 197, 202, 197, 195);

        PriceCrossedTheValueWithRetestCheckingResults results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), true, BigDecimal.valueOf(300), TWO);

        assertNull(results.getCrossCandle());
        assertNull(results.getLastRetestCandle());
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, но откатывается ниже диапазона

        date = date.plusDays(1);

        generateCandles(instrument, date, 196, 197, 202, 197, 195);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), true, BigDecimal.valueOf(200), TWO);

        assertNull(results.getCrossCandle());
        assertNull(results.getLastRetestCandle());
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит еще выше

        date = date.plusDays(1);

        generateCandles(instrument, date, 196, 197, 204, 206, 208);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), true, BigDecimal.valueOf(200), TWO);

        assertEquals(8, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
        assertTrue(results.getCrossCandle().getClose().compareTo(BigDecimal.valueOf(204)) == 0);
        assertNull(results.getLastRetestCandle());
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит ниже цены закрытия crossed свечи,
        // но выше диапазона

        date = date.plusDays(1);

        generateCandles(instrument, date, 196, 197, 204, 203);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), true, BigDecimal.valueOf(200), TWO);

        assertEquals(8, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
        assertTrue(results.getCrossCandle().getClose().compareTo(BigDecimal.valueOf(204)) == 0);
        assertNull(results.getLastRetestCandle());
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит в диапазон ниже цены закрытия crossed свечи

        date = date.plusDays(1);

        generateCandles(instrument, date, 196, 197, 204, 202, 201);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), true, BigDecimal.valueOf(200), TWO);

        assertEquals(8, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
        assertTrue(results.getCrossCandle().getClose().compareTo(BigDecimal.valueOf(204)) == 0);
        assertEquals(10, results.getLastRetestCandle().getCloseDate().getHour());
        assertEquals(59, results.getLastRetestCandle().getCloseDate().getMinute());
        assertTrue(results.getLastRetestCandle().getClose().compareTo(BigDecimal.valueOf(201)) == 0);
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит в диапазон ниже цены закрытия crossed свечи,
        // а затем опускается ниже диапазона

        date = date.plusDays(1);

        generateCandles(instrument, date, 196, 197, 204, 202, 201, 197);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), true, BigDecimal.valueOf(200), TWO);

        assertNull(results.getCrossCandle());
        assertNull(results.getLastRetestCandle());
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит в диапазон ниже цены закрытия crossed свечи,
        // затем выходит выше диапазона, но ниже закрытия crossed свечи

        date = date.plusDays(1);

        generateCandles(instrument, date, 196, 197, 204, 202, 201, 203);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), true, BigDecimal.valueOf(200), TWO);

        assertEquals(8, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
        assertTrue(results.getCrossCandle().getClose().compareTo(BigDecimal.valueOf(204)) == 0);
        assertEquals(10, results.getLastRetestCandle().getCloseDate().getHour());
        assertEquals(59, results.getLastRetestCandle().getCloseDate().getMinute());
        assertTrue(results.getLastRetestCandle().getClose().compareTo(BigDecimal.valueOf(201)) == 0);
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит в диапазон ниже цены закрытия crossed свечи,
        // затем выходит выше диапазона и выше закрытия crossed свечи - ретест состоялся

        date = date.plusDays(1);

        generateCandles(instrument, date, 196, 197, 204, 202, 201, 205, 206);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), true, BigDecimal.valueOf(200), TWO);

        assertEquals(8, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
        assertTrue(results.getCrossCandle().getClose().compareTo(BigDecimal.valueOf(204)) == 0);
        assertEquals(10, results.getLastRetestCandle().getCloseDate().getHour());
        assertEquals(59, results.getLastRetestCandle().getCloseDate().getMinute());
        assertTrue(results.getLastRetestCandle().getClose().compareTo(BigDecimal.valueOf(201)) == 0);
        assertEquals(11, results.getConfirmCandle().getCloseDate().getHour());
        assertEquals(59, results.getConfirmCandle().getCloseDate().getMinute());
        assertTrue(results.getConfirmCandle().getClose().compareTo(BigDecimal.valueOf(205)) == 0);
    }

    @Test
    @Transactional
    public void testCheckFallPriceCrossedTheValueWithRetest() {

        Instrument instrument = testHelper.getRandomInstrument();

        LocalDateTime date = LocalDateTime.now().minusDays(30).truncatedTo(DAYS);

        // проверяемое значение = 300, свечей, преодолевающих такую цену - нет

        generateCandles(instrument, date, 204, 203, 198, 203, 205);

        PriceCrossedTheValueWithRetestCheckingResults results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), false, BigDecimal.valueOf(100), TWO);

        assertNull(results.getCrossCandle());
        assertNull(results.getLastRetestCandle());
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, но откатывается ниже диапазона

        date = date.plusDays(1);

        generateCandles(instrument, date, 204, 203, 198, 203, 205);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), false, BigDecimal.valueOf(200), TWO);

        assertNull(results.getCrossCandle());
        assertNull(results.getLastRetestCandle());
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит еще выше

        date = date.plusDays(1);

        generateCandles(instrument, date, 204, 203, 196, 194, 192);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), false, BigDecimal.valueOf(200), TWO);

        assertEquals(8, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
        assertTrue(results.getCrossCandle().getClose().compareTo(BigDecimal.valueOf(196)) == 0);
        assertNull(results.getLastRetestCandle());
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит ниже цены закрытия crossed свечи,
        // но выше диапазона

        date = date.plusDays(1);

        generateCandles(instrument, date, 204, 203, 196, 197);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), false, BigDecimal.valueOf(200), TWO);

        assertEquals(8, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
        assertTrue(results.getCrossCandle().getClose().compareTo(BigDecimal.valueOf(196)) == 0);
        assertNull(results.getLastRetestCandle());
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит в диапазон ниже цены закрытия crossed свечи

        date = date.plusDays(1);

        generateCandles(instrument, date, 204, 203, 196, 198, 199);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), false, BigDecimal.valueOf(200), TWO);

        assertEquals(8, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
        assertTrue(results.getCrossCandle().getClose().compareTo(BigDecimal.valueOf(196)) == 0);
        assertEquals(10, results.getLastRetestCandle().getCloseDate().getHour());
        assertEquals(59, results.getLastRetestCandle().getCloseDate().getMinute());
        assertTrue(results.getLastRetestCandle().getClose().compareTo(BigDecimal.valueOf(199)) == 0);
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит в диапазон ниже цены закрытия crossed свечи,
        // а затем опускается ниже диапазона

        date = date.plusDays(1);

        generateCandles(instrument, date, 204, 203, 196, 198, 199, 203);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), false, BigDecimal.valueOf(200), TWO);

        assertNull(results.getCrossCandle());
        assertNull(results.getLastRetestCandle());
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит в диапазон ниже цены закрытия crossed свечи,
        // затем выходит выше диапазона, но ниже закрытия crossed свечи

        date = date.plusDays(1);

        generateCandles(instrument, date, 204, 203, 196, 198, 199, 197);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), false, BigDecimal.valueOf(200), TWO);

        assertEquals(8, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
        assertTrue(results.getCrossCandle().getClose().compareTo(BigDecimal.valueOf(196)) == 0);
        assertEquals(10, results.getLastRetestCandle().getCloseDate().getHour());
        assertEquals(59, results.getLastRetestCandle().getCloseDate().getMinute());
        assertTrue(results.getLastRetestCandle().getClose().compareTo(BigDecimal.valueOf(199)) == 0);
        assertNull(results.getConfirmCandle());

        // проверяемое значение = 200, цена его преодолевает, далее уходит в диапазон ниже цены закрытия crossed свечи,
        // затем выходит выше диапазона и выше закрытия crossed свечи - ретест состоялся

        date = date.plusDays(1);

        generateCandles(instrument, date, 204, 203, 196, 198, 199, 195, 194);

        results = candlesAnalyzer
                .checkPriceCrossedTheValueWithRetest(instrument, ONE_HOUR, date,
                        LocalDateTime.now(), false, BigDecimal.valueOf(200), TWO);

        assertEquals(8, results.getCrossCandle().getCloseDate().getHour());
        assertEquals(59, results.getCrossCandle().getCloseDate().getMinute());
        assertTrue(results.getCrossCandle().getClose().compareTo(BigDecimal.valueOf(196)) == 0);
        assertEquals(10, results.getLastRetestCandle().getCloseDate().getHour());
        assertEquals(59, results.getLastRetestCandle().getCloseDate().getMinute());
        assertTrue(results.getLastRetestCandle().getClose().compareTo(BigDecimal.valueOf(199)) == 0);
        assertEquals(11, results.getConfirmCandle().getCloseDate().getHour());
        assertEquals(59, results.getConfirmCandle().getCloseDate().getMinute());
        assertTrue(results.getConfirmCandle().getClose().compareTo(BigDecimal.valueOf(195)) == 0);
    }

    @Test
    @Transactional
    public void testGetLastPrice() {

        Instrument instrument = testHelper.getRandomInstrument();

        LocalDateTime date = LocalDateTime.now().minusDays(1).truncatedTo(DAYS);

        generateCandles(instrument, date, 196, 198, 200, 202, 204, 206, 208, 210, 212);

        Optional<BigDecimal> lastPriceOpt = candlesAnalyzer
                .getLastPrice(instrument, ONE_HOUR, date.withHour(6).withMinute(0));

        assertTrue(lastPriceOpt.isEmpty());

        lastPriceOpt = candlesAnalyzer.getLastPrice(instrument, ONE_HOUR, date.withHour(9).withMinute(0));

        assertTrue(lastPriceOpt.get().compareTo(BigDecimal.valueOf(200)) == 0);

        lastPriceOpt = candlesAnalyzer.getLastPrice(instrument, ONE_HOUR, date.withHour(9).withMinute(30));

        assertTrue(lastPriceOpt.get().compareTo(BigDecimal.valueOf(200)) == 0);

        lastPriceOpt = candlesAnalyzer.getLastPrice(instrument, ONE_HOUR, date.withHour(10).withMinute(0));

        assertTrue(lastPriceOpt.get().compareTo(BigDecimal.valueOf(202)) == 0);

        lastPriceOpt = candlesAnalyzer.getLastPrice(instrument, ONE_HOUR, date.withHour(19).withMinute(0));

        assertTrue(lastPriceOpt.get().compareTo(BigDecimal.valueOf(212)) == 0);
    }
}
