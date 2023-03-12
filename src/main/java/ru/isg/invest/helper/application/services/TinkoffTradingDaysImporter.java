package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.domain.model.TradingDay;
import ru.isg.invest.helper.infrastructure.repositories.TradingDayRepository;
import ru.isg.invest.helper.infrastructure.tinkoff.TinkoffApiClient;
import ru.tinkoff.piapi.contract.v1.TradingSchedule;
import ru.tinkoff.piapi.core.utils.DateUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Created by s.ivanov on 5/22/22.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TinkoffTradingDaysImporter {

    private final TinkoffApiClient tinkoffApiClient;
    private final TradingDayRepository tradingDayRepository;

    @Transactional
    public void importTradingDays() {

        // Допустимые в API даты: from >= текущей даты, from < текущая дата + 7 дней

        LocalDateTime dateFrom = LocalDateTime.now(UTC)
                .truncatedTo(DAYS);

        Instant instantFrom = dateFrom.toInstant(UTC);

        Instant instantTo = dateFrom.plusDays(6).toInstant(UTC);

        log.info("Trading days importer started for period: {} - {}", instantFrom, instantTo);

        List<TradingSchedule> tradingSchedules = tinkoffApiClient.getInstrumentsService()
                .getTradingSchedulesSync(instantFrom, instantTo);

        tradingSchedules.forEach(ts ->
                ts.getDaysList().forEach(d ->
                        saveTradingDay(ts.getExchange(),
                                LocalDate.ofInstant(DateUtils.timestampToInstant(d.getDate()), UTC),
                                d.getIsTradingDay(),
                                LocalDateTime.ofInstant(DateUtils.timestampToInstant(d.getStartTime()), UTC),
                                LocalDateTime
                                        .ofInstant(DateUtils.timestampToInstant(d.getEndTime()), UTC))));
    }

    private TradingDay saveTradingDay(String exchange, LocalDate date, boolean tradingDay, LocalDateTime startDate,
            LocalDateTime endDate) {

        return tradingDayRepository.findTradingDayByExchangeAndDate(exchange, date)
                .map(dbTradingDay -> {
                    dbTradingDay
                            .setTradingDay(tradingDay)
                            .setStartDate(startDate)
                            .setEndDate(endDate);
                    return tradingDayRepository.save(dbTradingDay);
                })
                .orElseGet(() -> tradingDayRepository
                        .save(new TradingDay(exchange, date, tradingDay, startDate, endDate)));
    }
}
