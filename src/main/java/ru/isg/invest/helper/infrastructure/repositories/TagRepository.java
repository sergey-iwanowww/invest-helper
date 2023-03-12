package ru.isg.invest.helper.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.domain.model.Tag;

import java.util.UUID;

/**
 * Created by s.ivanov on 11.01.2022.
 */
public interface TagRepository extends JpaRepository<Tag, UUID> {

}
