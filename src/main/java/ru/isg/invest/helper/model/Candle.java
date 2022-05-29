package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
@Accessors(chain = true)
public class Candle {

    public Candle(Instrument instrument, TimeFrames timeFrame, LocalDateTime openDate, BigDecimal min, BigDecimal max, BigDecimal open,
            BigDecimal close, long volume, boolean complete) {
        this.instrument = instrument;
        this.timeFrame = timeFrame;
        this.openDate = openDate;
        this.closeDate = openDate.plus(timeFrame.getAmount(), timeFrame.getChronoUnit()).minusSeconds(1);
        this.min = min.setScale(4, RoundingMode.HALF_UP);
        this.max = max.setScale(4, RoundingMode.HALF_UP);
        this.open = open.setScale(4, RoundingMode.HALF_UP);
        this.close = close.setScale(4, RoundingMode.HALF_UP);
        this.volume = volume;
        this.complete = complete;
    }

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(nullable = false)
    @Enumerated(STRING)
    private TimeFrames timeFrame;

    @Column(nullable = false)
    private LocalDateTime openDate;

    @Column(nullable = false)
    private LocalDateTime closeDate;

    @Column(nullable = false)
    private BigDecimal min;

    @Column(nullable = false)
    private BigDecimal max;

    @Column(nullable = false)
    private BigDecimal open;

    @Column(nullable = false)
    private BigDecimal close;

    @Setter
    @Column(nullable = false)
    private long volume;

    @Setter
    @Column(nullable = false)
    private boolean complete;

    public Candle setMin(BigDecimal min) {
        this.min = min.setScale(4, RoundingMode.HALF_UP);
        return this;
    }

    public Candle setMax(BigDecimal max) {
        this.max = max.setScale(4, RoundingMode.HALF_UP);
        return this;
    }

    public Candle setOpen(BigDecimal open) {
        this.open = open.setScale(4, RoundingMode.HALF_UP);
        return this;
    }

    public Candle setClose(BigDecimal close) {
        this.close = close.setScale(4, RoundingMode.HALF_UP);
        return this;
    }
}
