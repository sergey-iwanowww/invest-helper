package ru.isg.invest.helper.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.isg.invest.helper.application.dtos.IdeaActivationEvent;
import ru.isg.invest.helper.application.dtos.IdeaFinishingEvent;
import ru.isg.invest.helper.application.services.ServiceRegistry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.ZoneOffset.UTC;
import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Entity
@Table(name = "ideas")
@Getter
@Accessors(chain = true)
@NoArgsConstructor(access = PROTECTED)
public class Idea {

    public Idea(Instrument instrument, IdeaConceptTypes conceptType, LocalDateTime generatedDate, Source source, Author author) {

        this.instrument = instrument;
        this.conceptType = conceptType;
        this.generatedDate = generatedDate;
        this.source = source;
        this.author = author;
        this.createdDate = LocalDateTime.now(UTC);
        this.status = IdeaStatuses.WAITING_FOR_ACTIVATION;
    }

    @Id
    private UUID id = UUID.randomUUID();

    @Setter
    @ManyToOne
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @OneToOne
    @JoinColumn(name = "start_trigger_id")
    private IdeaTrigger startTrigger;

    @OneToOne
    @JoinColumn(name = "finish_trigger_id")
    private IdeaTrigger finishTrigger;

    @Setter
    @Enumerated(STRING)
    @Column(nullable = false)
    private IdeaConceptTypes conceptType;

    @Setter
    @Column(nullable = false)
    private LocalDateTime generatedDate;

    @Setter
    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Setter
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Setter
    private String text;

    @Setter
    private String imagePath;

    @ManyToMany
    @JoinTable(
            name = "idea_tag_links",
            joinColumns = @JoinColumn(name = "idea_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = newArrayList();

    @Setter
    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Setter
    private LocalDateTime deletedDate;

    @Setter
    private LocalDateTime activatedDate;

    @Setter
    private BigDecimal activatedPrice;

    @Setter
    private LocalDateTime finishedDate;

    @Setter
    private BigDecimal finishedPrice;

    @Setter
    private LocalDateTime cancelledDate;

    @Setter
    private BigDecimal cancelledPrice;

    @Setter
    @Column(nullable = false)
    @Enumerated(STRING)
    private IdeaStatuses status = IdeaStatuses.WAITING_FOR_ACTIVATION;

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void setTags(List<Tag> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public Idea setStartTrigger(IdeaTrigger startTrigger) {

        startTrigger.setIdea(this);
        startTrigger.setWaitingForActivation();
        this.startTrigger = startTrigger;

        return this;
    }

    public Idea setFinishTrigger(IdeaTrigger finishTrigger) {

        finishTrigger.setIdea(this);
        this.finishTrigger = finishTrigger;

        return this;
    }

    public void activate(LocalDateTime activatedDate, BigDecimal activatedPrice) {

        this.activatedDate = activatedDate;
        this.activatedPrice = activatedPrice;

        if (this.finishTrigger != null) {
            this.finishTrigger.setWaitingForActivation();
        }

        this.status = IdeaStatuses.ACTIVE;

        ServiceRegistry.getApplicationEventPublisher().publishEvent(new IdeaActivationEvent(this.id));
    }

    public void finish(LocalDateTime finishedDate, BigDecimal finishedPrice) {

        this.finishedDate = finishedDate;
        this.finishedPrice = finishedPrice;

        this.status = IdeaStatuses.FINISHED;

        ServiceRegistry.getApplicationEventPublisher().publishEvent(new IdeaFinishingEvent(this.id));
    }

    public void cancel(LocalDateTime cancelledDate, BigDecimal cancelledPrice) {

        this.cancelledDate = cancelledDate;
        this.cancelledPrice = cancelledPrice;

        this.status = IdeaStatuses.CANCELLED;
    }

    public void processTriggerActivation(IdeaTrigger trigger, LocalDateTime activatedDate, BigDecimal activatedPrice) {
        if (trigger == startTrigger) {
            activate(activatedDate, activatedPrice);
        } else if (trigger == finishTrigger) {
            finish(activatedDate, activatedPrice);
        } else {
            throw new IllegalArgumentException("Unknown trigger with id = " + trigger.getId());
        }
    }
}
