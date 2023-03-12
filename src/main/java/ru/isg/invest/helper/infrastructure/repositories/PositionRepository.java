package ru.isg.invest.helper.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.domain.model.Position;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by s.ivanov on 20.06.2022.
 */
public interface PositionRepository extends JpaRepository<Position, UUID> {

    Optional<Position> findByPortfolioIdAndInstrumentId(UUID portfolioId, UUID instrumentId);
}
