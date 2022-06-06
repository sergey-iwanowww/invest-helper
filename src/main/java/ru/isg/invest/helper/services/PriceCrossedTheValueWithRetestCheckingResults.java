package ru.isg.invest.helper.services;

import lombok.Value;
import ru.isg.invest.helper.model.Candle;

/**
 * Created by s.ivanov on 05.06.2022.
 */
@Value
public class PriceCrossedTheValueWithRetestCheckingResults {
    Candle crossCandle;
    Candle lastRetestCandle;
    Candle confirmCandle;
}
