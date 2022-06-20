package ru.isg.invest.helper.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.model.Operation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by s.ivanov on 13.06.2022.
 */
public interface OperationRepository extends JpaRepository<Operation, UUID> {

    Optional<Operation> findByPortfolioIdAndExternalId(UUID portfolioId, String externalId);

    List<Operation> findByPortfolioId(UUID portfolioId);

    Page<Operation> findByPortfolioId(UUID portfolioId, Pageable pageable);

    List<Operation> findByPortfolioIdAndInstrumentIdOrderByDate(UUID portfolioId, UUID instrumentId);
}
