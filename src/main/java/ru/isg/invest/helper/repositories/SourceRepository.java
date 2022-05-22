package ru.isg.invest.helper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.model.Source;

import java.util.UUID;

/**
 * Created by s.ivanov on 06.01.2022.
 */
public interface SourceRepository extends JpaRepository<Source, UUID> {

}
