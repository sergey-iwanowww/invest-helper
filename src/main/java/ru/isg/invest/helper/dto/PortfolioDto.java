package ru.isg.invest.helper.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.isg.invest.helper.model.Brokers;

import java.util.UUID;

/**
 * Created by s.ivanov on 02.12.2021.
 */
@Getter
@Accessors(chain = true)
public class PortfolioDto {

    @Setter
    private UUID id;

    @Setter
    private String name;

    @Setter
    private Brokers broker;

}
