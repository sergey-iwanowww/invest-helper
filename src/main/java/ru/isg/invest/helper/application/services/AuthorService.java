package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.application.dtos.AuthorDto;
import ru.isg.invest.helper.domain.model.Author;
import ru.isg.invest.helper.infrastructure.repositories.AuthorRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorDto authorToDto(Author author) {
        return new AuthorDto()
                .setId(author.getId())
                .setName(author.getName());
    }

    public Optional<Author> getAuthorByName(String name) {
        return authorRepository.getAuthorByName(name);
    }

    public Author getAuthor(UUID authorId) {
        return authorRepository.findById(authorId).orElseThrow();
    }
}
