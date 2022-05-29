package ru.isg.invest.helper.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.isg.invest.helper.model.IdeaConceptTypes;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@Data
public class IdeaRequest {

    @NotNull
    private UUID instrumentId;

    @NotNull
    private IdeaTriggerData startTrigger;

    private IdeaTriggerData finishTrigger;

    @NotNull
    private IdeaConceptTypes conceptType;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime generatedDate;

    @NotNull
    private UUID sourceId;

    @NotNull
    private UUID authorId;

    private String text;

    private List<UUID> tagIds;
}
