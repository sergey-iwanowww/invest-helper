package ru.isg.invest.helper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.model.CandlesImportTask;
import ru.isg.invest.helper.model.CandlesImportTaskStatuses;

import java.util.List;
import java.util.UUID;

/**
 * Created by s.ivanov on 25.05.2022.
 */
public interface CandlesImportTaskRepository extends JpaRepository<CandlesImportTask, UUID> {

    List<CandlesImportTask> getTasksByStatusOrderByCreatedDate(CandlesImportTaskStatuses status);
}
