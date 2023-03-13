package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.domain.model.Candle;
import ru.isg.invest.helper.domain.model.Instrument;
import ru.isg.invest.helper.domain.model.TimeFrames;
import ru.isg.invest.helper.infrastructure.repositories.CandleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by s.ivanov on 05.06.2022.
 */
@Service
@RequiredArgsConstructor
public class CandlesAnalyzer {

    private final CandleRepository candleRepository;

    public Optional<Candle> getLastCandle(Instrument instrument, TimeFrames timeFrame) {
        return candleRepository.getCandlesByInstrumentIdAndTimeFrameOrderByOpenDateDesc(instrument.getId(), timeFrame,
                PageRequest.of(0, 1)).stream().findAny();
    }

    public Optional<BigDecimal> getLastPrice(Instrument instrument, TimeFrames timeFrame, LocalDateTime date) {
        return candleRepository
                .getCandlesByOpenDate(instrument.getId(), timeFrame, date).stream()
                .sorted(Comparator.comparing(Candle::getOpenDate).reversed())
                .limit(1)
                .findAny()
                .map(candle -> date.compareTo(candle.getCloseDate()) >= 0 ? candle.getClose() : candle.getOpen());
    }

    public PriceCrossedTheValueCheckingResults checkPriceCrossedTheValue(Instrument instrument, TimeFrames timeFrame,
            LocalDateTime dateFrom, LocalDateTime dateTo, boolean rise, BigDecimal value) {

        List<Candle> candles = candleRepository
                .getCandlesByCloseDate(instrument.getId(), timeFrame, dateFrom, dateTo).stream()
                .sorted(Comparator.comparing(Candle::getOpenDate))
                .collect(Collectors.toList());

        for (Candle candle : candles) {
            if (rise && candle.getClose().compareTo(value) >= 0
                    || !rise && candle.getClose().compareTo(value) <= 0) {
                return new PriceCrossedTheValueCheckingResults(candle);
            }
        }
        return new PriceCrossedTheValueCheckingResults(null);
    }

    public PriceCrossedTheValueWithRetestCheckingResults checkPriceCrossedTheValueWithRetest(Instrument instrument,
            TimeFrames timeFrame,
            LocalDateTime dateFrom, LocalDateTime dateTo, boolean rise, BigDecimal value, BigDecimal delta) {

        List<Candle> candles = candleRepository
                .getCandlesByCloseDate(instrument.getId(), timeFrame, dateFrom, dateTo).stream()
                .sorted(Comparator.comparing(Candle::getOpenDate))
                .collect(Collectors.toList());

        var crossed = false;
        Candle crossCandle = null;

        var retested = false;
        Candle lastRetestCandle = null;

        for (Candle candle : candles) {
            if (retested) {

                if (rise && candle.getClose().compareTo(crossCandle.getClose()) > 0
                        || !rise && candle.getClose().compareTo(crossCandle.getClose()) < 0) {

                    // если в состоянии 'retested' свеча преодолела уровень закрытия crossed свечи, считаем, что ретест произошел

                    return new PriceCrossedTheValueWithRetestCheckingResults(crossCandle, lastRetestCandle, candle);

                } else if (rise && candle.getClose().compareTo(value.subtract(delta)) >= 0 && candle.getClose()
                        .compareTo(value.add(delta)) <= 0
                        || !rise && candle.getClose().compareTo(value.add(delta)) <= 0 && candle.getClose()
                        .compareTo(value.subtract(delta)) >= 0) {

                    // если в состоянии 'retested' цена осталась в диапазоне, обновляем lastRetestCandle

                    lastRetestCandle = candle;

                } else if (rise && candle.getClose().compareTo(value.subtract(delta)) < 0
                        || !rise && candle.getClose().compareTo(value.add(delta)) > 0) {

                    // если в состоянии 'retested' цена опустилась ниже уровня диапазона, начинаем сначала

                    retested = false;
                    lastRetestCandle = null;

                    crossed = false;
                    crossCandle = null;
                }

            } else if (crossed) {

                if (rise && candle.getClose().compareTo(crossCandle.getClose()) < 0
                        && candle.getClose().compareTo(value.subtract(delta)) >= 0
                        && candle.getClose().compareTo(value.add(delta)) <= 0
                        || !rise && candle.getClose().compareTo(crossCandle.getClose()) > 0
                        && candle.getClose().compareTo(value.add(delta)) <= 0
                        && candle.getClose().compareTo(value.subtract(delta)) >= 0) {

                    // если в состоянии 'crossed' цена откатилась в диапазон, при этом перепрыгнув цену закрытия crossed свечи,
                    // переходим в состояние 'retested'

                    retested = true;
                    lastRetestCandle = candle;

                } else if (rise && candle.getClose().compareTo(value.subtract(delta)) < 0
                        || !rise && candle.getClose().compareTo(value.add(delta)) > 0) {

                    // если в состоянии 'crossed' цена вернулась и вышла из диапазона, начинаем сначала

                    retested = false;
                    lastRetestCandle = null;

                    crossed = false;
                    crossCandle = null;
                }
            } else {

                if (rise && candle.getClose().compareTo(value) >= 0
                        || !rise && candle.getClose().compareTo(value) <= 0) {

                    // если свеча пробила уровень, переходим в состояние 'crossed', запоминаем свечу

                    crossed = true;
                    crossCandle = candle;
                }
            }
        }

        return new PriceCrossedTheValueWithRetestCheckingResults(crossCandle, lastRetestCandle, null);
    }
}
