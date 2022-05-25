package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
import static ru.isg.invest.helper.model.CandlesImportTaskStatuses.ACTIVE;
import static ru.isg.invest.helper.model.CandlesImportTaskStatuses.DONE;
import static ru.isg.invest.helper.model.CandlesImportTaskStatuses.IN_PROGRESS;

/**
 * Created by s.ivanov on 5/22/22.
 */
@Entity
@Table(name = "candles_import_tasks")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class CandlesImportTask {

    public CandlesImportTask(Instrument instrument, TimeFrames timeFrame, LocalDateTime dateFrom, LocalDateTime dateTo) {
        this.instrument = instrument;
        this.timeFrame = timeFrame;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.createdDate = LocalDateTime.now();
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
    private LocalDateTime dateFrom;

    @Column(nullable = false)
    private LocalDateTime dateTo;

    @Setter
    private LocalDateTime lastImportedCandleDateFrom;

    @Setter
    @Enumerated(STRING)
    private CandlesImportTaskStatuses status = ACTIVE;

    private LocalDateTime createdDate;

    public void processingStarted() {
        this.status = IN_PROGRESS;
    }

    public void processingFinished() {
        if (lastImportedCandleDateFrom.plus(timeFrame.getAmount(), timeFrame.getChronoUnit()).compareTo(dateTo) >= 0) {
            status = DONE;
        } else {
            status = ACTIVE;
        }
    }
}
