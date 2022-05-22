package ru.isg.invest.helper.dto.tinkoff;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Created by s.ivanov on 20.11.2021.
 */
@Setter
@Getter
@ToString(callSuper = true)
public class Instrument {
    private String figi;
    private String ticker;
    private String isin;
    private BigDecimal minPriceIncrement;
    private BigDecimal faceValue;
    private BigDecimal lot;
    private String currency;
    private String name;
    private InstrumentTypes type;
    private BigDecimal minQuantity;
}
