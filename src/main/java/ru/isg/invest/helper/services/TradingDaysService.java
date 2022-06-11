package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.model.TradingDay;
import ru.isg.invest.helper.repositories.TradingDayRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Created by s.ivanov on 11.06.2022.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradingDaysService {

    private final TradingDayRepository tradingDayRepository;

    /**
     * Проверяет, торговался ли инструмент, начиная с dateFrom (включая) и до dateTo (исключая)
     */
    public boolean isInstrumentTraded(Instrument instrument, LocalDateTime dateFrom, LocalDateTime dateTo) {

        Map<LocalDate, TradingDay> tradingDays = tradingDayRepository
                .findTradingDayByExchangeAndDateGreaterThanEqualAndDateLessThanOrderByDate(instrument.getExchange(),
                        dateFrom, dateTo)
                .stream()
                .collect(Collectors.toMap(TradingDay::getDate, td -> td));

        // Важно учесть, что должны быть учтены дни, начиная от дня даты dateFrom включительно и до
        // дня даты dateTo включительно, если время dateTo > 00:00 и не включительно, если время dateTo == 00:00
        for (LocalDateTime date = dateFrom.truncatedTo(DAYS); date.isBefore(dateTo); date = date.plusDays(1)) {
            TradingDay tradingDay = tradingDays.get(date.toLocalDate());
            if (tradingDay != null) {
                // если день торговый и проверяемый период пересекается с временем торгов, считаем, что инструмент
                // торговался
                if (tradingDay.isTradingDay()
                        && tradingDay.getStartDate().compareTo(dateTo) <= 0
                        && tradingDay.getEndDate().compareTo(dateFrom) > 0) {
                    return true;
                }
            } else {
                // если tradingDay не найден, считаем, что инструмент торгуется в этот день
                return true;
            }
        }

        return false;
    }
}
