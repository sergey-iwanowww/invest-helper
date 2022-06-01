package ru.isg.invest.helper.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.dto.IdeaDto;
import ru.isg.invest.helper.dto.IdeaRequest;
import ru.isg.invest.helper.dto.IdeaTriggerData;
import ru.isg.invest.helper.model.Author;
import ru.isg.invest.helper.model.Candle;
import ru.isg.invest.helper.model.IdeaStatuses;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.model.Source;
import ru.isg.invest.helper.repositories.CandleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.isg.invest.helper.model.IdeaConceptTypes.FALL;
import static ru.isg.invest.helper.model.IdeaConceptTypes.RISE;
import static ru.isg.invest.helper.model.IdeaStatuses.ACTIVE;
import static ru.isg.invest.helper.model.IdeaStatuses.FINISHED;
import static ru.isg.invest.helper.model.IdeaTriggerStatuses.ACTIVATED;
import static ru.isg.invest.helper.model.IdeaTriggerStatuses.NEW;
import static ru.isg.invest.helper.model.IdeaTriggerStatuses.WAITING_FOR_ACTIVATION;
import static ru.isg.invest.helper.model.TimeFrames.ONE_HOUR;

/**
 * Created by s.ivanov on 30.05.2022.
 */
@SpringBootTest
public class IdeasCheckerTest {

    @Autowired
    private CandleRepository candleRepository;
    @Autowired
    private IdeasService ideasService;
    @Autowired
    private TestHelper testHelper;
    @Autowired
    private IdeasChecker ideasChecker;

    private IdeaDto createIdea(Instrument instrument, LocalDateTime beginOfDay, IdeaTriggerData startTriggerData,
            IdeaTriggerData finishTriggerData) {

        Author author = testHelper.getRandomAuthor();

        Source source = testHelper.getRandomSource();

        return ideasService.createIdea(new IdeaRequest()
                .setAuthorId(author.getId())
                .setSourceId(source.getId())
                .setConceptType(RISE)
                .setStartTrigger(startTriggerData)
                .setFinishTrigger(finishTriggerData)
                .setGeneratedDate(beginOfDay.withHour(1).withMinute(0))
                .setInstrumentId(instrument.getId())
                .setText("тест текст"));
    }

    private void generateCandles(Instrument instrument, LocalDateTime beginOfDay) {

        // генерируются свечи, начиная с 7:00 по 15:00
        int openPrice = 200;
        for (int i = 7; i <= 15; i++) {

            int closePrice = openPrice + 2;

            candleRepository.save(new Candle(
                    instrument,
                    ONE_HOUR,
                    beginOfDay.withHour(i).withMinute(0),
                    BigDecimal.valueOf(openPrice - 1),
                    BigDecimal.valueOf(closePrice + 1),
                    BigDecimal.valueOf(openPrice),
                    BigDecimal.valueOf(closePrice),
                    100000,
                    true));

            openPrice = closePrice;
        }
    }

    @Test
    @Transactional
    public void testStartDateIdeaTrigger() {

        Instrument instrument = testHelper.getRandomInstrument();

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.DAYS);

        generateCandles(instrument, yesterday);

        // TODO: добавить валидацию при задании триггеров, финишный триггер не должен быть раньше стартового
        // стартовый триггер выставлен, но он не сработает
        // финишный триггер выставлен на 10:30

        IdeaDto idea0 = createIdea(instrument, yesterday,
                new IdeaTriggerData(yesterday.plusDays(2), ONE_HOUR),
                new IdeaTriggerData(yesterday.withHour(10).withMinute(30), ONE_HOUR));

        // стартовый триггер выставлен на 6:30
        // во время срабатывания стартового триггера цена активации неизвестна

        IdeaDto idea1 = createIdea(instrument, yesterday,
                new IdeaTriggerData(yesterday.withHour(6).withMinute(30), ONE_HOUR),
                null);

        // стартовый триггер выставлен на 10:30
        // во время срабатывания стартового триггера цена активации == цене открытия свечи с временем 10:00 и == 206

        IdeaDto idea2 = createIdea(instrument, yesterday,
                new IdeaTriggerData(yesterday.withHour(10).withMinute(30), ONE_HOUR),
                null);

        // стартовый триггер выставлен на 21:00
        // во время срабатывания стартового триггера цена активации == цене закрытия свечи с временем 19:00 и == 220

        IdeaDto idea3 = createIdea(instrument, yesterday,
                new IdeaTriggerData(yesterday.withHour(21).withMinute(0), ONE_HOUR),
                null);

        ideasChecker.check();


        IdeaDto idea0AfterChecking = ideasService.getIdea(idea0.getId());

        assertEquals(IdeaStatuses.WAITING_FOR_ACTIVATION, idea0AfterChecking.getStatus());
        assertNull(idea0AfterChecking.getActivatedDate());
        assertNull(idea0AfterChecking.getActivatedPrice());

        assertEquals(WAITING_FOR_ACTIVATION, idea0AfterChecking.getStartTrigger().getStatus());


        IdeaDto idea1AfterChecking = ideasService.getIdea(idea1.getId());

        assertEquals(ACTIVE, idea1AfterChecking.getStatus());
        assertNotNull(idea1AfterChecking.getActivatedDate());
        assertNull(idea1AfterChecking.getActivatedPrice());

        assertEquals(ACTIVATED, idea1AfterChecking.getStartTrigger().getStatus());


        IdeaDto idea2AfterChecking = ideasService.getIdea(idea2.getId());

        assertEquals(ACTIVE, idea2AfterChecking.getStatus());
        assertNotNull(idea2AfterChecking.getActivatedDate());
        assertTrue(BigDecimal.valueOf(206).compareTo(idea2AfterChecking.getActivatedPrice()) == 0);

        assertEquals(ACTIVATED, idea2AfterChecking.getStartTrigger().getStatus());


        IdeaDto idea3AfterChecking = ideasService.getIdea(idea3.getId());

        assertEquals(ACTIVE, idea3AfterChecking.getStatus());
        assertNotNull(idea3AfterChecking.getActivatedDate());
        assertTrue(BigDecimal.valueOf(218).compareTo(idea3AfterChecking.getActivatedPrice()) == 0);

        assertEquals(ACTIVATED, idea3AfterChecking.getStartTrigger().getStatus());
    }

    @Test
    @Transactional
    public void testFinishDateIdeaTrigger() {

        Instrument instrument = testHelper.getRandomInstrument();

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).truncatedTo(MINUTES);

        generateCandles(instrument, yesterday);

        // TODO: добавить валидацию при задании триггеров, финишный триггер не должен быть раньше стартового
        // стартовый триггер выставлен, но он не сработает
        // финишный триггер выставлен на 10:30

        IdeaDto idea0 = createIdea(instrument, yesterday,
                new IdeaTriggerData(yesterday.plusDays(2), ONE_HOUR),
                new IdeaTriggerData(yesterday.withHour(10).withMinute(30), ONE_HOUR));

        // стартовый триггер выставлен на 3:00
        // финишный триггер выставлен, но не сработает

        IdeaDto idea1 = createIdea(instrument, yesterday,
                new IdeaTriggerData(yesterday.withHour(3).withMinute(0), ONE_HOUR),
                new IdeaTriggerData(yesterday.plusDays(2), ONE_HOUR));

        // стартовый триггер выставлен на 3:00
        // финишный триггер выставлен на 6:30
        // во время срабатывания финишного триггера цена активации неизвестна

        IdeaDto idea2 = createIdea(instrument, yesterday,
                new IdeaTriggerData(yesterday.withHour(3).withMinute(0), ONE_HOUR),
                new IdeaTriggerData(yesterday.withHour(6).withMinute(30), ONE_HOUR));

        // стартовый триггер выставлен на 9:30
        // финишный триггер выставлен на 10:30
        // во время срабатывания финишного триггера цена активации == цене открытия свечи с временем 10:00 и == 206

        IdeaDto idea3 = createIdea(instrument, yesterday,
                new IdeaTriggerData(yesterday.withHour(9).withMinute(30), ONE_HOUR),
                new IdeaTriggerData(yesterday.withHour(10).withMinute(30), ONE_HOUR));

        // стартовый триггер выставлен на 9:30
        // финишный триггер выставлен на 21:00
        // во время срабатывания финишного триггера цена активации == цене закрытия свечи с временем 19:00 и == 220

        IdeaDto idea4 = createIdea(instrument, yesterday,
                new IdeaTriggerData(yesterday.withHour(9).withMinute(30), ONE_HOUR),
                new IdeaTriggerData(yesterday.withHour(21), ONE_HOUR));

        ideasChecker.check();

        IdeaDto idea0AfterChecking = ideasService.getIdea(idea0.getId());

        assertEquals(IdeaStatuses.WAITING_FOR_ACTIVATION, idea0AfterChecking.getStatus());
        assertNull(idea0AfterChecking.getFinishedDate());
        assertNull(idea0AfterChecking.getFinishedPrice());

        assertEquals(NEW, idea0AfterChecking.getFinishTrigger().getStatus());


        IdeaDto idea1AfterChecking = ideasService.getIdea(idea1.getId());

        assertEquals(ACTIVE, idea1AfterChecking.getStatus());
        assertNull(idea1AfterChecking.getFinishedDate());

        assertEquals(WAITING_FOR_ACTIVATION, idea1AfterChecking.getFinishTrigger().getStatus());


        IdeaDto idea2AfterChecking = ideasService.getIdea(idea2.getId());

        assertEquals(FINISHED, idea2AfterChecking.getStatus());
        assertNotNull(idea2AfterChecking.getFinishedDate());
        assertNull(idea2AfterChecking.getFinishedPrice());

        assertEquals(ACTIVATED, idea2AfterChecking.getFinishTrigger().getStatus());


        IdeaDto idea3AfterChecking = ideasService.getIdea(idea3.getId());

        assertEquals(FINISHED, idea3AfterChecking.getStatus());
        assertNotNull(idea3AfterChecking.getFinishedDate());
        assertTrue(BigDecimal.valueOf(206).compareTo(idea3AfterChecking.getFinishedPrice()) == 0);

        assertEquals(ACTIVATED, idea3AfterChecking.getFinishTrigger().getStatus());


        IdeaDto idea4AfterChecking = ideasService.getIdea(idea4.getId());

        assertEquals(FINISHED, idea4AfterChecking.getStatus());
        assertNotNull(idea4AfterChecking.getFinishedDate());
        assertTrue(BigDecimal.valueOf(218).compareTo(idea4AfterChecking.getFinishedPrice()) == 0);

        assertEquals(ACTIVATED, idea4AfterChecking.getFinishTrigger().getStatus());
    }

    @Test
    @Transactional
    public void testStartRisePriceIdeaTrigger() {

        Instrument instrument = testHelper.getRandomInstrument();

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).truncatedTo(MINUTES);

        generateCandles(instrument, yesterday);

        // стартовый триггер выставлен, но он не сработает
        // финишный триггер выставлен на 200

        IdeaDto idea0 = createIdea(instrument, yesterday,
                new IdeaTriggerData(BigDecimal.valueOf(300), ONE_HOUR),
                new IdeaTriggerData(BigDecimal.valueOf(200), ONE_HOUR));

        // стартовый триггер выставлен на 100
        // дата активации == дате закрытия первой известной закрытой свечи с ценой > 200 и == 7:59:59
        // цена активации == дате закрытия первой известной свечи с ценой > 200 и == 202

        IdeaDto idea1 = createIdea(instrument, yesterday,
                new IdeaTriggerData(BigDecimal.valueOf(100), ONE_HOUR),
                null);

        // стартовый триггер выставлен на 206
        // дата срабатываная == дате свечи в 9:00 (по цене закрытия)
        // цена активации == 206

        IdeaDto idea2 = createIdea(instrument, yesterday,
                new IdeaTriggerData(BigDecimal.valueOf(206), ONE_HOUR),
                null);

        ideasChecker.check();


        IdeaDto idea0AfterChecking = ideasService.getIdea(idea0.getId());

        assertEquals(IdeaStatuses.WAITING_FOR_ACTIVATION, idea0AfterChecking.getStatus());
        assertNull(idea0AfterChecking.getActivatedDate());
        assertNull(idea0AfterChecking.getActivatedPrice());

        assertEquals(WAITING_FOR_ACTIVATION, idea0AfterChecking.getStartTrigger().getStatus());


        IdeaDto idea1AfterChecking = ideasService.getIdea(idea1.getId());

        assertEquals(ACTIVE, idea1AfterChecking.getStatus());
        assertEquals(7, idea1AfterChecking.getActivatedDate().getHour());
        assertEquals(59, idea1AfterChecking.getActivatedDate().getMinute());
        assertTrue(BigDecimal.valueOf(202).compareTo(idea1AfterChecking.getActivatedPrice()) == 0);

        assertEquals(ACTIVATED, idea1AfterChecking.getStartTrigger().getStatus());


        IdeaDto idea2AfterChecking = ideasService.getIdea(idea2.getId());

        assertEquals(ACTIVE, idea2AfterChecking.getStatus());
        assertEquals(9, idea2AfterChecking.getActivatedDate().getHour());
        assertEquals(59, idea2AfterChecking.getActivatedDate().getMinute());
        assertTrue(BigDecimal.valueOf(206).compareTo(idea2AfterChecking.getActivatedPrice()) == 0);

        assertEquals(ACTIVATED, idea2AfterChecking.getStartTrigger().getStatus());
    }
}
