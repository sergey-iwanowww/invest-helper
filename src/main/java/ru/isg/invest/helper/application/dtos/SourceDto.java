package ru.isg.invest.helper.application.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.isg.invest.helper.domain.model.SourceTypes;

import java.util.UUID;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Getter
@Setter
@Accessors(chain = true)
public class SourceDto {
    private UUID id;
    private SourceTypes type;
    private String name;
    private String address;
}
