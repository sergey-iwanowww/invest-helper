package ru.isg.invest.helper.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.isg.invest.helper.model.InstrumentTypes;
import ru.isg.invest.helper.model.Markets;
import ru.isg.invest.helper.model.Sectors;
import ru.isg.invest.helper.model.TradingModes;

import java.util.UUID;

/**
 * Created by s.ivanov on 02.12.2021.
 */
@Getter
@Setter
@Accessors(chain = true)
public class InstrumentDto {

    private UUID id;
    private InstrumentTypes type;
    private String ticker;
    private String name;
    private Markets market;
    private TradingModes tradingMode;
    private String currencyCode;
    private String figi;
    private Sectors sector;
}
