package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Entity
@Table(name = "ideas")
@Getter
@Accessors(chain = true)
@ToString
@NoArgsConstructor(access = PROTECTED)
public class Idea {

    public Idea(Instrument instrument, IdeaTrigger startTrigger, ConceptTypes conceptType, LocalDate generatedDate,
            Source source, Author author) {
        this.instrument = instrument;
        this.startTrigger = startTrigger;
        this.conceptType = conceptType;
        this.generatedDate = generatedDate;
        this.source = source;
        this.author = author;
        this.createdDate = LocalDateTime.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Setter
    @OneToOne(cascade = ALL)
    @JoinColumn(name = "start_trigger_id", nullable = false)
    private IdeaTrigger startTrigger;

    @Setter
    @OneToOne(cascade = ALL)
    @JoinColumn(name = "finish_trigger_id")
    private IdeaTrigger finishTrigger;

    @Setter
    @Enumerated(STRING)
    @Column(nullable = false)
    private ConceptTypes conceptType;

    @Setter
    @Column(nullable = false)
    private LocalDate generatedDate;

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
    private LocalDateTime startedDate;

    @Setter
    private BigDecimal startedPrice;

    @Setter
    private LocalDateTime finishedDate;

    @Setter
    private BigDecimal finishedPrice;

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void setTags(List<Tag> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }
}
