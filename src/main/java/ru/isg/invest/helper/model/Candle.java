package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

/**
 * Created by s.ivanov on 25.05.2022.
 */
@Entity
@Table(name = "candles")
@Getter
@ToString
@NoArgsConstructor(access = PROTECTED)
public class Candle {

    public Candle(Instrument instrument, TimeFrames timeFrame, BigDecimal min, BigDecimal max, BigDecimal open,
            BigDecimal close, BigDecimal volume) {
        this.instrument = instrument;
        this.timeFrame = timeFrame;
        this.min = min;
        this.max = max;
        this.open = open;
        this.close = close;
        this.volume = volume;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(nullable = false)
    @Enumerated(STRING)
    private TimeFrames timeFrame;

    @Column(nullable = false)
    private BigDecimal min;

    @Column(nullable = false)
    private BigDecimal max;

    @Column(nullable = false)
    private BigDecimal open;

    @Column(nullable = false)
    private BigDecimal close;

    @Column(nullable = false)
    private BigDecimal volume;
}
