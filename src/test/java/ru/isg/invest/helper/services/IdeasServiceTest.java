package ru.isg.invest.helper.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.application.dtos.IdeaDto;
import ru.isg.invest.helper.application.dtos.IdeaRequest;
import ru.isg.invest.helper.application.dtos.IdeaTriggerData;
import ru.isg.invest.helper.application.services.IdeasService;
import ru.isg.invest.helper.domain.model.Author;
import ru.isg.invest.helper.domain.model.IdeaStatuses;
import ru.isg.invest.helper.domain.model.IdeaTriggerStatuses;
import ru.isg.invest.helper.domain.model.Instrument;
import ru.isg.invest.helper.domain.model.Source;
import ru.isg.invest.helper.domain.model.Tag;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static ru.isg.invest.helper.domain.model.IdeaConceptTypes.FALL;
import static ru.isg.invest.helper.domain.model.TimeFrames.ONE_HOUR;

/**
 * Created by s.ivanov on 30.05.2022.
 */
@SpringBootTest
public class IdeasServiceTest {

    @Autowired
    private IdeasService ideasService;
    @Autowired
    private TestHelper testHelper;

    @Test
    @Transactional
    public void testIdeaCreating() {

        Author author = testHelper.getRandomAuthor();

        Source source = testHelper.getRandomSource();

        Instrument instrument = testHelper.getRandomInstrument();

        List<Tag> tags = testHelper.getRandomTags(2);

        IdeaRequest ideaRequest = new IdeaRequest()
                .setAuthorId(author.getId())
                .setSourceId(source.getId())
                .setConceptType(FALL)
                .setStartTrigger(new IdeaTriggerData()
                        .setDelta(BigDecimal.valueOf(5))
                        .setPrice(BigDecimal.valueOf(200))
                        .setWithRetest(true)
                        .setMonitoringTimeFrame(ONE_HOUR))
                .setFinishTrigger(new IdeaTriggerData()
                        .setDate(LocalDateTime.now(UTC).plusDays(1))
                        .setMonitoringTimeFrame(ONE_HOUR))
                .setGeneratedDate(LocalDateTime.now(UTC).minusMinutes(1))
                .setInstrumentId(instrument.getId())
                .setText("тест текст")
                .setTagIds(tags.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toList()));

        IdeaDto idea = ideasService.createIdea(ideaRequest);

        assertEquals(2, idea.getTags().size());
        assertEquals(IdeaStatuses.WAITING_FOR_ACTIVATION, idea.getStatus());
        assertNull(idea.getActivatedDate());
        assertNull(idea.getActivatedPrice());
        assertNull(idea.getFinishedDate());
        assertNull(idea.getFinishedPrice());

        assertEquals(IdeaTriggerStatuses.WAITING_FOR_ACTIVATION, idea.getStartTrigger().getStatus());
        assertNotNull(idea.getStartTrigger().getWaitingForActivationSettedDate());
        assertNull(idea.getStartTrigger().getActivatedDate());

        assertEquals(IdeaTriggerStatuses.NEW, idea.getFinishTrigger().getStatus());
        assertNull(idea.getFinishTrigger().getWaitingForActivationSettedDate());
        assertNull(idea.getFinishTrigger().getActivatedDate());
    }
}
