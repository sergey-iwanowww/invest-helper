package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.domain.model.Candle;
import ru.isg.invest.helper.domain.model.CandlesImportTask;
import ru.isg.invest.helper.domain.model.MonitoredCandle;
import ru.isg.invest.helper.domain.model.TimeFrames;
import ru.isg.invest.helper.infrastructure.repositories.CandlesImportTaskRepository;
import ru.isg.invest.helper.infrastructure.repositories.MonitoredInstrumentRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.time.ZoneOffset.UTC;
import static ru.isg.invest.helper.domain.model.CandlesImportTaskStatuses.ACTIVE;
import static ru.isg.invest.helper.domain.model.CandlesImportTaskStatuses.PROCESSING;
import static ru.isg.invest.helper.domain.model.TimeFrames.FIVE_MINUTES;

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
    private final TradingDaysService tradingDaysService;
    private final TinkoffInstrumentsObserver tinkoffInstrumentsObserver;

    @PostConstruct
    public void init() {
        check();
    }

    @Transactional
    public void check() {

        log.info("Monitored candles checking started");

        Set<String> figis = newHashSet();

        for (MonitoredCandle mc : monitoredInstrumentRepository.findAll()) {
            if (isCandlesImportAvailable(mc)) {

                Optional<LocalDateTime> candlesImportNeededDateFrom = checkCandlesImportNeeded(mc);
                if (candlesImportNeededDateFrom.isPresent()) {

                    log.debug(
                            "Need to create import task and available import for instrument = {}, timeFrame = {}",
                            mc.getInstrument().getId(), mc.getTimeFrame());

                    createCandlesImportTask(mc, candlesImportNeededDateFrom.get());
                } else {
                    log.warn("Not need to create import task for instrument = {}, timeframe = {}",
                            mc.getInstrument().getId(),
                            mc.getTimeFrame());
                }
            } else {
                log.warn("Not available creating import task for instrument = {}, timeframe = {}",
                        mc.getInstrument().getId(),
                        mc.getTimeFrame());
            }

            if (mc.getTimeFrame() == FIVE_MINUTES) {
                figis.add(mc.getInstrument().getFigi());
            }
        }

        tinkoffInstrumentsObserver.updateObservedCandles(figis);

        log.info("Monitored candles checking finished");
    }

    private boolean isCandlesImportAvailable(MonitoredCandle mc) {
        List<CandlesImportTask> activeTasks = candlesImportTaskRepository
                .findTasksByInstrumentAndTimeFrame(mc.getInstrument().getId(), mc.getTimeFrame(),
                        List.of(ACTIVE, PROCESSING));
        return activeTasks.size() == 0;
    }

    private Optional<LocalDateTime> checkCandlesImportNeeded(MonitoredCandle mc) {

        Optional<Candle> lastCandleOpt = candlesAnalyzer.getLastCandle(mc.getInstrument(), mc.getTimeFrame());
        if (lastCandleOpt.isPresent()) {
            // если последняя свеча найдена, вычисляем дату, с которой нужно делать импорт
            return getDateFrom(lastCandleOpt.get(), mc);
        } else {
            return Optional.of(TimeFrameUtils.getTimeFrameOpenDateDefault(mc.getTimeFrame()));
        }
    }

    private Optional<LocalDateTime> getDateFrom(Candle lastCandle, MonitoredCandle mc) {

        TimeFrames tf = mc.getTimeFrame();

        LocalDateTime сandleOpenDate = lastCandle.getOpenDate();
        LocalDateTime nextCandleOpenDate = сandleOpenDate.plus(tf.getAmount(), tf.getChronoUnit());

        LocalDateTime timeFrameDate = TimeFrameUtils.getTimeFrameOpenDate(LocalDateTime.now(UTC), tf);
        LocalDateTime prevTimeFrameDate = timeFrameDate.minus(tf.getAmount(), tf.getChronoUnit());
        LocalDateTime nextTimeFrameDate = timeFrameDate.plus(tf.getAmount(), tf.getChronoUnit());

        if (tf == FIVE_MINUTES) {

            if (сandleOpenDate.compareTo(timeFrameDate) == 0) {
                // если последняя свеча соотв-т текущему таймфрейму, импорт не делаем
                return Optional.empty();
            } else {
                if (!lastCandle.isComplete()) {
                    // если свеча не завершена, делаем импорт начиная от этой свечи
                    return Optional.of(сandleOpenDate);
                } else {
                    // если свеча завершена, проверяем, а не соотв-т ли свече предыдущему ТФ
                    if (сandleOpenDate.compareTo(prevTimeFrameDate) == 0) {
                        // если последняя свеча соотв-т предыдущему ТФ, импорт не делаем
                        return Optional.empty();
                    } else {
                        // если последняя свеча более ранняя, проверяем торговался ли инструмент, начиная от даты след. свечи до текущего ТФ (исключая)
                        if (tradingDaysService.isInstrumentTraded(mc.getInstrument(), nextCandleOpenDate, timeFrameDate)) {
                            // если торговался, делаем импорт, начиная от след. свечи
                            return Optional.of(nextCandleOpenDate);
                        } else {
                            // если не торговался, импорт не делаем
                            return Optional.empty();
                        }
                    }
                }
            }
        } else {
            if (!lastCandle.isComplete()) {
                // если свеча не завершена, делаем импорт начиная от этой свечи
                return Optional.of(сandleOpenDate);
            } else {
                if (сandleOpenDate.compareTo(timeFrameDate) == 0) {
                    // если последняя свеча соотв-т текущему таймфрейму, импорт не делаем
                    return Optional.empty();
                } else {
                    // если последняя свеча более ранняя, проверяем торговался ли инструмент, начиная от даты след. свечи до след. ТФ (исключая)
                    if (tradingDaysService.isInstrumentTraded(mc.getInstrument(), nextCandleOpenDate, nextTimeFrameDate)) {
                        // если торговался, делаем импорт, начиная от след. свечи
                        return Optional.of(nextCandleOpenDate);
                    } else {
                        // если не торговался, импорт не делаем
                        return Optional.empty();
                    }
                }
            }
        }
    }

    private CandlesImportTask createCandlesImportTask(MonitoredCandle mc, LocalDateTime dateFrom) {

        CandlesImportTask task = candlesImportTaskRepository.save(
                new CandlesImportTask(mc.getInstrument(), mc.getTimeFrame(), dateFrom, null, true));

        log.debug("Import task created for instrument with ticker = {}, timeFrame = {}, dateFrom = {}",
                mc.getInstrument().getTicker(), mc.getTimeFrame(), dateFrom);

        return task;
    }

}
