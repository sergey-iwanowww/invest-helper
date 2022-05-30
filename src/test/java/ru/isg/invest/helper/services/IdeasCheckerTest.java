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
import static ru.isg.invest.helper.model.IdeaStatuses.ACTIVE;
import static ru.isg.invest.helper.model.IdeaTriggerStatuses.ACTIVATED;
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

    @Test
    @Transactional
    public void testStartDateIdeaTrigger() {

        Instrument instrument = testHelper.getRandomInstrument();

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).truncatedTo(MINUTES);

        int openPrice = 200;
        for (int i = 7; i <= 15; i++) {

            int closePrice = openPrice + 2;

            candleRepository.save(new Candle(
                    instrument,
                    ONE_HOUR,
                    yesterday.withHour(i).withMinute(0),
                    BigDecimal.valueOf(openPrice - 1),
                    BigDecimal.valueOf(closePrice + 1),
                    BigDecimal.valueOf(openPrice),
                    BigDecimal.valueOf(closePrice),
                    100000,
                    true));

            openPrice = closePrice;
        }

        Author author = testHelper.getRandomAuthor();

        Source source = testHelper.getRandomSource();

        // идея 1 сгенерирована в 6:00
        // триггер выставлен на 6:30
        // во время срабатывания триггера цена активации неизвестна

        IdeaDto idea1 = ideasService.createIdea(new IdeaRequest()
                .setAuthorId(author.getId())
                .setSourceId(source.getId())
                .setConceptType(FALL)
                .setStartTrigger(new IdeaTriggerData()
                        .setDate(yesterday.withHour(6).withMinute(30))
                        .setMonitoringTimeFrame(ONE_HOUR))
                .setFinishTrigger(new IdeaTriggerData()
                        .setDate(yesterday.plusDays(10))
                        .setMonitoringTimeFrame(ONE_HOUR))
                .setGeneratedDate(yesterday.withHour(6).withMinute(0))
                .setInstrumentId(instrument.getId())
                .setText("тест текст"));

        // идея 2 сгенерирована в 8:00
        // триггер выставлен на 10:30
        // во время срабатывания триггера цена активации == цене открытия свечи с временем 10:00 и == 206

        IdeaDto idea2 = ideasService.createIdea(new IdeaRequest()
                .setAuthorId(author.getId())
                .setSourceId(source.getId())
                .setConceptType(FALL)
                .setStartTrigger(new IdeaTriggerData()
                        .setDate(yesterday.withHour(10).withMinute(30))
                        .setMonitoringTimeFrame(ONE_HOUR))
                .setFinishTrigger(new IdeaTriggerData()
                        .setDate(yesterday.plusDays(10))
                        .setMonitoringTimeFrame(ONE_HOUR))
                .setGeneratedDate(yesterday.withHour(8).withMinute(0))
                .setInstrumentId(instrument.getId())
                .setText("тест текст"));

        // идея 3 сгенерирована в 8:00
        // триггер выставлен на 21:00
        // во время срабатывания триггера цена активации == цене закрытия свечи с временем 19:00 и == 220

        IdeaDto idea3 = ideasService.createIdea(new IdeaRequest()
                .setAuthorId(author.getId())
                .setSourceId(source.getId())
                .setConceptType(FALL)
                .setStartTrigger(new IdeaTriggerData()
                        .setDate(yesterday.withHour(21).withMinute(0))
                        .setMonitoringTimeFrame(ONE_HOUR))
                .setFinishTrigger(new IdeaTriggerData()
                        .setDate(yesterday.plusDays(10))
                        .setMonitoringTimeFrame(ONE_HOUR))
                .setGeneratedDate(yesterday.withHour(8).withMinute(0))
                .setInstrumentId(instrument.getId())
                .setText("тест текст"));

        ideasChecker.check();

        IdeaDto idea1AfterChecking = ideasService.getIdea(idea1.getId());

        assertEquals(ACTIVE, idea1AfterChecking.getStatus());
        assertNotNull(idea1AfterChecking.getActivatedDate());
        assertNull(idea1AfterChecking.getActivatedPrice());

        assertEquals(ACTIVATED, idea1AfterChecking.getStartTrigger().getStatus());
        assertEquals(WAITING_FOR_ACTIVATION, idea1AfterChecking.getFinishTrigger().getStatus());

        assertNull(idea1AfterChecking.getFinishedDate());
        assertNull(idea1AfterChecking.getFinishedPrice());


        IdeaDto idea2AfterChecking = ideasService.getIdea(idea2.getId());

        assertEquals(ACTIVE, idea2AfterChecking.getStatus());
        assertNotNull(idea2AfterChecking.getActivatedDate());
        assertTrue(BigDecimal.valueOf(206).compareTo(idea2AfterChecking.getActivatedPrice()) == 0);

        assertEquals(ACTIVATED, idea2AfterChecking.getStartTrigger().getStatus());
        assertEquals(WAITING_FOR_ACTIVATION, idea2AfterChecking.getFinishTrigger().getStatus());

        assertNull(idea2AfterChecking.getFinishedDate());
        assertNull(idea2AfterChecking.getFinishedPrice());


        IdeaDto idea3AfterChecking = ideasService.getIdea(idea3.getId());

        assertEquals(ACTIVE, idea3AfterChecking.getStatus());
        assertNotNull(idea3AfterChecking.getActivatedDate());
        assertTrue(BigDecimal.valueOf(218).compareTo(idea3AfterChecking.getActivatedPrice()) == 0);

        assertEquals(ACTIVATED, idea3AfterChecking.getStartTrigger().getStatus());
        assertEquals(WAITING_FOR_ACTIVATION, idea3AfterChecking.getFinishTrigger().getStatus());

        assertNull(idea3AfterChecking.getFinishedDate());
        assertNull(idea3AfterChecking.getFinishedPrice());

    }
}
