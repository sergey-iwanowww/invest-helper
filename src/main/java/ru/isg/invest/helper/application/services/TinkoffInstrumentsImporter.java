package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.domain.model.Currencies;
import ru.isg.invest.helper.domain.model.Instrument;
import ru.isg.invest.helper.domain.model.InstrumentTypes;
import ru.isg.invest.helper.infrastructure.repositories.InstrumentRepository;
import ru.isg.invest.helper.infrastructure.tinkoff.TinkoffApiClient;

import static ru.isg.invest.helper.domain.model.InstrumentTypes.BOND;
import static ru.isg.invest.helper.domain.model.InstrumentTypes.CURRENCY;
import static ru.isg.invest.helper.domain.model.InstrumentTypes.FUND;
import static ru.isg.invest.helper.domain.model.InstrumentTypes.FUTURE;
import static ru.isg.invest.helper.domain.model.InstrumentTypes.STOCK;
import static ru.tinkoff.piapi.contract.v1.InstrumentStatus.INSTRUMENT_STATUS_ALL;

/**
 * Created by s.ivanov on 12.06.2022.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TinkoffInstrumentsImporter {

    private final TinkoffApiClient tinkoffApiClient;
    private final InstrumentRepository instrumentRepository;

    @Transactional
    public void importInstruments() {

        tinkoffApiClient.getInstrumentsService().getBondsSync(INSTRUMENT_STATUS_ALL)
                .forEach(i -> saveInstrument(BOND, i.getTicker(), i.getName(), i.getCurrency(), i.getFigi(),
                        i.getExchange()));

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        tinkoffApiClient.getInstrumentsService().getCurrenciesSync(INSTRUMENT_STATUS_ALL)
                .forEach(i -> saveInstrument(CURRENCY, i.getTicker(), i.getName(), i.getCurrency(), i.getFigi(),
                        i.getExchange()));

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        tinkoffApiClient.getInstrumentsService().getEtfsSync(INSTRUMENT_STATUS_ALL)
                .forEach(i -> saveInstrument(FUND, i.getTicker(), i.getName(), i.getCurrency(), i.getFigi(),
                        i.getExchange()));

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        tinkoffApiClient.getInstrumentsService().getFuturesSync(INSTRUMENT_STATUS_ALL)
                .forEach(i -> saveInstrument(FUTURE, i.getTicker(), i.getName(), i.getCurrency(), i.getFigi(),
                        i.getExchange()));

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        tinkoffApiClient.getInstrumentsService().getSharesSync(INSTRUMENT_STATUS_ALL)
                .forEach(i -> saveInstrument(STOCK, i.getTicker(), i.getName(), i.getCurrency(), i.getFigi(),
                        i.getExchange()));
    }

    private Instrument saveInstrument(InstrumentTypes type, String ticker, String name, String currency, String figi,
            String exchange) {

        log.info("type: {}, ticker: {}, name: {}, currency: {}, exchange: {}",
                type, ticker, name, currency, exchange);

        if (!currency.equalsIgnoreCase("RUB") && !currency.equalsIgnoreCase("USD")) {
            log.info("Currency not supported, skip it");
            return null;
        }

        return instrumentRepository.findByFigi(figi)
                .map(dbInstrument -> {
                    if (!dbInstrument.getName().equals(name)) {
                        dbInstrument.setName(name);
                        return instrumentRepository.save(dbInstrument);
                    } else {
                        return dbInstrument;
                    }
                })
                .orElseGet(() -> instrumentRepository
                        .save(new Instrument(type, ticker, name, Currencies.valueOf(currency.toUpperCase()), figi, exchange)));
    }
}
