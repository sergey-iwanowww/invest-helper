package ru.isg.invest.helper.model;

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

import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;
import static ru.isg.invest.helper.model.CandlesImportTaskStatuses.ACTIVE;
import static ru.isg.invest.helper.model.CandlesImportTaskStatuses.DONE;
import static ru.isg.invest.helper.model.CandlesImportTaskStatuses.PROCESSING;

/**
 * Created by s.ivanov on 5/22/22.
 */
@Entity
@Table(name = "candles_import_tasks")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class CandlesImportTask {

    public CandlesImportTask(Instrument instrument, TimeFrames timeFrame, LocalDateTime dateFrom,
            LocalDateTime dateTo) {
        this.instrument = instrument;
        this.timeFrame = timeFrame;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.createdDate = LocalDateTime.now();
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

    @Column(nullable = false)
    private LocalDateTime dateTo;

    @Setter
    @Enumerated(STRING)
    @Column(nullable = false)
    private CandlesImportTaskStatuses status = ACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    public void processingStarted() {
        this.status = PROCESSING;
    }

    public void processingFinished(LocalDateTime lastCompletedCandleDate) {

        if (lastCompletedCandleDate.plus(timeFrame.getAmount(), timeFrame.getChronoUnit()).compareTo(dateTo) >= 0) {
            status = DONE;
        } else {
            status = ACTIVE;
        }
    }
}
