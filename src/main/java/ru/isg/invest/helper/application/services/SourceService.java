package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.application.dtos.SourceDto;
import ru.isg.invest.helper.application.dtos.SourceRequest;
import ru.isg.invest.helper.domain.model.Author;
import ru.isg.invest.helper.domain.model.Source;
import ru.isg.invest.helper.infrastructure.repositories.AuthorRepository;
import ru.isg.invest.helper.infrastructure.repositories.SourceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SourceService {

    private final SourceRepository sourceRepository;
    private final AuthorService authorService;
    private final AuthorRepository authorRepository;

    public SourceDto sourceToDto(Source source) {
        return new SourceDto()
                .setName(source.getName())
                .setAddress(source.getAddress())
                .setType(source.getType())
                .setId(source.getId());
    }

    public List<SourceDto> listSources() {
        return sourceRepository.findAll().stream()
                .map(this::sourceToDto)
                .collect(Collectors.toList());
    }

    public SourceDto createSource(SourceRequest sourceRequest) {

        Source source = new Source()
                .setType(sourceRequest.getType())
                .setName(sourceRequest.getName())
                .setAddress(sourceRequest.getAddress());

        if (sourceRequest.getAuthorNames() != null) {
            source.setAuthors(sourceRequest.getAuthorNames().stream()
                    .map(name ->
                            authorService.getAuthorByName(name).orElseGet(() ->
                                    authorRepository.save(new Author(name))))
                    .collect(Collectors.toList()));
        }

        return sourceToDto(sourceRepository.save(source));
    }

    public void deleteSource(UUID sourceId) {
        getSourceOpt(sourceId).ifPresent(sourceRepository::delete);
    }

    public Optional<Source> getSourceOpt(UUID sourceId) {
        return sourceRepository.findById(sourceId);
    }

    public Source getSource(UUID sourceId) {
        return sourceRepository.findById(sourceId).orElseThrow();
    }
}
