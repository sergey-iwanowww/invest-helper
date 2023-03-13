package ru.isg.invest.helper.infrastructure.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.isg.invest.helper.domain.model.Candle;
import ru.isg.invest.helper.domain.model.TimeFrames;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by s.ivanov on 25.05.2022.
 */
public interface CandleRepository extends JpaRepository<Candle, UUID> {

    @Query("select c from Candle c where c.instrument.id = :instrumentId and c.timeFrame = :timeFrame "
            + " and c.openDate = :openDate")
    Optional<Candle> getCandleByInstrumentTimeFrameDate(UUID instrumentId, TimeFrames timeFrame,
            LocalDateTime openDate);

    /**
     * Отбирает свечи по инструменту, таймфрейму, если дата открытия не позже указанной даты.
     */
    @Query("select c from Candle c where c.instrument.id = :instrumentId and c.timeFrame = :timeFrame "
            + " and c.openDate <= :openDateTo")
    List<Candle> getCandlesByOpenDate(UUID instrumentId, TimeFrames timeFrame, LocalDateTime openDateTo);

    /**
     * Отбирает свечи по инструменту, таймфрейму, если свеча завершена и дата закрытия свечи не ранее указанной даты От
     * и ранее даты До.
     */
    @Query("select c from Candle c where c.instrument.id = :instrumentId and c.timeFrame = :timeFrame "
            + " and c.closeDate >= :closeDateFrom and c.closeDate < :closeDateTo and c.complete = true")
    List<Candle> getCandlesByCloseDate(UUID instrumentId, TimeFrames timeFrame, LocalDateTime closeDateFrom,
            LocalDateTime closeDateTo);

    Page<Candle> getCandlesByInstrumentIdAndTimeFrameOrderByOpenDateDesc(UUID instrumentId, TimeFrames timeFrame,
            Pageable pageable);
}
