package ru.isg.invest.helper.model;

import lombok.Getter;

import java.time.temporal.ChronoUnit;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.WEEKS;

/**
 * Created by s.ivanov on 5/22/22.
 */
@Getter
public enum TimeFrames {

    FIVE_MINUTES(MINUTES, 5),
    ONE_HOUR(HOURS, 1),
    ONE_DAY(DAYS, 1),
    ONE_WEEK(WEEKS, 1),
    ONE_MONTH(MONTHS, 1);

    private ChronoUnit chronoUnit;
    private int amount;

    TimeFrames(ChronoUnit chronoUnit, int amount) {
        this.chronoUnit = chronoUnit;
        this.amount = amount;
    }
}
