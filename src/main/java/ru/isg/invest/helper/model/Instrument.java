package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * Created by s.ivanov on 13.11.2021.
 */
@Entity
@Table(name = "instruments")
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ToString
public class Instrument {

    @Id
    private UUID id = UUID.randomUUID();

    @Setter
    @Enumerated(EnumType.STRING)
    private InstrumentTypes type;

    @Setter
    private String ticker;

    @Setter
    private String name;

    @Setter
    @Enumerated(EnumType.STRING)
    private Markets market;

    @Setter
    @Enumerated(EnumType.STRING)
    private TradingModes tradingMode;

    @Setter
    private String currencyCode;

    @Setter
    private String figi;

    @Setter
    @Enumerated(EnumType.STRING)
    private Sectors sector;
}
