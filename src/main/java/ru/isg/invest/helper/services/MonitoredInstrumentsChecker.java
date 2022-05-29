package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.model.CandlesImportTask;
import ru.isg.invest.helper.model.MonitoredCandle;
import ru.isg.invest.helper.model.TimeFrames;
import ru.isg.invest.helper.repositories.CandlesImportTaskRepository;
import ru.isg.invest.helper.repositories.MonitoredInstrumentRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.DayOfWeek.MONDAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;

/**
 * Created by s.ivanov on 5/22/22.
 */
@Service
@RequiredArgsConstructor
public class MonitoredInstrumentsChecker {

    private final MonitoredInstrumentRepository monitoredInstrumentRepository;
    private final CandlesImportTaskRepository candlesImportTaskRepository;

    public void check() {
        monitoredInstrumentRepository.findAll().forEach(mi ->
                getCandlesImportNeededFrom(mi).ifPresent(dateFrom -> {
                    LocalDateTime dateTo = truncateDateToTimeFrame(LocalDateTime.now(), mi.getTimeFrame())
                            .plus(mi.getTimeFrame().getAmount(), mi.getTimeFrame().getChronoUnit());
                    createCandlesImportTask(mi, dateFrom, dateTo);
                }));
    }

    private Optional<LocalDateTime> getCandlesImportNeededFrom(MonitoredCandle monitoredCandle) {

        if (monitoredCandle.getLastCandleDate() == null) {
            return Optional.of(getFirstCandleFrom(monitoredCandle.getTimeFrame()));
        }

        TimeFrames timeFrame = monitoredCandle.getTimeFrame();

        LocalDateTime nextCandleDateFrom = monitoredCandle.getLastCandleDate()
                .plus(timeFrame.getAmount(), timeFrame.getChronoUnit());
        return Optional.ofNullable(LocalDateTime.now().compareTo(nextCandleDateFrom) >= 0 ? nextCandleDateFrom : null);
    }

    private CandlesImportTask createCandlesImportTask(MonitoredCandle monitoredCandle, LocalDateTime dateFrom,
            LocalDateTime dateTo) {
        CandlesImportTask task = new CandlesImportTask(monitoredCandle.getInstrument(), monitoredCandle.getTimeFrame(),
                dateFrom, dateTo);
        return candlesImportTaskRepository.save(task);
    }

    private LocalDateTime getFirstCandleFrom(TimeFrames timeFrame) {
        LocalDateTime curDate = LocalDateTime.now().truncatedTo(DAYS).withHour(7);
        return switch (timeFrame) {
            case FIVE_MINUTES -> curDate.minus(1, MONTHS);
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
