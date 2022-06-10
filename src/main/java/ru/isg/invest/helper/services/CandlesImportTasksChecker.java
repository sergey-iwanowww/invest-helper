package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.dto.ImportCandlesResult;
import ru.isg.invest.helper.model.CandlesImportTask;
import ru.isg.invest.helper.repositories.CandlesImportTaskRepository;

import static ru.isg.invest.helper.model.CandlesImportTaskStatuses.ACTIVE;

/**
 * Created by s.ivanov on 25.05.2022.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandlesImportTasksChecker {

    private final CandlesImportTaskRepository candlesImportTaskRepository;
    private final CandlesImporter candlesImporter;

    public void check() {

        log.info("Candles import tasks checking started");

        candlesImportTaskRepository.getTasksByStatusOrderByCreatedDate(ACTIVE)
                .forEach(this::processTask);

        log.info("Candles import tasks checking finished");
    }

    @Transactional
    public void processTask(CandlesImportTask task) {

        task.processingStarted();

        log.debug("Candles import started for instrument with id = {}, timeFrame = {}, dateFrom = {}, dateTo = {}",
                task.getInstrument().getId(), task.getTimeFrame(), task.getDateFrom(), task.getDateTo());

        ImportCandlesResult result = candlesImporter.importCandles(task.getInstrument(), task.getTimeFrame(),
                task.getDateFrom(), task.getDateTo());

        task.processingFinished(result.getLastCompletedCandleDate());

        log.debug("Candles import finished, last completed candleDate = {}", result.getLastCompletedCandleDate());

        candlesImportTaskRepository.save(task);
    }
}
