package ru.isg.invest.helper.infrastructure.tinkoff;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.stream.MarketDataStreamService;

import javax.annotation.PostConstruct;

/**
 * Created by s.ivanov on 10.06.2022.
 */
@Service
public class TinkoffApiClient {

    private InvestApi api;

    @Value("${tinkoff.api.token}")
    private String token;

    @PostConstruct
    public void init() {
        api = InvestApi.createReadonly(token);
    }

    public MarketDataService getMarketDataService() {
        return api.getMarketDataService();
    }

    public InstrumentsService getInstrumentsService() {
        return api.getInstrumentsService();
    }

    public OperationsService getOperationsService() {
        return api.getOperationsService();
    }

    public MarketDataStreamService getMarketDataStreamService() {
        return api.getMarketDataStreamService();
    }
}
