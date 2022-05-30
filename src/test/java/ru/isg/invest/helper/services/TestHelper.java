package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.model.Author;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.model.Source;
import ru.isg.invest.helper.model.Tag;
import ru.isg.invest.helper.repositories.AuthorRepository;
import ru.isg.invest.helper.repositories.InstrumentRepository;
import ru.isg.invest.helper.repositories.SourceRepository;
import ru.isg.invest.helper.repositories.TagRepository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.isg.invest.helper.model.InstrumentTypes.STOCK;
import static ru.isg.invest.helper.model.Markets.MOEX;
import static ru.isg.invest.helper.model.Sectors.OTHER;
import static ru.isg.invest.helper.model.SourceTypes.TELEGRAM;
import static ru.isg.invest.helper.model.TradingModes.T0;

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
        return authorRepository.findAll(PageRequest.of((int) (Math.random() * 4), 1)).stream()
                .findAny()
                .orElse(null);
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
        return sourceRepository.findAll(PageRequest.of((int) (Math.random() * 4), 1)).stream()
                .findAny()
                .orElse(null);
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
        return tagRepository.findAll(PageRequest.of((int) (Math.random() * 4), 1)).stream()
                .findAny()
                .orElse(null);
    }

    public List<Tag> getRandomTags(int size) {
        return tagRepository.findAll(PageRequest.of((int) (Math.random() * size), size)).getContent();
    }

    private boolean dbInitializedAlready() {
        return getRandomAuthor() != null && getRandomAuthor() != null && getRandomTag() != null
                && getRandomInstrument() != null;
    }

    private List<Instrument> createInstruments() {
        return IntStream.of(1, 2, 3, 4)
                .mapToObj(i -> createInstrument("тест акция " + i, "figi" + i, "TCKR" + i))
                .collect(Collectors.toList());
    }

    private Instrument createInstrument(String name, String figi, String ticker) {
        return instrumentRepository.save(new Instrument()
                .setName(name)
                .setType(STOCK)
                .setCurrencyCode("RUB")
                .setFigi(figi)
                .setMarket(MOEX)
                .setSector(OTHER)
                .setTicker(ticker)
                .setTradingMode(T0));
    }

    public Instrument getRandomInstrument() {
        return instrumentRepository.findAll(PageRequest.of((int) (Math.random() * 4), 1)).stream()
                .findAny()
                .orElse(null);
    }
}
