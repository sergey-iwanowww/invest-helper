package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

import static javax.persistence.EnumType.STRING;

/**
 * Created by s.ivanov on 13.11.2021.
 */
@Entity
@Table(name = "instruments")
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class Instrument {

    public Instrument(InstrumentTypes type, String ticker, String name, Currencies currency, String figi, String exchange) {
        this.type = type;
        this.ticker = ticker;
        this.name = name;
        this.currency = currency;
        this.figi = figi;
        this.exchange = exchange;
    }

    @Id
    private UUID id = UUID.randomUUID();

    @Enumerated(STRING)
    @Column(nullable = false)
    private InstrumentTypes type;

    @Column(nullable = false)
    private String ticker;

    @Setter
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(STRING)
    private Currencies currency;

    @Column(nullable = false)
    private String figi;

    @Column(nullable = false)
    private String exchange;
}
