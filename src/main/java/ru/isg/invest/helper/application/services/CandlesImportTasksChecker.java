package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.domain.model.CandlesImportTask;
import ru.isg.invest.helper.infrastructure.repositories.CandlesImportTaskRepository;

import static ru.isg.invest.helper.domain.model.CandlesImportTaskStatuses.ACTIVE;

/**
 * Created by s.ivanov on 25.05.2022.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandlesImportTasksChecker {

    private final CandlesImportTaskRepository candlesImportTaskRepository;
    private final CandlesImporter candlesImporter;

    @Transactional
    public void check() {

        log.info("Candles import tasks checking started");

        candlesImportTaskRepository.findTasksByStatusOrderByCreatedDate(ACTIVE)
                .forEach(this::processTask);

        log.info("Candles import tasks checking finished");
    }

    public void processTask(CandlesImportTask task) {

        task.processingStarted();

        log.debug("Candles import started for instrument with id = {}, timeFrame = {}, dateFrom = {}, dateTo = {}",
                task.getInstrument().getId(), task.getTimeFrame(), task.getDateFrom(), task.getDateTo());

        candlesImporter.importCandles(task.getInstrument(), task.getTimeFrame(), task.getDateFrom(), task.getDateTo(),
                task.isCompletedOnly());

        task.processingFinished();

        log.debug("Candles import finished");

        candlesImportTaskRepository.save(task);
    }
}
