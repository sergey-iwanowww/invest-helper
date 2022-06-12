package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.model.Candle;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.model.TimeFrames;
import ru.isg.invest.helper.repositories.CandleRepository;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.utils.DateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.ZoneOffset.UTC;
import static ru.isg.invest.helper.model.TimeFrames.ONE_DAY;
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
    private final TinkoffApiClient tinkoffApiClient;
    private final TradingDaysService tradingDaysService;

    private List<HistoricCandle> getCandles(String figi, CandleInterval candleInterval, Instant instantFrom,
            Instant instantTo) {

        List<HistoricCandle> result = new ArrayList<>();

        long requestMaxPeriod = switch (candleInterval) {
            case CANDLE_INTERVAL_5_MIN -> 24 * 60 * 60;
            case CANDLE_INTERVAL_HOUR -> 7 * 24 * 60 * 60;
            case CANDLE_INTERVAL_DAY -> 365 * 24 * 60 * 60;
            default -> throw new IllegalArgumentException("Candle interval not supported: " + candleInterval);
        };

        for (Instant instant = instantFrom; instant.isBefore(instantTo); instant = instant
                .plusSeconds(requestMaxPeriod)) {

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

            var tinkoffCandles = tinkoffApiClient.getMarketDataService()
                    .getCandlesSync(figi, instant, tmpInstantTo, candleInterval);

            log.debug("Received {} candles", tinkoffCandles.size());

            result.addAll(tinkoffCandles);
        }

        return result.stream()
                .sorted(Comparator.comparing(c -> DateUtils.timestampToInstant(c.getTime())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void importCandles(Instrument instrument, TimeFrames timeFrame, LocalDateTime dateFrom,
            LocalDateTime dateTo) {

        // В API Тинькофф в качестве параметров from и to передаются дата начала и дата окончания свечи
        // для соответствующего таймфрейма

        var instantFrom = dateFrom.toInstant(UTC);

        if (dateTo == null) {
            dateTo = TimeFrameUtils.getTimeFrameOpenDate(LocalDateTime.now(), timeFrame)
                    .plus(timeFrame.getAmount(), timeFrame.getChronoUnit());
        }
        var instantTo = dateTo.toInstant(UTC);

        // API Тинькофф не позволяет стянуть сразу готовые свечи ТФ ONE_WEEK, ONE_MONTH
        // Необходимо стягивать свечи ТФ ONE_DAY и вручную формировать свечи ONE_WEEK, ONE_MONTH

        TimeFrames timeFrameToUse = timeFrame;
        if (timeFrame == ONE_WEEK || timeFrame == ONE_MONTH) {
            timeFrameToUse = ONE_DAY;
        }

        var candleInterval = switch (timeFrameToUse) {
            case FIVE_MINUTES -> CandleInterval.CANDLE_INTERVAL_5_MIN;
            case ONE_HOUR -> CandleInterval.CANDLE_INTERVAL_HOUR;
            case ONE_DAY -> CandleInterval.CANDLE_INTERVAL_DAY;
            default -> throw new IllegalStateException();
        };

        List<Candle> gettedCandles = newArrayList();

        var tinkoffCandles = getCandles(instrument.getFigi(), candleInterval, instantFrom, instantTo);
        for (HistoricCandle tinkoffCandle : tinkoffCandles) {

            printCandle(tinkoffCandle);

            LocalDateTime tinkoffDate = LocalDateTime.ofInstant(
                    DateUtils.timestampToInstant(tinkoffCandle.getTime()), UTC);
            BigDecimal tinkoffMin = quotationToBigDecimal(tinkoffCandle.getLow());
            BigDecimal tinkoffMax = quotationToBigDecimal(tinkoffCandle.getHigh());
            BigDecimal tinkoffOpen = quotationToBigDecimal(tinkoffCandle.getOpen());
            BigDecimal tinkoffClose = quotationToBigDecimal(tinkoffCandle.getClose());

            gettedCandles.add(saveCandle(instrument, timeFrameToUse, tinkoffDate, tinkoffMin, tinkoffMax, tinkoffOpen,
                    tinkoffClose, tinkoffCandle.getVolume(), tinkoffCandle.getIsComplete()));
        }

        // формируем свечи ТФ ONE_WEEK, ONE_MONTH вручную
        if (timeFrame == ONE_WEEK || timeFrame == ONE_MONTH) {

            LocalDateTime date = null;
            BigDecimal min = null;
            BigDecimal max = null;
            BigDecimal open = null;
            BigDecimal close = null;
            Long volume = null;

            for (Candle candle : gettedCandles) {

                // Определяем дату свечи соотв-го ТФ-а
                var truncatedDate = TimeFrameUtils.getTimeFrameOpenDate(candle.getOpenDate(), timeFrame);

                // если дата свечи больше даты предыдущей свечи - нужно сохранить старые данные
                if (date != null && truncatedDate.isAfter(date)) {
                    saveCandle(instrument, timeFrame, date, min, max, open, close, volume, true);
                }

                // если свечей до сих пор не было или если дата свечи больше даты предыдущей свечи - сбросить
                // накопленные для свечи данные
                if (date == null || truncatedDate.isAfter(date)) {
                    date = truncatedDate;
                    min = null;
                    max = null;
                    open = candle.getOpen();
                    volume = null;
                }

                // накопить данные свечи

                min = min == null || candle.getMin().compareTo(min) < 0 ? candle.getMin() : min;

                max = max == null || candle.getMax().compareTo(max) > 0 ? candle.getMax() : max;

                close = candle.getClose();

                volume = volume == null ? candle.getVolume() : volume + candle.getVolume();
            }

            // Для вручную формируемых свечей если есть накопленные данные - сохраняем накопленные данные.
            // Закрыта свеча или нет, определяем, используя справочник торговых дней.
            if (date != null) {

                // если последняя дневная свеча не закрыта, недельная/месяная так же не может быть закрыта
                // если закрыта - проверяем, попадает ли следующая дневная свеча в период текущей недельной/месячной свечи
                // если не попадает, значит текущую недельную/месячную свечу можно закрыть
                // если попадает, определяем, торгуется ли инструмент от даты начала следующей дневной свечи (включая)
                //      до начала следующей недельной/месячной свечи (исключая):
                // если не торгуется - свечу закрываем
                // иначе - нет

                Candle lastGettedCandle = gettedCandles.get(gettedCandles.size() - 1);

                boolean complete;

                if (lastGettedCandle.isComplete()) {
                    LocalDateTime nextDayCandleDate = lastGettedCandle.getOpenDate().plus(timeFrame.getAmount(), timeFrame.getChronoUnit());
                    LocalDateTime nextDate = TimeFrameUtils.getTimeFrameOpenDate(nextDayCandleDate, timeFrame);
                    if (nextDate.isAfter(date)) {
                        complete = true;
                    } else {
                        complete = !tradingDaysService.isInstrumentTraded(instrument, nextDayCandleDate, nextDate);
                    }
                } else {
                    complete = false;
                }

                saveCandle(instrument, timeFrame, date, min, max, open, close, volume, complete);
            }
        }
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
}