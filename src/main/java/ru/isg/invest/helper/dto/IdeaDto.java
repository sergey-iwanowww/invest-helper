package ru.isg.invest.helper.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.isg.invest.helper.model.ConceptTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Getter
@Setter
@Accessors(chain = true)
public class IdeaDto {
    private UUID id;
    private InstrumentDto instrument;
    private IdeaTriggerDto startTrigger;
    private IdeaTriggerDto finishTrigger;
    private ConceptTypes conceptType;
    private LocalDate generatedDate;
    private SourceDto source;
    private AuthorDto author;
    private String text;
    private String imageUrl;
    private List<TagDto> tags;
    private LocalDateTime startedDate;
    private BigDecimal startedPrice;
    private LocalDateTime finishedDate;
    private BigDecimal finishedPrice;
}
