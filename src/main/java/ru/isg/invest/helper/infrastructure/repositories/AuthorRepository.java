package ru.isg.invest.helper.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.domain.model.Author;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by s.ivanov on 06.01.2022.
 */
public interface AuthorRepository extends JpaRepository<Author, UUID> {

    Optional<Author> getAuthorByName(String name);
}
