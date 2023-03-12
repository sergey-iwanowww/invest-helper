package ru.isg.invest.helper.application.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.isg.invest.helper.domain.model.InstrumentTypes;
import ru.isg.invest.helper.domain.model.Sectors;
import ru.isg.invest.helper.domain.model.TradingModes;

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
    private TradingModes tradingMode;
    private String currencyCode;
    private String figi;
    private Sectors sector;
}
