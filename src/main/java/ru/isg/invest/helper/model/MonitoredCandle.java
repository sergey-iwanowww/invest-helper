package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

/**
 * Created by s.ivanov on 5/22/22.
 */
@Entity
@Table(name = "monitored_candles")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class MonitoredCandle {

    public MonitoredCandle(Instrument instrument, TimeFrames timeFrame) {
        this.instrument = instrument;
        this.timeFrame = timeFrame;
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

    private LocalDateTime lastCandleDate;
}
