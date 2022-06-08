package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;
import ru.isg.invest.helper.dto.IdeaActivationEvent;
import ru.isg.invest.helper.dto.IdeaDto;
import ru.isg.invest.helper.dto.IdeaFinishingEvent;
import ru.isg.invest.helper.dto.IdeaRequest;
import ru.isg.invest.helper.dto.IdeaTriggerData;
import ru.isg.invest.helper.dto.IdeaTriggerDto;
import ru.isg.invest.helper.model.DateIdeaTrigger;
import ru.isg.invest.helper.model.Idea;
import ru.isg.invest.helper.model.IdeaTrigger;
import ru.isg.invest.helper.model.PriceIdeaTrigger;
import ru.isg.invest.helper.repositories.IdeaRepository;
import ru.isg.invest.helper.repositories.IdeaTriggerRepository;
import ru.isg.invest.helper.services.telegram.TelegramBotService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdeasService {

    private final IdeaRepository ideaRepository;
    private final IdeaTriggerRepository ideaTriggerRepository;
    private final InstrumentService instrumentService;
    private final SourceService sourceService;
    private final AuthorService authorService;
    private final TagService tagService;
    private final Notifier notifier;

    @Value("${pf-man.images.base.path:/data/images}")
    private String IMAGES_BASE_PATH;

    @Transactional
    public IdeaDto createIdea(IdeaRequest ideaRequest) {

        IdeaTrigger startTrigger = ideaTriggerFromRequest(ideaRequest.getStartTrigger());
        startTrigger = ideaTriggerRepository.save(startTrigger);

        IdeaTrigger finishTrigger = null;
        if (ideaRequest.getFinishTrigger() != null) {
            finishTrigger = ideaTriggerFromRequest(ideaRequest.getFinishTrigger());
            finishTrigger = ideaTriggerRepository.save(finishTrigger);
        }

        Idea idea = new Idea(instrumentService.getInstrument(ideaRequest.getInstrumentId()),
                ideaRequest.getConceptType(), ideaRequest.getGeneratedDate(),
                sourceService.getSource(ideaRequest.getSourceId()),
                authorService.getAuthor(ideaRequest.getAuthorId()));

        if (ideaRequest.getTagIds() != null) {
            idea.setTags(ideaRequest.getTagIds().stream()
                    .map(tagService::getTagEntity)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        idea.setText(ideaRequest.getText());

        idea = ideaRepository.save(idea);

        idea.setStartTrigger(startTrigger);
        ideaTriggerRepository.save(startTrigger);

        if (finishTrigger != null) {
            idea.setFinishTrigger(finishTrigger);
            ideaTriggerRepository.save(finishTrigger);
        }

        return ideaToDto(ideaRepository.save(idea));
    }

    @Transactional
    public List<IdeaDto> listIdeas() {
        return ideaRepository.findAll().stream()
                .map(this::ideaToDto)
                .collect(Collectors.toList());
    }

    private IdeaDto ideaToDto(Idea idea) {

        IdeaDto ideaDto = new IdeaDto()
                .setId(idea.getId())
                .setInstrument(instrumentService.instrumentToDto(idea.getInstrument()))
                .setStartTrigger(ideaTriggerToDto(idea.getStartTrigger()))
                .setFinishTrigger(ideaTriggerToDto(idea.getFinishTrigger()))
                .setConceptType(idea.getConceptType())
                .setGeneratedDate(idea.getGeneratedDate())
                .setSource(sourceService.sourceToDto(idea.getSource()))
                .setAuthor(authorService.authorToDto(idea.getAuthor()))
                .setText(idea.getText())
                .setActivatedDate(idea.getActivatedDate())
                .setActivatedPrice(idea.getActivatedPrice())
                .setFinishedDate(idea.getFinishedDate())
                .setFinishedPrice(idea.getFinishedPrice())
                .setCancelledDate(idea.getCancelledDate())
                .setCancelledPrice(idea.getCancelledPrice())
                .setCreatedDate(idea.getCreatedDate())
                .setStatus(idea.getStatus());

        if (StringUtils.hasText(idea.getImagePath())) {
            ideaDto.setImageUrl(generateImageUrl(idea));
        }

        if (idea.getTags() != null) {
            ideaDto.setTags(idea.getTags().stream()
                    .map(tagService::tagToDto)
                    .collect(Collectors.toList()));
        }

        return ideaDto;
    }

    private String generateImageUrl(Idea idea) {
        return "/ideas/" + idea.getId() + "/images";
    }

    public void saveImage(UUID ideaId, InputStream is) throws IOException {

        Idea idea = getIdeaEntity(ideaId);

        Files.createDirectories(Paths.get(IMAGES_BASE_PATH));

        if (StringUtils.hasText(idea.getImagePath())) {
            Files.deleteIfExists(Paths.get(idea.getImagePath()));
        }

        String pathStr = IMAGES_BASE_PATH + "/" + ideaId + ".jpg";

        Path path = Paths.get(pathStr);
        Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);

        idea.setImagePath(pathStr);
        ideaRepository.save(idea);
    }

    public byte[] getImage(UUID ideaId) throws IOException {
        return Files.readAllBytes(Paths.get(IMAGES_BASE_PATH + "/" + ideaId + ".jpg"));
    }

    public void deleteIdea(UUID ideaId) throws IOException {

        Idea idea = getIdeaEntity(ideaId);

        ideaRepository.delete(idea);

        if (StringUtils.hasText(idea.getImagePath())) {
            Files.deleteIfExists(Paths.get(idea.getImagePath()));
        }
    }

    @Transactional
    public IdeaDto updateIdea(UUID id, IdeaRequest ideaRequest) {

        Idea idea = getIdeaEntity(id);

        idea
                .setInstrument(instrumentService.getInstrument(ideaRequest.getInstrumentId()))
                .setStartTrigger(ideaTriggerFromRequest(ideaRequest.getStartTrigger()))
                .setConceptType(ideaRequest.getConceptType())
                .setGeneratedDate(ideaRequest.getGeneratedDate())
                .setSource(sourceService.getSource(ideaRequest.getSourceId()))
                .setAuthor(authorService.getAuthor(ideaRequest.getAuthorId()));

        if (ideaRequest.getFinishTrigger() != null) {
            idea.setFinishTrigger(ideaTriggerFromRequest(ideaRequest.getFinishTrigger()));
        }

        if (ideaRequest.getTagIds() != null) {
            idea.setTags(ideaRequest.getTagIds().stream()
                    .map(tagService::getTagEntity)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        idea.setText(ideaRequest.getText());

        return ideaToDto(ideaRepository.save(idea));
    }

    @Transactional
    public IdeaDto getIdea(UUID ideaId) {
        Idea idea = getIdeaEntity(ideaId);
        return ideaToDto(idea);
    }

    public Idea getIdeaEntity(UUID ideaId) {
        return ideaRepository.findById(ideaId).orElseThrow();
    }

    private IdeaTrigger ideaTriggerFromRequest(IdeaTriggerData ideaTriggerData) {

        if (ideaTriggerData == null) {
            return null;
        }

        if (ideaTriggerData.getDate() != null) {
            return new DateIdeaTrigger(ideaTriggerData.getDate());
        } else if (ideaTriggerData.getPrice() != null) {
            return new PriceIdeaTrigger(ideaTriggerData.getPrice(), ideaTriggerData.getDelta(),
                    ideaTriggerData.getWithRetest() != null && ideaTriggerData.getWithRetest(),
                    ideaTriggerData.getMonitoringTimeFrame());
        } else {
            throw new IllegalArgumentException("Trigger data is incorrect");
        }
    }

    private IdeaTriggerDto ideaTriggerToDto(IdeaTrigger ideaTrigger) {

        if (ideaTrigger == null) {
            return null;
        }

        IdeaTriggerDto result = null;

        // TODO: hibernate instanceof problem
        if (ideaTrigger instanceof DateIdeaTrigger dateIdeaTrigger) {
            result = new IdeaTriggerDto()
                    .setDate(dateIdeaTrigger.getDate());
        } else if (ideaTrigger instanceof PriceIdeaTrigger priceIdeaTrigger) {
            result = new IdeaTriggerDto()
                    .setPrice(priceIdeaTrigger.getPrice())
                    .setWithRetest(priceIdeaTrigger.isWithRetest())
                    .setDelta(priceIdeaTrigger.getDelta())
                    .setMonitoringTimeFrame(priceIdeaTrigger.getMonitoringTimeFrame());
        } else {
            throw new IllegalArgumentException("Trigger type not supported");
        }

        return result
                .setStatus(ideaTrigger.getStatus())
                .setWaitingForActivationSettedDate(ideaTrigger.getWaitingForActivationSettedDate())
                .setActivatedDate(ideaTrigger.getActivatedDate());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleIdeaActivationEvent(IdeaActivationEvent ideaActivationEvent) {
        notifier.notifyUsersAboutIdeaActivation(ideaActivationEvent.getIdeaId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleIdeaActivationEvent(IdeaFinishingEvent ideaFinishingEvent) {
        notifier.notifyUsersAboutIdeaFinishing(ideaFinishingEvent.getIdeaId());
    }
}
