package ru.isg.invest.helper.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.isg.invest.helper.domain.model.CandlesImportTask;
import ru.isg.invest.helper.domain.model.CandlesImportTaskStatuses;
import ru.isg.invest.helper.domain.model.TimeFrames;

import java.util.List;
import java.util.UUID;

/**
 * Created by s.ivanov on 25.05.2022.
 */
public interface CandlesImportTaskRepository extends JpaRepository<CandlesImportTask, UUID> {

    List<CandlesImportTask> findTasksByStatusOrderByCreatedDate(CandlesImportTaskStatuses status);

    @Query("select t from CandlesImportTask t where t.instrument.id = :instrumentId and t.timeFrame = :timeFrame "
            + " and t.status in :statuses")
    List<CandlesImportTask> findTasksByInstrumentAndTimeFrame(UUID instrumentId, TimeFrames timeFrame,
            List<CandlesImportTaskStatuses> statuses);

    List<CandlesImportTask> findCandlesImportTasksByInstrumentId(UUID instrumentId);
}
