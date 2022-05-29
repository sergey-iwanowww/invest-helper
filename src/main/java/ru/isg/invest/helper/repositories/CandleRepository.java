package ru.isg.invest.helper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.isg.invest.helper.model.Candle;
import ru.isg.invest.helper.model.TimeFrames;

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
    Optional<Candle> getCandleByInstrumentTimeFrameDate(UUID instrumentId, TimeFrames timeFrame, LocalDateTime openDate);

    /**
     * Отбирает свечи по инструменту, таймфрейму, если дата открытия не позже указанной даты.
      */
    @Query("select c from Candle c where c.instrument.id = :instrumentId and c.timeFrame = :timeFrame "
            + " and c.openDate <= :openDateTo")
    List<Candle> getCandles1(UUID instrumentId, TimeFrames timeFrame, LocalDateTime openDateTo);

    /**
     * Отбирает свечи по инструменту, таймфрейму, если свеча завершена и дата закрытия свечи не ранее указанной даты.
     */
    @Query("select c from Candle c where c.instrument.id = :instrumentId and c.timeFrame = :timeFrame "
            + " and c.closeDate >= :closeDateFrom and c.complete = true")
    List<Candle> getCandles2(UUID instrumentId, TimeFrames timeFrame, LocalDateTime closeDateFrom);
}
