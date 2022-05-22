package ru.isg.invest.helper.dto;

import lombok.Data;
import ru.isg.invest.helper.model.InstrumentTypes;
import ru.isg.invest.helper.model.Markets;
import ru.isg.invest.helper.model.Sectors;
import ru.isg.invest.helper.model.TradingModes;

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

    @NotNull
    private Markets market;

    @NotNull
    private TradingModes tradingMode;

    @NotBlank
    private String currencyCode;

    @NotBlank
    private String figi;

    @NotNull
    private Sectors sector;
}
