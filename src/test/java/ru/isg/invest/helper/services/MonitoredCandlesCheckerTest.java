package ru.isg.invest.helper.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.application.services.MonitoredCandlesChecker;
import ru.isg.invest.helper.application.services.TimeFrameUtils;
import ru.isg.invest.helper.domain.model.Candle;
import ru.isg.invest.helper.domain.model.CandlesImportTask;
import ru.isg.invest.helper.domain.model.Instrument;
import ru.isg.invest.helper.domain.model.MonitoredCandle;
import ru.isg.invest.helper.domain.model.TradingDay;
import ru.isg.invest.helper.infrastructure.repositories.CandleRepository;
import ru.isg.invest.helper.infrastructure.repositories.CandlesImportTaskRepository;
import ru.isg.invest.helper.infrastructure.repositories.MonitoredInstrumentRepository;
import ru.isg.invest.helper.infrastructure.repositories.TradingDayRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.isg.invest.helper.domain.model.CandleSources.IMPORTER;
import static ru.isg.invest.helper.domain.model.TimeFrames.ONE_HOUR;

/**
 * Created by s.ivanov on 30.05.2022.
 */
@SpringBootTest
public class MonitoredCandlesCheckerTest {

    @Autowired
    private CandleRepository candleRepository;
    @Autowired
    private TestHelper testHelper;
    @Autowired
    private CandlesImportTaskRepository candlesImportTaskRepository;
    @Autowired
    private MonitoredInstrumentRepository monitoredInstrumentRepository;
    @Autowired
    private TradingDayRepository tradingDayRepository;
    @Autowired
    private MonitoredCandlesChecker monitoredCandlesChecker;

    @Test
    @Transactional
    public void testCheck() {

        List<Instrument> instrs = testHelper.getRandomInstruments(5);

        // instr0 - создание задачи недоступно - активная задача уже есть

        Instrument instr0 = instrs.get(0);

        CandlesImportTask existsTask = new CandlesImportTask(instr0, ONE_HOUR, LocalDateTime.now(UTC),
                LocalDateTime.now(UTC), true);
        existsTask.processingStarted();
        candlesImportTaskRepository.save(existsTask);

        monitoredInstrumentRepository.save(new MonitoredCandle(instr0, ONE_HOUR));

        // instr1 - создание задачи доступно, предыдущей свечи нет, дата начала дефолтная

        Instrument instr1 = instrs.get(1);

        monitoredInstrumentRepository.save(new MonitoredCandle(instr1, ONE_HOUR));

        // instr2 - создание задачи доступно, предыдущая свеча не завершена, дата начала = дате начала этой свечи

        Instrument instr2 = instrs.get(2);

        LocalDateTime prevHour = LocalDateTime.now(UTC).minusHours(1).truncatedTo(HOURS);
        Candle instr2PrevCandle = candleRepository.save(new Candle(instr2, ONE_HOUR, prevHour, ZERO, ZERO, ZERO, ZERO,
                0, false, IMPORTER));

        monitoredInstrumentRepository.save(new MonitoredCandle(instr2, ONE_HOUR));

        // instr3 - создание задачи доступно, предыдущая свеча завершена, инструмент в период от окончания пред. свечи до
        // текущей даты торговался, дата начала = дате начала след. свечи после завершенной

        Instrument instr3 = instrs.get(3);

        tradingDayRepository.save(new TradingDay(instr3.getExchange(), LocalDate.now(UTC), true,
                LocalDateTime.now(UTC).minusHours(2).truncatedTo(HOURS),
                LocalDateTime.now(UTC).minusHours(2).truncatedTo(HOURS)));

        prevHour = LocalDateTime.now(UTC).minusHours(4).truncatedTo(HOURS);
        Candle instr3PrevCandle = candleRepository.save(new Candle(instr3, ONE_HOUR, prevHour, ZERO, ZERO, ZERO, ZERO,
                0, true, IMPORTER));

        monitoredInstrumentRepository.save(new MonitoredCandle(instr3, ONE_HOUR));

        // instr4 - создание задачи доступно, предыдущая свеча завершена, инструмент в период от окончания пред. свечи до
        // текущей даты НЕ торговался, дата начала = null

        Instrument instr4 = instrs.get(4);

        tradingDayRepository.save(new TradingDay(instr4.getExchange(), LocalDate.now(UTC), false,
                LocalDateTime.now(UTC).truncatedTo(DAYS), LocalDateTime.now(UTC).truncatedTo(DAYS)));

        prevHour = LocalDateTime.now(UTC).minusHours(4).truncatedTo(HOURS);
        candleRepository.save(new Candle(instr4, ONE_HOUR, prevHour, ZERO, ZERO, ZERO, ZERO, 0, true, IMPORTER));

        monitoredInstrumentRepository.save(new MonitoredCandle(instr4, ONE_HOUR));

        // Запуск проверки
        monitoredCandlesChecker.check();

        List<CandlesImportTask> instr0Tasks = candlesImportTaskRepository
                .findCandlesImportTasksByInstrumentId(instr0.getId());
        assertEquals(1, instr0Tasks.size());
        assertEquals(existsTask.getId(), instr0Tasks.get(0).getId());

        List<CandlesImportTask> instr1Tasks = candlesImportTaskRepository
                .findCandlesImportTasksByInstrumentId(instr1.getId());
        assertEquals(1, instr1Tasks.size());
        CandlesImportTask instr1Task = instr1Tasks.get(0);
        assertEquals(TimeFrameUtils.getTimeFrameOpenDateDefault(ONE_HOUR), instr1Task.getDateFrom());
        assertNull(instr1Task.getDateTo());

        List<CandlesImportTask> instr2Tasks = candlesImportTaskRepository
                .findCandlesImportTasksByInstrumentId(instr2.getId());
        assertEquals(1, instr2Tasks.size());
        CandlesImportTask instr2Task = instr2Tasks.get(0);
        assertEquals(instr2PrevCandle.getOpenDate(), instr2Task.getDateFrom());
        assertNull(instr2Task.getDateTo());

        List<CandlesImportTask> instr3Tasks = candlesImportTaskRepository
                .findCandlesImportTasksByInstrumentId(instr3.getId());
        assertEquals(1, instr3Tasks.size());
        CandlesImportTask instr3Task = instr3Tasks.get(0);
        assertEquals(instr3PrevCandle.getOpenDate().plus(ONE_HOUR.getAmount(), ONE_HOUR.getChronoUnit()),
                instr3Task.getDateFrom());
        assertNull(instr3Task.getDateTo());

        List<CandlesImportTask> instr4Tasks = candlesImportTaskRepository
                .findCandlesImportTasksByInstrumentId(instr4.getId());
        assertTrue(instr4Tasks.isEmpty());
    }
}
