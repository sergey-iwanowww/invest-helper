package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.dto.IdeaActivationEvent;
import ru.isg.invest.helper.dto.IdeaFinishingEvent;
import ru.isg.invest.helper.model.DateIdeaTrigger;
import ru.isg.invest.helper.model.Idea;
import ru.isg.invest.helper.model.IdeaStatuses;
import ru.isg.invest.helper.model.IdeaTrigger;
import ru.isg.invest.helper.model.PriceIdeaTrigger;
import ru.isg.invest.helper.repositories.CandleRepository;
import ru.isg.invest.helper.repositories.IdeaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;
import static ru.isg.invest.helper.model.IdeaConceptTypes.RISE;
import static ru.isg.invest.helper.model.IdeaStatuses.ACTIVE;
import static ru.isg.invest.helper.model.IdeaTriggerStatuses.WAITING_FOR_ACTIVATION;
import static ru.isg.invest.helper.model.TimeFrames.ONE_HOUR;

/**
 * Created by s.ivanov on 29.05.2022.
 */
@Service
@RequiredArgsConstructor
public class IdeasChecker {

    private final IdeaRepository ideaRepository;
    private final CandlesAnalyzer candlesAnalyzer;
    private final TriggersChecker triggersChecker;

    @Transactional
    public void check() {
        ideaRepository.getIdeasByStatusIn(List.of(IdeaStatuses.WAITING_FOR_ACTIVATION, ACTIVE))
                .forEach(this::processIdea);
    }

    public void processIdea(Idea idea) {

        if (idea.getStatus() == IdeaStatuses.WAITING_FOR_ACTIVATION) {
            processWaitingForActivationIdea(idea);
        }

        if (idea.getStatus() == ACTIVE) {
            processActiveIdea(idea);
        }
    }

    private void processWaitingForActivationIdea(Idea idea) {

        IdeaTrigger startTrigger = idea.getStartTrigger();

        checkState(startTrigger.getStatus() == WAITING_FOR_ACTIVATION,
                "Start trigger has no suitable status for processing: " + startTrigger.getStatus());

        startTrigger.acceptVisitor(new IdeaTriggerProcessorVisitor());
    }

    private void processActiveIdea(Idea idea) {

        if (idea.getFinishTrigger() == null) {
            return;
        }

        IdeaTrigger finishTrigger = idea.getFinishTrigger();

        checkState(finishTrigger.getStatus() == WAITING_FOR_ACTIVATION,
                "Finish trigger has no suitable status for processing: " + finishTrigger.getStatus());

        finishTrigger.acceptVisitor(new IdeaTriggerProcessorVisitor());
    }

    private static int fake = 0;

    @Transactional
    public void fake() {
        if (fake % 2 == 0) {
            ServiceRegistry.getApplicationEventPublisher()
                    .publishEvent(new IdeaActivationEvent(UUID.fromString("af215ebb-5a15-4045-8f23-4b184de39f88")));
        } else {
            ServiceRegistry.getApplicationEventPublisher()
                    .publishEvent(new IdeaFinishingEvent(UUID.fromString("af215ebb-5a15-4045-8f23-4b184de39f88")));
        }
        fake++;
    }

    private class IdeaTriggerProcessorVisitor implements IdeaTriggerVisitor {
        @Override
        public void visitDataIdeaTrigger(DateIdeaTrigger trigger) {
            TriggerActivationNeededResults results = triggersChecker.checkActivationNeeded(trigger);
            if (results.isActivationNeeded()) {
                trigger.activate(results.getDate(), results.getPrice());
            }
        }
        @Override
        public void visitPriceIdeaTrigger(PriceIdeaTrigger trigger) {
            TriggerActivationNeededResults results = triggersChecker.checkActivationNeeded(trigger);
            if (results.isActivationNeeded()) {
                trigger.activate(results.getDate(), results.getPrice());
            }
        }
    }
}
