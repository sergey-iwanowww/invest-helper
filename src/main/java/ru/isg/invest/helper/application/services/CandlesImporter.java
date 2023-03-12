package ru.isg.invest.helper.application.services;

import ru.isg.invest.helper.domain.model.Instrument;
import ru.isg.invest.helper.domain.model.TimeFrames;

import java.time.LocalDateTime;

/**
 * Created by s.ivanov on 24.11.2021.
 */
public interface CandlesImporter {

    void importCandles(Instrument instrument, TimeFrames timeFrame, LocalDateTime dateFrom, LocalDateTime dateTo,
            boolean completedOnly);
}
