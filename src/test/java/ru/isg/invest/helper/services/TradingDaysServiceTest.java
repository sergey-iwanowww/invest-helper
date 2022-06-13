package ru.isg.invest.helper.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.model.TradingDay;
import ru.isg.invest.helper.repositories.TradingDayRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by s.ivanov on 30.05.2022.
 */
@SpringBootTest
public class TradingDaysServiceTest {

    @Autowired
    private TradingDayRepository tradingDayRepository;
    @Autowired
    private TradingDaysService tradingDaysService;
    @Autowired
    private TestHelper testHelper;

    @Test
    @Transactional
    public void test() {

        Instrument instr = testHelper.getRandomInstrument();

        tradingDayRepository.save(new TradingDay(instr.getExchange(), LocalDate.now(), false,
                LocalDateTime.now(), LocalDateTime.now()));

        LocalDateTime tradingDayDateFrom = LocalDateTime.now().plusDays(1).truncatedTo(DAYS);
        int startTradingHour = 7;
        int endTradingHour = 15;

        tradingDayRepository.save(new TradingDay(instr.getExchange(), tradingDayDateFrom.toLocalDate(), true,
                LocalDateTime.now().plusDays(1).withHour(startTradingHour).truncatedTo(HOURS),
                LocalDateTime.now().plusDays(1).withHour(endTradingHour).truncatedTo(HOURS)));

        LocalDateTime notTradingDayDateFrom = LocalDateTime.now().plusDays(2).truncatedTo(DAYS);

        tradingDayRepository.save(new TradingDay(instr.getExchange(), notTradingDayDateFrom.toLocalDate(), false,
                LocalDateTime.now(), LocalDateTime.now()));

        // даты выпадают на даты, когда tradingDays нет, в этом случае по умолчанию считается, что интсрумент торговался
        boolean traded = tradingDaysService
                .isInstrumentTraded(instr, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(2));
        assertTrue(traded);

        // даты выпадают на дни, когда инструмент не торгуется
        traded = tradingDaysService
                .isInstrumentTraded(instr, LocalDateTime.now(), LocalDateTime.now());
        assertFalse(traded);

        // правая граница дат == дате начала дня, когда инструмент торгуется, но поскольку правая граница диапазона
        // в отбор не включается, должно вернуться false
        traded = tradingDaysService
                .isInstrumentTraded(instr, LocalDateTime.now(), tradingDayDateFrom);
        assertFalse(traded);

        // правая граница дат > даты начала дня, когда инструмент торгуется, но < даты начала периода торговли
        traded = tradingDaysService
                .isInstrumentTraded(instr, LocalDateTime.now(), tradingDayDateFrom.plusHours(1));
        assertFalse(traded);

        // правая граница дат == дате начала периода торговли инструмента, но поскольку правая граница диапазона
        // в отбор не включается, должно вернуться false
        traded = tradingDaysService
                .isInstrumentTraded(instr, LocalDateTime.now(), tradingDayDateFrom.withHour(startTradingHour));
        assertFalse(traded);

        // правая граница дат попадает в период торговли инструмента
        traded = tradingDaysService
                .isInstrumentTraded(instr, LocalDateTime.now(), tradingDayDateFrom.withHour(startTradingHour + 1));
        assertTrue(traded);

        // левая граница дат попадает в период торговли инструмента
        traded = tradingDaysService
                .isInstrumentTraded(instr, tradingDayDateFrom.withHour(startTradingHour + 1), LocalDateTime.now().plusDays(2));
        assertTrue(traded);

        // левая граница дат == дате окончания периода торговли инструмента, лева граница диапазона
        // в отбор не включается, должно вернуться true
        traded = tradingDaysService
                .isInstrumentTraded(instr, tradingDayDateFrom.withHour(endTradingHour), LocalDateTime.now().plusDays(2));
        assertTrue(traded);

        // левая граница дат == дате начала дня, когда торговли нет, следующего за днем, когда торговля есть
        traded = tradingDaysService
                .isInstrumentTraded(instr, notTradingDayDateFrom, LocalDateTime.now().plusDays(2));
        assertFalse(traded);
    }
}
