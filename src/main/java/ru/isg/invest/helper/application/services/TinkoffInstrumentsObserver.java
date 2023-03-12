package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.infrastructure.tinkoff.TinkoffApiClient;
import ru.tinkoff.piapi.core.stream.MarketDataSubscriptionService;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static ru.tinkoff.piapi.contract.v1.SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIVE_MINUTES;

/**
 * Created by s.ivanov on 16.06.2022.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TinkoffInstrumentsObserver {

    public static final String STREAM_CANDLES = "candles";

    private final TinkoffApiClient tinkoffApiClient;
    private final TinkoffObservedCandleProcessor tinkoffObservedCandleProcessor;

    private final Set<String> observedInstrumentFigis = newHashSet();

    public void updateObservedCandles(Set<String> figis) {

        if (figis.equals(observedInstrumentFigis)) {
            log.info("Not need to update observed candles");
            return;
        }

        log.info("Need to update observed candles");

        if (!observedInstrumentFigis.isEmpty()) {

            log.info("Clear all observed candles: {}", observedInstrumentFigis);

            getSubscriptionService()
                    .unsubscribeCandles(newArrayList(observedInstrumentFigis), SUBSCRIPTION_INTERVAL_FIVE_MINUTES);
            observedInstrumentFigis.clear();
        }

        if (!figis.isEmpty()) {
            log.info("New observed candles: {}", figis);

            getSubscriptionService().subscribeCandles(newArrayList(figis), SUBSCRIPTION_INTERVAL_FIVE_MINUTES);
            observedInstrumentFigis.addAll(figis);
        } else {
            log.info("New observed candles set is empty");
        }
    }

    private MarketDataSubscriptionService getSubscriptionService() {

        MarketDataSubscriptionService candlesStream = tinkoffApiClient.getMarketDataStreamService()
                .getStreamById(STREAM_CANDLES);
        if (candlesStream == null) {

            log.info("Init candles stream");

            candlesStream = tinkoffApiClient.getMarketDataStreamService().newStream(STREAM_CANDLES,
                    (mdr) -> {
                        try {
                            tinkoffObservedCandleProcessor.process(mdr.getCandle());
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    },
                    (t) -> {

                        log.error(t.getMessage(), t);

                        try {
                            Thread.sleep(10000L);
                        } catch (InterruptedException e) {
                            log.error(e.getMessage(), e);
                        }

                        getSubscriptionService();
                    }
            );
        }

        return candlesStream;
    }
}