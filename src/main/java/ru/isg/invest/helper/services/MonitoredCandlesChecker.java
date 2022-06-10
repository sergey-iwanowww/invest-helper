package ru.isg.invest.helper.services;

import jdk.jshell.Snippet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.model.Candle;
import ru.isg.invest.helper.model.CandlesImportTask;
import ru.isg.invest.helper.model.CandlesImportTaskStatuses;
import ru.isg.invest.helper.model.MonitoredCandle;
import ru.isg.invest.helper.model.TimeFrames;
import ru.isg.invest.helper.repositories.CandlesImportTaskRepository;
import ru.isg.invest.helper.repositories.MonitoredInstrumentRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static java.time.DayOfWeek.MONDAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;
import static ru.isg.invest.helper.model.CandlesImportTaskStatuses.ACTIVE;
import static ru.isg.invest.helper.model.CandlesImportTaskStatuses.PROCESSING;

/**
 * Created by s.ivanov on 5/22/22.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoredCandlesChecker {

    private final MonitoredInstrumentRepository monitoredInstrumentRepository;
    private final CandlesImportTaskRepository candlesImportTaskRepository;
    private final CandlesAnalyzer candlesAnalyzer;

    @Transactional
    public void check() {

        log.info("Monitored candles checking started");

        monitoredInstrumentRepository.findAll().forEach(mc -> {

            if (isCandlesImportNeeded(mc)) {

                if (isCandlesImportAvailable(mc)) {

                    log.debug("Need to create import task and available import for instrument with ticker = {}, timeFrame = {}",
                            mc.getInstrument().getTicker(), mc.getTimeFrame());

                    createCandlesImportTask(mc);
                }
            }
        });

        log.info("Monitored candles checking finished");
    }

    private boolean isCandlesImportAvailable(MonitoredCandle mc) {
        List<CandlesImportTask> activeTasks = candlesImportTaskRepository
                .getTasksByInstrumentAndTimeFrame(mc.getInstrument().getId(), mc.getTimeFrame(),
                        List.of(ACTIVE, PROCESSING));
        return activeTasks.size() == 0;
    }

    private boolean isCandlesImportNeeded(MonitoredCandle mc) {
        Optional<Candle> lastCandleOpt = candlesAnalyzer.getLastCandle(mc.getInstrument(), mc.getTimeFrame());
        if (lastCandleOpt.isPresent()) {
            LocalDateTime dateFrom = lastCandleOpt.get().getOpenDate()
                    .plus(mc.getTimeFrame().getAmount(), mc.getTimeFrame().getChronoUnit());
            return LocalDateTime.now().compareTo(dateFrom) >= 0;
        } else {
            return true;
        }
    }

    private CandlesImportTask createCandlesImportTask(MonitoredCandle mc) {

        LocalDateTime dateFrom;

        Optional<Candle> lastCandleOpt = candlesAnalyzer.getLastCandle(mc.getInstrument(), mc.getTimeFrame());
        if (lastCandleOpt.isPresent()) {
            dateFrom = lastCandleOpt.get().getOpenDate()
                    .plus(mc.getTimeFrame().getAmount(), mc.getTimeFrame().getChronoUnit());
        } else {
            dateFrom = getFirstCandleFrom(mc.getTimeFrame());
        }

        LocalDateTime dateTo = truncateDateToTimeFrame(LocalDateTime.now(), mc.getTimeFrame());

        CandlesImportTask task = candlesImportTaskRepository.save(
                new CandlesImportTask(mc.getInstrument(), mc.getTimeFrame(), dateFrom, dateTo));

        log.debug("Import task created for instrument with ticker = {}, timeFrame = {}, dateFrom = {}, dateTo = {}",
                mc.getInstrument().getTicker(), mc.getTimeFrame(), dateFrom, dateTo);

        return task;
    }

    private LocalDateTime getFirstCandleFrom(TimeFrames timeFrame) {
        LocalDateTime curDate = LocalDateTime.now().truncatedTo(DAYS).withHour(0);
        return switch (timeFrame) {
            case FIVE_MINUTES -> curDate.minus(1, DAYS);
            case ONE_HOUR -> curDate.minus(2, MONTHS);
            case ONE_DAY -> curDate.minus(1, YEARS);
            case ONE_WEEK -> curDate.minus(5, YEARS);
            case ONE_MONTH -> curDate.minus(10, YEARS);
        };
    }

    private LocalDateTime truncateDateToTimeFrame(LocalDateTime date, TimeFrames timeFrame) {
        return switch (timeFrame) {
            case FIVE_MINUTES -> LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(),
                    date.getHour(), date.getMinute() / 5 * 5);
            case ONE_HOUR -> date.truncatedTo(HOURS);
            case ONE_DAY -> date.truncatedTo(DAYS).withHour(7);
            case ONE_WEEK -> {
                var tmpDate = date.truncatedTo(DAYS);
                while (tmpDate.getDayOfWeek() != MONDAY) {
                    tmpDate = tmpDate.minusDays(1);
                }
                yield tmpDate.withHour(7);
            }
            case ONE_MONTH -> date.truncatedTo(DAYS).withDayOfMonth(1).withHour(7);
        };
    }
}
