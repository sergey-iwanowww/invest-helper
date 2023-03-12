package ru.isg.invest.helper.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.domain.model.Source;

import java.util.UUID;

/**
 * Created by s.ivanov on 06.01.2022.
 */
public interface SourceRepository extends JpaRepository<Source, UUID> {

}
