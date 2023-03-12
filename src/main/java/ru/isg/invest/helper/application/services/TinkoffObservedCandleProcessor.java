package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.domain.model.Instrument;
import ru.isg.invest.helper.domain.model.TimeFrames;
import ru.isg.invest.helper.infrastructure.repositories.CandleRepository;
import ru.isg.invest.helper.infrastructure.repositories.InstrumentRepository;
import ru.tinkoff.piapi.contract.v1.Candle;
import ru.tinkoff.piapi.core.utils.DateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.time.ZoneOffset.UTC;
import static ru.isg.invest.helper.domain.model.CandleSources.IMPORTER;
import static ru.isg.invest.helper.domain.model.CandleSources.STREAM;
import static ru.isg.invest.helper.domain.model.TimeFrames.FIVE_MINUTES;
import static ru.tinkoff.piapi.contract.v1.SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIVE_MINUTES;
import static ru.tinkoff.piapi.core.utils.MapperUtils.quotationToBigDecimal;

/**
 * Created by s.ivanov on 16.06.2022.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TinkoffObservedCandleProcessor {

    private final TradingDaysService tradingDaysService;
    private final CandlesAnalyzer candlesAnalyzer;
    private final InstrumentRepository instrumentRepository;
    private final CandleRepository candleRepository;

    public void process(Candle candle) {

        checkArgument(candle.getInterval() == SUBSCRIPTION_INTERVAL_FIVE_MINUTES,
                "Candle subscription interval not supported: " + candle.getInterval());

        processFiveMinutesintervalCandle(candle);
    }

    private void processFiveMinutesintervalCandle(Candle tinkoffCandle) {

        LocalDateTime tinkoffDate = LocalDateTime.ofInstant(
                DateUtils.timestampToInstant(tinkoffCandle.getTime()), UTC);
        BigDecimal tinkoffMin = quotationToBigDecimal(tinkoffCandle.getLow());
        BigDecimal tinkoffMax = quotationToBigDecimal(tinkoffCandle.getHigh());
        BigDecimal tinkoffOpen = quotationToBigDecimal(tinkoffCandle.getOpen());
        BigDecimal tinkoffClose = quotationToBigDecimal(tinkoffCandle.getClose());

        Instrument instrument = instrumentRepository.findByFigi(tinkoffCandle.getFigi())
                .orElseThrow();

        Optional<ru.isg.invest.helper.domain.model.Candle> lastCandleOpt = candlesAnalyzer
                .getLastCandle(instrument, FIVE_MINUTES);
        if (lastCandleOpt.isPresent()) {

            ru.isg.invest.helper.domain.model.Candle lastCandle = lastCandleOpt.get();

            // нужно проверить, совпадает ли по времени тек. обрабатываемая свеча с последней свечой из БД

            if (lastCandle.getOpenDate().compareTo(tinkoffDate) == 0) {

                // если совпадает - отправляем на обновление

                saveCandle(instrument, FIVE_MINUTES, tinkoffDate, tinkoffMin, tinkoffMax, tinkoffOpen, tinkoffClose,
                        tinkoffCandle.getVolume());
            } else {
                // если не совпадает, значит свеча может быть либо предыдущей, либо старой,
                // но в любом случа, нужно убедиться, что последняя свеча из БД завершена

                if (lastCandle.isComplete()) {

                    // свеча завершена, теперь нужно проверить, не является ли текущая
                    // обрабатываемая свеча следующий для ТФ 5 мин

                    LocalDateTime nextCandleOpenDate = lastCandle.getOpenDate()
                            .plus(FIVE_MINUTES.getAmount(), FIVE_MINUTES.getChronoUnit());
                    if (nextCandleOpenDate.compareTo(tinkoffDate) == 0) {

                        // текущая обрабатываемая свеча является следующий для ТФ 5 мин - добавляем ее

                        saveCandle(instrument, FIVE_MINUTES, tinkoffDate, tinkoffMin, tinkoffMax, tinkoffOpen, tinkoffClose,
                                tinkoffCandle.getVolume());
                    } else {

                        // текущая обрабатываемая свеча не является следующей, теперь нужно убедиться,
                        // что в промежуток между последней и текущей обрабатываемой свечой не осуществлялась торговля

                        if (tradingDaysService.isInstrumentTraded(instrument, nextCandleOpenDate, tinkoffDate)) {

                            // если торговля осуществлялась - добавление недоступно, нужно дождаться,
                            // пока importer добавит все исторические свечи

                            log.info("Candle saving not available: last candle completed, but next candles not exists");
                        } else {
                            // если торговля осуществлялась - добавляем ее

                            saveCandle(instrument, FIVE_MINUTES, tinkoffDate, tinkoffMin, tinkoffMax, tinkoffOpen,
                                    tinkoffClose, tinkoffCandle.getVolume());
                        }
                    }
                } else {

                    // добавление новой свечи без закрытия старой недоступно, нужно дождаться,
                    // пока importer добавит все исторические свечи

                    log.info("Candle saving not available: last candle not completed");
                }
            }
        } else {
            // если свеч нет вообще, добавление недоступно, нужно дождаться,
            // пока importer добавит все исторические свечи

            log.info("Candle saving not available: candles not exists");
        }
    }

    private ru.isg.invest.helper.domain.model.Candle saveCandle(Instrument instrument, TimeFrames timeFrame,
            LocalDateTime date, BigDecimal min, BigDecimal max, BigDecimal open, BigDecimal close, long volume) {

        log.info(
                "Candle will be saved by TinkoffObservedCandleProcessor for instrument = {}, timeFrame = {}, date = {}",
                instrument.getId(), timeFrame, date);

        return candleRepository.getCandleByInstrumentTimeFrameDate(instrument.getId(), timeFrame, date)
                .map(dbCandle -> {

                    // при обновлении свеча из источника STREAM имеет низший приоритет, т.к. менее точна
                    //
                    // если свеча уже имеется и изменена, нужно проверить источник имеющейся свечи:
                    //      если источник - STREAM, то обновляем в любом случае
                    //      если источник - IMPORTER, то обновляем только если свеча не завершена,
                    //          иначе, если завершена, пропускаем

                    if (dbCandle.getClose().compareTo(close.setScale(4, RoundingMode.HALF_UP)) != 0
                            || dbCandle.getOpen().compareTo(open.setScale(4, RoundingMode.HALF_UP)) != 0
                            || dbCandle.getMin().compareTo(min.setScale(4, RoundingMode.HALF_UP)) != 0
                            || dbCandle.getMax().compareTo(max.setScale(4, RoundingMode.HALF_UP)) != 0
                            || dbCandle.getVolume() != volume
                            || !dbCandle.isComplete()) {

                        if (dbCandle.getSource() == STREAM) {
                            dbCandle
                                    .setClose(close)
                                    .setMax(max)
                                    .setMin(min)
                                    .setOpen(open)
                                    .setVolume(volume)
                                    .setComplete(true)
                                    .setSource(STREAM);
                            dbCandle = candleRepository.save(dbCandle);
                        } else if (dbCandle.getSource() == IMPORTER) {
                            if (!dbCandle.isComplete()) {
                                dbCandle
                                        .setClose(close)
                                        .setMax(max)
                                        .setMin(min)
                                        .setOpen(open)
                                        .setVolume(volume)
                                        .setComplete(true)
                                        .setSource(STREAM);
                                dbCandle = candleRepository.save(dbCandle);
                            }
                        } else {
                            throw new IllegalStateException();
                        }
                    }
                    return dbCandle;
                })
                .orElseGet(() -> candleRepository
                        .save(new ru.isg.invest.helper.domain.model.Candle(instrument, timeFrame, date, min, max, open, close,
                                volume, true, STREAM)));
    }
}