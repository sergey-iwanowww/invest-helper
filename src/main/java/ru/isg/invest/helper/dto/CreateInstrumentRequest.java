package ru.isg.invest.helper.dto;

import lombok.Data;
import ru.isg.invest.helper.model.InstrumentTypes;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@Data
public class CreateInstrumentRequest {

    @NotNull
    private InstrumentTypes type;

    @NotBlank
    private String ticker;

    @NotBlank
    private String name;

    @NotBlank
    private String currencyCode;

    @NotBlank
    private String figi;
}
