package ru.isg.invest.helper.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

/**
 * Created by s.ivanov on 5/22/22.
 */
@Entity
@Table(name = "candles_import_tasks")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class CandlesImportTask {

    public CandlesImportTask(Instrument instrument, TimeFrames timeFrame, LocalDateTime dateFrom,
            LocalDateTime dateTo, boolean completedOnly) {
        this.instrument = instrument;
        this.timeFrame = timeFrame;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.createdDate = LocalDateTime.now(UTC);
        this.completedOnly = completedOnly;
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
    private LocalDateTime dateFrom;

    @Column
    private LocalDateTime dateTo;

    @Setter
    @Enumerated(STRING)
    @Column(nullable = false)
    private CandlesImportTaskStatuses status = CandlesImportTaskStatuses.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private boolean completedOnly;

    public void processingStarted() {
        this.status = CandlesImportTaskStatuses.PROCESSING;
    }

    public void processingFinished() {
        status = CandlesImportTaskStatuses.DONE;
    }
}
