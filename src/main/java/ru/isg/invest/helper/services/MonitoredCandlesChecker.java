package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.model.Candle;
import ru.isg.invest.helper.model.CandlesImportTask;
import ru.isg.invest.helper.model.MonitoredCandle;
import ru.isg.invest.helper.model.TimeFrames;
import ru.isg.invest.helper.repositories.CandlesImportTaskRepository;
import ru.isg.invest.helper.repositories.MonitoredInstrumentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;
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
    private final TradingDaysService tradingDaysService;

    @Transactional
    public void check() {

        log.info("Monitored candles checking started");

        monitoredInstrumentRepository.findAll().forEach(mc -> {

            if (isCandlesImportAvailable(mc)) {

                Optional<LocalDateTime> candlesImportNeededDateFrom = checkCandlesImportNeeded(mc);
                if (candlesImportNeededDateFrom.isPresent()) {

                    log.debug(
                            "Need to create import task and available import for instrument with ticker = {}, timeFrame = {}",
                            mc.getInstrument().getTicker(), mc.getTimeFrame());

                    createCandlesImportTask(mc, candlesImportNeededDateFrom.get());
                }
            }
        });

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
            return getDateFrom(lastCandleOpt.get(), mc);
        } else {
            return Optional.of(TimeFrameUtils.getTimeFrameOpenDateDefault(mc.getTimeFrame()));
        }
    }

    private Optional<LocalDateTime> getDateFrom(Candle lastCandle, MonitoredCandle mc) {

        // если свеча закрыта, нужно проверять, торговался ли интрумент в период от начала следующей свечи и до текущей даты
        // если свеча не закрыта, нужно ее обновить

        if (lastCandle.isComplete()) {

            LocalDateTime nextCandleOpenDate = lastCandle.getOpenDate()
                    .plus(mc.getTimeFrame().getAmount(), mc.getTimeFrame().getChronoUnit());

            if (tradingDaysService.isInstrumentTraded(mc.getInstrument(), nextCandleOpenDate, LocalDateTime.now())) {
                return Optional.of(nextCandleOpenDate);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(lastCandle.getOpenDate());
        }
    }

    private CandlesImportTask createCandlesImportTask(MonitoredCandle mc, LocalDateTime dateFrom) {

        CandlesImportTask task = candlesImportTaskRepository.save(
                new CandlesImportTask(mc.getInstrument(), mc.getTimeFrame(), dateFrom, null));

        log.debug("Import task created for instrument with ticker = {}, timeFrame = {}, dateFrom = {}",
                mc.getInstrument().getTicker(), mc.getTimeFrame(), dateFrom);

        return task;
    }


}
