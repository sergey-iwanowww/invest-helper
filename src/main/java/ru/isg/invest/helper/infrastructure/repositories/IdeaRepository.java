package ru.isg.invest.helper.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.domain.model.Idea;
import ru.isg.invest.helper.domain.model.IdeaStatuses;

import java.util.List;
import java.util.UUID;

/**
 * Created by s.ivanov on 06.01.2022.
 */
public interface IdeaRepository extends JpaRepository<Idea, UUID> {

    List<Idea> getIdeasByStatusIn(List<IdeaStatuses> statuses);
}
