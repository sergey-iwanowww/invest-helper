package ru.isg.invest.helper.services;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.dto.ImportCandlesResult;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.model.TimeFrames;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.InvestApi;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Created by s.ivanov on 26.05.2022.
 */
@Service
@Slf4j
public class TinkoffCandlesImporter implements CandlesImporter {

    InvestApi api;

    @Value("${tinkoff.api.token}")
    private String token;

    @PostConstruct
    public void init() {
        api = InvestApi.createSandbox(token);
    }

    @Override
    public ImportCandlesResult importCandles(Instrument instrument, TimeFrames timeFrame, LocalDateTime dateFrom,
            LocalDateTime dateTo) {

        var candles = api.getMarketDataService()
                .getCandlesSync(instrument.getFigi(), dateFrom.toInstant(ZoneOffset.UTC),
                        dateTo.toInstant(ZoneOffset.UTC),
                        toCandleInterval(timeFrame));

        for (HistoricCandle candle : candles) {
            printCandle(candle);
        }

        return new ImportCandlesResult(LocalDateTime.now());
    }

    private CandleInterval toCandleInterval(TimeFrames timeFrame) {
        return switch (timeFrame) {
            case FIVE_MINS -> CandleInterval.CANDLE_INTERVAL_5_MIN;
            case ONE_HOUR -> CandleInterval.CANDLE_INTERVAL_HOUR;
            case ONE_DAY, ONE_WEEK, ONE_MONTH -> CandleInterval.CANDLE_INTERVAL_DAY;
        };
    }

    private static void printCandle(HistoricCandle candle) {
        var open = quotationToBigDecimal(candle.getOpen());
        var close = quotationToBigDecimal(candle.getClose());
        var high = quotationToBigDecimal(candle.getHigh());
        var low = quotationToBigDecimal(candle.getLow());
        var volume = candle.getVolume();
        var time = timestampToString(candle.getTime());
        log.info(
                "цена открытия: {}, цена закрытия: {}, минимальная цена за 1 лот: {}, максимальная цена за 1 лот: {}, объем " +
                        "торгов в лотах: {}, время свечи: {}",
                open, close, low, high, volume, time);
    }

    public static BigDecimal quotationToBigDecimal(Quotation value) {
        if (value == null) {
            return null;
        }
        return mapUnitsAndNanos(value.getUnits(), value.getNano());
    }

    public static BigDecimal mapUnitsAndNanos(long units, int nanos) {
        if (units == 0 && nanos == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(units).add(BigDecimal.valueOf(nanos, 9));
    }

    public static String timestampToString(Timestamp timestamp) {
        return epochMillisToString(timestamp.getSeconds() * 1_000);
    }

    public static String epochMillisToString(long epochMillis) {
        var zonedDateTime = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.of("Etc/GMT"));
        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }
}
