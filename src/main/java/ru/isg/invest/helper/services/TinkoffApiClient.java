package ru.isg.invest.helper.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;

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
        api = InvestApi.createSandbox(token);
    }

    public MarketDataService getMarketDataService() {
        return api.getMarketDataService();
    }

    public InstrumentsService getInstrumentsService() {
        return api.getInstrumentsService();
    }
}
