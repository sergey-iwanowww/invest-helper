package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.dto.ImportCandlesResult;
import ru.isg.invest.helper.model.CandlesImportTask;
import ru.isg.invest.helper.repositories.CandlesImportTaskRepository;

import static ru.isg.invest.helper.model.CandlesImportTaskStatuses.ACTIVE;

/**
 * Created by s.ivanov on 25.05.2022.
 */
@Service
@RequiredArgsConstructor
public class CandlesImportTasksChecker {

    private final CandlesImportTaskRepository candlesImportTaskRepository;
    private final CandlesImporter candlesImporter;

    public void check() {
        candlesImportTaskRepository.getTasksByStatusOrderByCreatedDate(ACTIVE)
                .forEach(this::processTask);
    }

    private void processTask(CandlesImportTask task) {

        task.processingStarted();

        ImportCandlesResult result = candlesImporter.importCandles(task.getInstrument(), task.getTimeFrame(),
                task.getDateFrom(), task.getDateTo());

        task.processingFinished(result.getLastCompletedCandleDate());

        candlesImportTaskRepository.save(task);
    }
}
