package ru.isg.invest.helper.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.domain.model.IdeaTrigger;

import java.util.UUID;

/**
 * Created by s.ivanov on 29.05.2022.
 */
public interface IdeaTriggerRepository extends JpaRepository<IdeaTrigger, UUID> {

}
