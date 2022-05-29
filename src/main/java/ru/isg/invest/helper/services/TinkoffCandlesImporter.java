package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.dto.ImportCandlesResult;
import ru.isg.invest.helper.model.Candle;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.model.TimeFrames;
import ru.isg.invest.helper.repositories.CandleRepository;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.utils.DateUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.MONDAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static ru.isg.invest.helper.model.TimeFrames.FIVE_MINUTES;
import static ru.isg.invest.helper.model.TimeFrames.ONE_DAY;
import static ru.isg.invest.helper.model.TimeFrames.ONE_HOUR;
import static ru.isg.invest.helper.model.TimeFrames.ONE_MONTH;
import static ru.isg.invest.helper.model.TimeFrames.ONE_WEEK;
import static ru.tinkoff.piapi.core.utils.MapperUtils.quotationToBigDecimal;

/**
 * Created by s.ivanov on 26.05.2022.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TinkoffCandlesImporter implements CandlesImporter {

    private final CandleRepository candleRepository;

    private InvestApi api;

    @Value("${tinkoff.api.token}")
    private String token;

    @PostConstruct
    public void init() {
        api = InvestApi.createSandbox(token);
    }

    @Override
    public ImportCandlesResult importCandles(Instrument instrument, TimeFrames timeFrame, LocalDateTime dateFrom,
            LocalDateTime dateTo) {

        // В API Тинькофф в качестве параметров from и to передаются дата начала и дата окончания свечи
        // для соответствующего таймфрейма

        var instantFrom = dateFrom.toInstant(ZoneOffset.UTC);
        var instantTo = dateTo.toInstant(ZoneOffset.UTC);

        var candleInterval = switch (timeFrame) {
            case FIVE_MINUTES -> CandleInterval.CANDLE_INTERVAL_5_MIN;
            case ONE_HOUR -> CandleInterval.CANDLE_INTERVAL_HOUR;
            case ONE_DAY, ONE_WEEK, ONE_MONTH -> CandleInterval.CANDLE_INTERVAL_DAY;
        };

        var tinkoffCandles = getCandles(instrument.getFigi(), candleInterval, instantFrom, instantTo);

        LocalDateTime date = null;
        BigDecimal min = null;
        BigDecimal max = null;
        BigDecimal open = null;
        BigDecimal close = null;
        Long volume = null;

        LocalDateTime lastCompletedCandleDate = null;

        for (HistoricCandle tinkoffCandle : tinkoffCandles) {

            printCandle(tinkoffCandle);

            var tinkoffDate = LocalDateTime.ofInstant(
                    DateUtils.timestampToInstant(tinkoffCandle.getTime()), ZoneOffset.UTC);
            var tinkoffMin = quotationToBigDecimal(tinkoffCandle.getLow());
            var tinkoffMax = quotationToBigDecimal(tinkoffCandle.getHigh());
            var tinkoffOpen = quotationToBigDecimal(tinkoffCandle.getOpen());
            var tinkoffClose = quotationToBigDecimal(tinkoffCandle.getClose());
            var tinkoffVolume = tinkoffCandle.getVolume();

            if (timeFrame == FIVE_MINUTES || timeFrame == ONE_HOUR || timeFrame == ONE_DAY) {
                saveCandle(instrument, timeFrame, tinkoffDate, tinkoffMin, tinkoffMax, tinkoffOpen,
                        tinkoffClose, tinkoffVolume, tinkoffCandle.getIsComplete());
                if (tinkoffCandle.getIsComplete()) {
                    lastCompletedCandleDate = tinkoffDate;
                }
            } else {

                var truncatedDate = truncateDateToTimeFrame(tinkoffDate, timeFrame);;

                if (date != null && truncatedDate.isAfter(date)) {
                    Candle candle = saveCandle(instrument, timeFrame, date, min, max, open, close, volume, true);
                    lastCompletedCandleDate = date;
                }

                if (date == null || truncatedDate.isAfter(date)) {
                    date = truncatedDate;
                    min = null;
                    max = null;
                    open = tinkoffOpen;
                    volume = null;
                }

                min = min == null || tinkoffMin.compareTo(min) < 0 ? tinkoffMin : min;

                max = max == null || tinkoffMax.compareTo(max) > 0 ? tinkoffMax : max;

                close = tinkoffClose;

                volume = volume == null ? tinkoffVolume : volume + tinkoffVolume;
            }
        }

        if ((timeFrame == ONE_WEEK || timeFrame == ONE_MONTH) && date != null) {
            saveCandle(instrument, timeFrame, date, min, max, open, close, volume, false);
        }

        return new ImportCandlesResult(lastCompletedCandleDate);
    }

    private List<HistoricCandle> getCandles(String figi, CandleInterval candleInterval, Instant instantFrom,
            Instant instantTo) {

        List<HistoricCandle> result = new ArrayList<>();

        long requestMaxPeriod = switch (candleInterval) {
            case CANDLE_INTERVAL_5_MIN -> 24 * 60 * 60;
            case CANDLE_INTERVAL_HOUR -> 7 * 24 * 60 * 60;
            case CANDLE_INTERVAL_DAY -> 365 * 24 * 60 * 60;
            default -> throw new IllegalArgumentException("Candle interval not supported: " + candleInterval);
        };

        for (Instant instant = instantFrom; instant.isBefore(instantTo); instant = instant.plusSeconds(requestMaxPeriod)) {

            // TODO: некоторая гарантия отсутствия > 60 запросов в минуту с одним токеном к api тинькофф
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            Instant tmpInstantTo = instant.plusSeconds(requestMaxPeriod);
            if (tmpInstantTo.isAfter(instantTo)) {
                tmpInstantTo = instantTo;
            }

            log.debug("Try to get candles from tinkoff api: figi = {}, from = {}, to = {}, candleInterval = {}",
                    figi, instant, tmpInstantTo, candleInterval);

            var tinkoffCandles = api.getMarketDataService()
                    .getCandlesSync(figi, instant, tmpInstantTo, candleInterval);

            log.debug("Received {} candles", tinkoffCandles.size());

            result.addAll(tinkoffCandles);
        }

        return result.stream()
                .sorted(Comparator.comparing(c -> DateUtils.timestampToInstant(c.getTime())))
                .collect(Collectors.toList());
    }

    private Candle saveCandle(Instrument instrument, TimeFrames timeFrame, LocalDateTime date, BigDecimal min,
            BigDecimal max, BigDecimal open, BigDecimal close, long volume, boolean complete) {

        return candleRepository.getCandleByInstrumentTimeFrameDate(instrument.getId(), timeFrame, date)
                .map(dbCandle -> {
                    if (!dbCandle.isComplete()) {
                        dbCandle
                                .setClose(close)
                                .setComplete(complete)
                                .setMax(max)
                                .setMin(min)
                                .setOpen(open)
                                .setVolume(volume)
                                .setComplete(complete);
                        dbCandle = candleRepository.save(dbCandle);
                    } else if (dbCandle.getClose().compareTo(close.setScale(4, RoundingMode.HALF_UP)) != 0
                            || dbCandle.getOpen().compareTo(open.setScale(4, RoundingMode.HALF_UP)) != 0
                            || dbCandle.getMin().compareTo(min.setScale(4, RoundingMode.HALF_UP)) != 0
                            || dbCandle.getMax().compareTo(max.setScale(4, RoundingMode.HALF_UP)) != 0
                            || dbCandle.getVolume() != volume
                            || dbCandle.isComplete() != complete) {
                        throw new IllegalStateException(
                                "Свеча в БД закрыта, но не соответсвует свече, полученной от Тинькофф API");
                    }
                    return dbCandle;
                })
                .orElseGet(() -> candleRepository
                        .save(new Candle(instrument, timeFrame, date, min, max, open, close, volume, complete)));
    }

    private static void printCandle(HistoricCandle candle) {
        var open = quotationToBigDecimal(candle.getOpen());
        var close = quotationToBigDecimal(candle.getClose());
        var high = quotationToBigDecimal(candle.getHigh());
        var low = quotationToBigDecimal(candle.getLow());
        var volume = candle.getVolume();
        var time = DateUtils.timestampToString(candle.getTime());
        log.info("o: {}, c: {}, l: {}, h: {}, v: {}, t: {}, compl: {}",
                open, close, low, high, volume, time, candle.getIsComplete());
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