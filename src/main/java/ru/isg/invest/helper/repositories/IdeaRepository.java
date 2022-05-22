package ru.isg.invest.helper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.model.Idea;

import java.util.UUID;

/**
 * Created by s.ivanov on 06.01.2022.
 */
public interface IdeaRepository extends JpaRepository<Idea, UUID> {

}
