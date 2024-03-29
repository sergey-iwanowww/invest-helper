package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.domain.model.Author;
import ru.isg.invest.helper.domain.model.Instrument;
import ru.isg.invest.helper.domain.model.Source;
import ru.isg.invest.helper.domain.model.Tag;
import ru.isg.invest.helper.infrastructure.repositories.AuthorRepository;
import ru.isg.invest.helper.infrastructure.repositories.InstrumentRepository;
import ru.isg.invest.helper.infrastructure.repositories.SourceRepository;
import ru.isg.invest.helper.infrastructure.repositories.TagRepository;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static ru.isg.invest.helper.domain.model.Currencies.RUB;
import static ru.isg.invest.helper.domain.model.InstrumentTypes.STOCK;
import static ru.isg.invest.helper.domain.model.SourceTypes.TELEGRAM;

/**
 * Created by s.ivanov on 31.05.2022.
 */
@Service
@RequiredArgsConstructor
public class TestHelper {

    private final AuthorRepository authorRepository;
    private final SourceRepository sourceRepository;
    private final TagRepository tagRepository;
    private final InstrumentRepository instrumentRepository;

    @PostConstruct
    public void initDb() {

        if (dbInitializedAlready()) {
            return;
        }

        createAuthors();
        createSources();
        createTags();
        createInstruments();
    }

    private List<Author> createAuthors() {
        return IntStream.of(1, 2, 3, 4)
                .mapToObj(i -> createAuthor("автор " + i))
                .collect(Collectors.toList());
    }

    private Author createAuthor(String name) {
        return authorRepository.save(new Author(name));
    }

    public Author getRandomAuthor() {
        List<Author> authors = authorRepository.findAll();
        Collections.shuffle(authors);
        return authors.size() > 0 ? authors.get(0) : null;
    }

    private List<Source> createSources() {
        return IntStream.of(1, 2, 3, 4)
                .mapToObj(i -> createSource("источник " + i, List.of(getRandomAuthor())))
                .collect(Collectors.toList());
    }

    private Source createSource(String name, List<Author> authors) {
        return sourceRepository.save(new Source()
                .setAddress("тест адрес")
                .setAuthors(authors)
                .setName(name)
                .setType(TELEGRAM));
    }

    public Source getRandomSource() {
        List<Source> sources = sourceRepository.findAll();
        Collections.shuffle(sources);
        return sources.size() > 0 ? sources.get(0) : null;
    }

    private List<Tag> createTags() {
        return IntStream.of(1, 2, 3, 4)
                .mapToObj(i -> createTag("тэг " + i))
                .collect(Collectors.toList());
    }

    private Tag createTag(String name) {
        return tagRepository.save(new Tag()
                .setName(name));
    }

    public Tag getRandomTag() {
        List<Tag> tags = getRandomTags(1);
        return tags.size() > 0 ? tags.get(0) : null;
    }

    public List<Tag> getRandomTags(int size) {
        List<Tag> tags = tagRepository.findAll();
        Collections.shuffle(tags);
        return tags.subList(0, size);
    }

    private boolean dbInitializedAlready() {
        return getRandomAuthor() != null && getRandomAuthor() != null && getRandomTag() != null
                && getRandomInstrument() != null;
    }

    private List<Instrument> createInstruments() {
        return IntStream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .mapToObj(i -> createInstrument("тест акция " + i, "figi" + i, "TCKR" + i, "ex" + i))
                .collect(Collectors.toList());
    }

    private Instrument createInstrument(String name, String figi, String ticker, String exchange) {
        return instrumentRepository.save(new Instrument(STOCK, ticker, name, RUB, figi, exchange));
    }

    public Instrument getRandomInstrument() {
        List<Instrument> instruments = getRandomInstruments(1);
        return instruments.size() > 0 ? instruments.get(0) : null;
    }

    public List<Instrument> getRandomInstruments(int size) {
        List<Instrument> instruments = instrumentRepository.findAll();
        Collections.shuffle(instruments);
        return instruments.size() >= size ? instruments.subList(0, size) : newArrayList();
    }
}
