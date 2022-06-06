package ru.isg.invest.helper.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.dto.IdeaDto;
import ru.isg.invest.helper.dto.IdeaRequest;
import ru.isg.invest.helper.dto.IdeaTriggerData;
import ru.isg.invest.helper.model.Author;
import ru.isg.invest.helper.model.DateIdeaTrigger;
import ru.isg.invest.helper.model.IdeaConceptTypes;
import ru.isg.invest.helper.model.IdeaStatuses;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.model.PriceIdeaTrigger;
import ru.isg.invest.helper.model.Source;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
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
    private IdeasService ideasService;
    @Autowired
    private TestHelper testHelper;
    @MockBean
    private TriggersChecker triggersChecker;
    @Autowired
    private IdeasChecker ideasChecker;

    private IdeaDto createIdea(Instrument instrument, LocalDateTime beginOfDay, IdeaConceptTypes conceptType,
            IdeaTriggerData startTriggerData, IdeaTriggerData finishTriggerData) {

        Author author = testHelper.getRandomAuthor();

        Source source = testHelper.getRandomSource();

        return ideasService.createIdea(new IdeaRequest()
                .setAuthorId(author.getId())
                .setSourceId(source.getId())
                .setConceptType(conceptType)
                .setStartTrigger(startTriggerData)
                .setFinishTrigger(finishTriggerData)
                .setGeneratedDate(beginOfDay.withHour(1).withMinute(0))
                .setInstrumentId(instrument.getId())
                .setText("тест текст"));
    }

    @Test
    @Transactional
    public void testDateIdeasProcessing() {

        List<Instrument> instruments = testHelper.getRandomInstruments(3);

        Instrument startNotActivatedFinishNotActivatedInstrument = instruments.get(0);
        Instrument startActivatedFinishNotActivatedInstrument = instruments.get(1);
        Instrument startActivatedFinishActivatedInstrument = instruments.get(2);

        when(triggersChecker.checkActivationNeeded(
                argThat((DateIdeaTrigger x) -> {
                    if (x == null) {
                        return true;
                    }

                    if (x.getIdea().getInstrument().getId().equals(startNotActivatedFinishNotActivatedInstrument.getId())) {
                        return true;
                    } else if (x.getIdea().getInstrument().getId().equals(startActivatedFinishNotActivatedInstrument.getId())) {
                        if (x == x.getIdea().getFinishTrigger()) {
                            return true;
                        }
                    }

                    return false;
                })))
                .thenReturn(new TriggerActivationNeededResults(false, null, null));

        LocalDateTime curDate = LocalDateTime.now();
        LocalDateTime yesterdayBegOfDay = curDate.minusDays(1).truncatedTo(ChronoUnit.DAYS);

        LocalDateTime triggerActivationDate = yesterdayBegOfDay.withHour(10);
        BigDecimal triggerActivationPrice = TEN;

        when(triggersChecker.checkActivationNeeded(
                argThat((DateIdeaTrigger x) -> {
                    if (x == null) {
                        return false;
                    }

                    if (x.getIdea().getInstrument().getId().equals(startActivatedFinishNotActivatedInstrument.getId())) {
                        if (x == x.getIdea().getStartTrigger()) {
                            return true;
                        }
                    } else if (x.getIdea().getInstrument().getId().equals(startActivatedFinishActivatedInstrument.getId())) {
                        return true;
                    }

                    return false;
                })))
                .thenReturn(new TriggerActivationNeededResults(true, triggerActivationDate, triggerActivationPrice));

        // идея по дате, стартовый триггер не активируется, финишный не активируется
        IdeaDto idea1 = createIdea(startNotActivatedFinishNotActivatedInstrument, yesterdayBegOfDay, RISE,
                new IdeaTriggerData(curDate, ONE_HOUR),
                new IdeaTriggerData(curDate, ONE_HOUR));

        // идея по дате, стартовый триггер активируется, финишный не активируется
        IdeaDto idea2 = createIdea(startActivatedFinishNotActivatedInstrument, yesterdayBegOfDay, RISE,
                new IdeaTriggerData(curDate, ONE_HOUR),
                new IdeaTriggerData(curDate, ONE_HOUR));

        // идея по дате, стартовый триггер активируется, финишный активируется
        IdeaDto idea3 = createIdea(startActivatedFinishActivatedInstrument, yesterdayBegOfDay, RISE,
                new IdeaTriggerData(curDate, ONE_HOUR),
                new IdeaTriggerData(curDate, ONE_HOUR));

        ideasChecker.check();


        IdeaDto idea1AfterChecking = ideasService.getIdea(idea1.getId());

        assertEquals(IdeaStatuses.WAITING_FOR_ACTIVATION, idea1AfterChecking.getStatus());
        assertNull(idea1AfterChecking.getActivatedDate());
        assertNull(idea1AfterChecking.getActivatedPrice());

        assertEquals(WAITING_FOR_ACTIVATION, idea1AfterChecking.getStartTrigger().getStatus());
        assertNull(idea1AfterChecking.getStartTrigger().getActivatedDate());

        assertEquals(NEW, idea1AfterChecking.getFinishTrigger().getStatus());
        assertNull(idea1AfterChecking.getFinishTrigger().getActivatedDate());


        IdeaDto idea2AfterChecking = ideasService.getIdea(idea2.getId());

        assertEquals(ACTIVE, idea2AfterChecking.getStatus());
        assertEquals(triggerActivationDate, idea2AfterChecking.getActivatedDate());
        assertEquals(triggerActivationPrice, idea2AfterChecking.getActivatedPrice());

        assertEquals(ACTIVATED, idea2AfterChecking.getStartTrigger().getStatus());
        assertTrue(idea2AfterChecking.getStartTrigger().getActivatedDate().compareTo(curDate) >= 0);

        assertEquals(WAITING_FOR_ACTIVATION, idea2AfterChecking.getFinishTrigger().getStatus());
        assertNull(idea2AfterChecking.getFinishTrigger().getActivatedDate());


        IdeaDto idea3AfterChecking = ideasService.getIdea(idea3.getId());

        assertEquals(FINISHED, idea3AfterChecking.getStatus());
        assertEquals(triggerActivationDate, idea3AfterChecking.getActivatedDate());
        assertEquals(triggerActivationPrice, idea3AfterChecking.getActivatedPrice());
        assertEquals(triggerActivationDate, idea3AfterChecking.getFinishedDate());
        assertEquals(triggerActivationPrice, idea3AfterChecking.getFinishedPrice());

        assertEquals(ACTIVATED, idea3AfterChecking.getStartTrigger().getStatus());
        assertTrue(idea3AfterChecking.getStartTrigger().getActivatedDate().compareTo(curDate) >= 0);

        assertEquals(ACTIVATED, idea3AfterChecking.getFinishTrigger().getStatus());
        assertTrue(idea3AfterChecking.getFinishTrigger().getActivatedDate().compareTo(curDate) >= 0);
    }

    @Test
    @Transactional
    public void testPriceIdeasProcessing() {

        List<Instrument> instruments = testHelper.getRandomInstruments(3);

        Instrument startNotActivatedFinishNotActivatedInstrument = instruments.get(0);
        Instrument startActivatedFinishNotActivatedInstrument = instruments.get(1);
        Instrument startActivatedFinishActivatedInstrument = instruments.get(2);

        when(triggersChecker.checkActivationNeeded(
                argThat((PriceIdeaTrigger x) -> {
                    if (x == null) {
                        return true;
                    }

                    if (x.getIdea().getInstrument().getId().equals(startNotActivatedFinishNotActivatedInstrument.getId())) {
                        return true;
                    } else if (x.getIdea().getInstrument().getId().equals(startActivatedFinishNotActivatedInstrument.getId())) {
                        if (x == x.getIdea().getFinishTrigger()) {
                            return true;
                        }
                    }

                    return false;
                })))
                .thenReturn(new TriggerActivationNeededResults(false, null, null));

        LocalDateTime curDate = LocalDateTime.now();
        LocalDateTime yesterdayBegOfDay = curDate.minusDays(1).truncatedTo(ChronoUnit.DAYS);

        LocalDateTime triggerActivationDate = yesterdayBegOfDay.withHour(10);
        BigDecimal triggerActivationPrice = TEN;

        when(triggersChecker.checkActivationNeeded(
                argThat((PriceIdeaTrigger x) -> {
                    if (x == null) {
                        return false;
                    }

                    if (x.getIdea().getInstrument().getId().equals(startActivatedFinishNotActivatedInstrument.getId())) {
                        if (x == x.getIdea().getStartTrigger()) {
                            return true;
                        }
                    } else if (x.getIdea().getInstrument().getId().equals(startActivatedFinishActivatedInstrument.getId())) {
                        return true;
                    }

                    return false;
                })))
                .thenReturn(new TriggerActivationNeededResults(true, triggerActivationDate, triggerActivationPrice));


        // идея по цене, стартовый триггер не активируется, финишный не активируется
        IdeaDto idea1 = createIdea(startNotActivatedFinishNotActivatedInstrument, yesterdayBegOfDay, FALL,
                new IdeaTriggerData(TEN, ONE_HOUR),
                new IdeaTriggerData(TEN, ONE_HOUR));

        // идея по цене, стартовый триггер активируется, финишный не активируется
        IdeaDto idea2 = createIdea(startActivatedFinishNotActivatedInstrument, yesterdayBegOfDay, FALL,
                new IdeaTriggerData(TEN, ONE_HOUR),
                new IdeaTriggerData(TEN, ONE_HOUR));

        // идея по цене, стартовый триггер активируется, финишный активируется
        IdeaDto idea3 = createIdea(startActivatedFinishActivatedInstrument, yesterdayBegOfDay, FALL,
                new IdeaTriggerData(TEN, ONE_HOUR),
                new IdeaTriggerData(TEN, ONE_HOUR));

        ideasChecker.check();


        IdeaDto idea1AfterChecking = ideasService.getIdea(idea1.getId());

        assertEquals(IdeaStatuses.WAITING_FOR_ACTIVATION, idea1AfterChecking.getStatus());
        assertNull(idea1AfterChecking.getActivatedDate());
        assertNull(idea1AfterChecking.getActivatedPrice());

        assertEquals(WAITING_FOR_ACTIVATION, idea1AfterChecking.getStartTrigger().getStatus());
        assertNull(idea1AfterChecking.getStartTrigger().getActivatedDate());

        assertEquals(NEW, idea1AfterChecking.getFinishTrigger().getStatus());
        assertNull(idea1AfterChecking.getFinishTrigger().getActivatedDate());


        IdeaDto idea2AfterChecking = ideasService.getIdea(idea2.getId());

        assertEquals(ACTIVE, idea2AfterChecking.getStatus());
        assertNotNull(idea2AfterChecking.getActivatedDate());
        assertNotNull(idea2AfterChecking.getActivatedPrice());

        assertTrue(idea2AfterChecking.getStartTrigger().getActivatedDate().compareTo(curDate) >= 0);
        assertNotNull(idea2AfterChecking.getStartTrigger().getActivatedDate());

        assertEquals(WAITING_FOR_ACTIVATION, idea2AfterChecking.getFinishTrigger().getStatus());
        assertNull(idea2AfterChecking.getFinishTrigger().getActivatedDate());


        IdeaDto idea3AfterChecking = ideasService.getIdea(idea3.getId());

        assertEquals(FINISHED, idea3AfterChecking.getStatus());
        assertNotNull(idea3AfterChecking.getActivatedDate());
        assertNotNull(idea3AfterChecking.getActivatedPrice());
        assertNotNull(idea3AfterChecking.getFinishedDate());
        assertNotNull(idea3AfterChecking.getFinishedPrice());

        assertEquals(ACTIVATED, idea3AfterChecking.getStartTrigger().getStatus());
        assertTrue(idea3AfterChecking.getStartTrigger().getActivatedDate().compareTo(curDate) >= 0);

        assertEquals(ACTIVATED, idea3AfterChecking.getFinishTrigger().getStatus());
        assertTrue(idea3AfterChecking.getFinishTrigger().getActivatedDate().compareTo(curDate) >= 0);
    }
}
