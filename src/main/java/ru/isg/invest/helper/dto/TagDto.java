package ru.isg.invest.helper.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

/**
 * Created by s.ivanov on 11.01.2022.
 */
@Getter
@Setter
@Accessors(chain = true)
public class TagDto {
    private UUID id;
    private String name;
}
