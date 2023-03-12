package ru.isg.invest.helper.application.services;

import lombok.Value;
import ru.isg.invest.helper.domain.model.Candle;

/**
 * Created by s.ivanov on 05.06.2022.
 */
@Value
public class PriceCrossedTheValueWithRetestCheckingResults {
    Candle crossCandle;
    Candle lastRetestCandle;
    Candle confirmCandle;
}
