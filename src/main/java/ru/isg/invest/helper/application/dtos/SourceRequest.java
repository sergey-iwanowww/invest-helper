package ru.isg.invest.helper.application.dtos;

import lombok.Data;
import ru.isg.invest.helper.domain.model.SourceTypes;

import java.util.List;

/**
 * Created by s.ivanov on 11.01.2022.
 */
@Data
public class SourceRequest {
    private List<String> authorNames;
    private SourceTypes type;
    private String name;
    private String address;
}
