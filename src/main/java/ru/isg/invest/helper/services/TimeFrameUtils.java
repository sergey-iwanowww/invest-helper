package ru.isg.invest.helper.services;

import ru.isg.invest.helper.model.TimeFrames;

import java.time.LocalDateTime;

import static java.time.DayOfWeek.MONDAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;
import static ru.isg.invest.helper.model.TimeFrames.ONE_MONTH;
import static ru.isg.invest.helper.model.TimeFrames.ONE_WEEK;

/**
 * Created by s.ivanov on 13.06.2022.
 */
public class TimeFrameUtils {

    public static LocalDateTime getTimeFrameOpenDate(LocalDateTime date, TimeFrames timeFrame) {
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

    public static LocalDateTime getTimeFrameCloseDate(LocalDateTime date, TimeFrames timeFrame) {
        return switch (timeFrame) {
            case FIVE_MINUTES, ONE_HOUR -> getTimeFrameOpenDate(date, timeFrame)
                    .plus(timeFrame.getAmount(), timeFrame.getChronoUnit())
                    .minus(1, MILLIS);
            case ONE_DAY -> date.truncatedTo(DAYS)
                    .plusDays(1)
                    .minus(1, MILLIS);
            case ONE_WEEK -> getTimeFrameOpenDate(date, ONE_WEEK)
                    .plusWeeks(1)
                    .truncatedTo(DAYS)
                    .minus(1, MILLIS);
            case ONE_MONTH -> getTimeFrameOpenDate(date, ONE_MONTH)
                    .plusMonths(1)
                    .truncatedTo(DAYS)
                    .minus(1, MILLIS);
        };
    }

    public static LocalDateTime getTimeFrameOpenDateDefault(TimeFrames timeFrame) {
        LocalDateTime curDate = LocalDateTime.now().truncatedTo(DAYS).withHour(7);
        return switch (timeFrame) {
            case FIVE_MINUTES -> curDate.minus(1, DAYS);
            case ONE_HOUR -> curDate.minus(2, MONTHS);
            case ONE_DAY -> curDate.minus(1, YEARS);
            case ONE_WEEK -> curDate.minus(5, YEARS);
            case ONE_MONTH -> curDate.minus(10, YEARS);
        };
    }
}
