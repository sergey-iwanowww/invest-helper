package ru.isg.invest.helper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.model.Operation;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by s.ivanov on 13.06.2022.
 */
public interface OperationRepository extends JpaRepository<Operation, UUID> {

    Optional<Operation> findByExternalId(String externalId);
}
