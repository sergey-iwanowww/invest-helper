package ru.isg.invest.helper.dto;

import lombok.Data;
import ru.isg.invest.helper.model.Brokers;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@Data
public class CreatePortfolioRequest {

    @NotBlank
    private String name;

    @NotNull
    private Brokers broker;
}
