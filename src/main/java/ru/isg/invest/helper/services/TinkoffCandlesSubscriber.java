package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.repositories.MonitoredInstrumentRepository;
import ru.tinkoff.piapi.contract.v1.Candle;
import ru.tinkoff.piapi.core.stream.MarketDataSubscriptionService;
import ru.tinkoff.piapi.core.utils.DateUtils;
import ru.tinkoff.piapi.core.utils.MapperUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

import static ru.tinkoff.piapi.contract.v1.SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE;

/**
 * Created by s.ivanov on 16.06.2022.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TinkoffCandlesSubscriber {

    private final TinkoffApiClient tinkoffApiClient;
    private final MonitoredInstrumentRepository monitoredInstrumentRepository;

    @PostConstruct
    public void init() {

        MarketDataSubscriptionService first = tinkoffApiClient.getMarketDataStreamService().newStream("first",
                (mdr) -> {
                    Candle candle = mdr.getCandle();
                    log.info("{} {}: {} - {}, {} - {}, {}, {}",
                            candle.getFigi(),
                            DateUtils.timestampToString(candle.getTime()),
                            MapperUtils.quotationToBigDecimal(candle.getOpen()),
                            MapperUtils.quotationToBigDecimal(candle.getClose()),
                            MapperUtils.quotationToBigDecimal(candle.getLow()),
                            MapperUtils.quotationToBigDecimal(candle.getHigh()),
                            DateUtils.timestampToString(candle.getLastTradeTs()),
                            candle.getVolume());
                },
                (t) -> {
                    log.error(t.getMessage(), t);
                }
        );

        List<String> figis = monitoredInstrumentRepository.findAll().stream()
                .map(mi -> mi.getInstrument().getFigi())
                .distinct()
                .collect(Collectors.toList());

        first.subscribeCandles(figis, SUBSCRIPTION_INTERVAL_ONE_MINUTE);
    }
}