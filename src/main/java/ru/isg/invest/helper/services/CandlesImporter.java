package ru.isg.invest.helper.services;

import ru.isg.invest.helper.dto.ImportCandlesResult;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.model.TimeFrames;

import java.time.LocalDateTime;

/**
 * Created by s.ivanov on 24.11.2021.
 */
public interface CandlesImporter {

    ImportCandlesResult importCandles(Instrument instrument, TimeFrames timeFrame, LocalDateTime dateFrom, LocalDateTime dateTo);
}
