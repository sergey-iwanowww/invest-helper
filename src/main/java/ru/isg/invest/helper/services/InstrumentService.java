package ru.isg.invest.helper.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.dto.InstrumentDto;
import ru.isg.invest.helper.dto.tinkoff.InstrumentsResponse;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.model.InstrumentTypes;
import ru.isg.invest.helper.model.Markets;
import ru.isg.invest.helper.model.Sectors;
import ru.isg.invest.helper.model.TradingModes;
import ru.isg.invest.helper.repositories.InstrumentRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.isg.invest.helper.dto.tinkoff.InstrumentTypes.Bond;
import static ru.isg.invest.helper.dto.tinkoff.InstrumentTypes.Currency;
import static ru.isg.invest.helper.dto.tinkoff.InstrumentTypes.Etf;
import static ru.isg.invest.helper.dto.tinkoff.InstrumentTypes.Stock;
import static ru.isg.invest.helper.model.InstrumentTypes.BOND;
import static ru.isg.invest.helper.model.InstrumentTypes.CURRENCY;
import static ru.isg.invest.helper.model.InstrumentTypes.FUND;
import static ru.isg.invest.helper.model.InstrumentTypes.STOCK;
import static ru.isg.invest.helper.model.Markets.MOEX;
import static ru.isg.invest.helper.model.Markets.SPBEXCHANGE;
import static ru.isg.invest.helper.model.TradingModes.T0;
import static ru.isg.invest.helper.model.TradingModes.T1;
import static ru.isg.invest.helper.model.TradingModes.T2;

/**
 * Created by s.ivanov on 24.11.2021.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public Instrument createInstrument(InstrumentTypes type, String ticker, String name, Markets market,
            TradingModes tradingMode, String currencyCode, String figi, Sectors sector) {

        Instrument instrument = new Instrument()
                .setType(type)
                .setTicker(ticker)
                .setName(name)
                .setMarket(market)
                .setTradingMode(tradingMode)
                .setCurrencyCode(currencyCode)
                .setFigi(figi)
                .setSector(sector);

        return instrumentRepository.save(instrument);
    }

    public List<InstrumentDto> listInstruments() {
        return instrumentRepository.findAll().stream()
                .map(this::instrumentToDto)
                .collect(Collectors.toList());
    }

    public InstrumentDto instrumentToDto(Instrument instrument) {
        return new InstrumentDto()
                .setFigi(instrument.getFigi())
                .setCurrencyCode(instrument.getCurrencyCode())
                .setId(instrument.getId())
                .setName(instrument.getName())
                .setType(instrument.getType())
                .setMarket(instrument.getMarket())
                .setTicker(instrument.getTicker())
                .setTradingMode(instrument.getTradingMode())
                .setSector(instrument.getSector());
    }

    public void loadInstrumentsFromTinkoffJson(InputStream is) throws IOException {

        InstrumentsResponse response;
        try {
            ObjectMapper om = new ObjectMapper();
            response = om.readValue(is, InstrumentsResponse.class);
        }
        finally {
            try {
                is.close();
            } catch (Exception e) {}
        }

        if (response == null || response.getPayload().getInstruments() == null
                || response.getPayload().getInstruments().size() == 0) {
            throw new IllegalStateException("Ошибка парсинга файла");
        }

        response.getPayload().getInstruments().forEach(this::processInstrument);
    }

    private void processInstrument(ru.isg.invest.helper.dto.tinkoff.Instrument tinkoffInstrument) {

        Instrument instrument = findEqualInstrument(tinkoffInstrument);

        if (instrument != null) {
            return;
        }

        instrument = new Instrument()
                .setTradingMode(defineTradingMode(tinkoffInstrument))
                .setType(defineType(tinkoffInstrument))
                .setFigi(tinkoffInstrument.getFigi())
                .setMarket(defineMarket(tinkoffInstrument))
                .setName(tinkoffInstrument.getName())
                .setSector(Sectors.OTHER)
                .setCurrencyCode(tinkoffInstrument.getCurrency())
                .setTicker(tinkoffInstrument.getTicker());

        instrumentRepository.save(instrument);
    }

    private TradingModes defineTradingMode(ru.isg.invest.helper.dto.tinkoff.Instrument tinkoffInstrument) {
        if (tinkoffInstrument.getType() == Bond || tinkoffInstrument.getType() == Etf) {
            return T1;
        } else if (tinkoffInstrument.getType() == Stock) {
            return T2;
        } else if (tinkoffInstrument.getType() == Currency) {
            return T0;
        } else {
            throw new IllegalArgumentException("Tinkoff instrument type not supported");
        }
    }

    private Markets defineMarket(ru.isg.invest.helper.dto.tinkoff.Instrument tinkoffInstrument) {
        if (tinkoffInstrument.getCurrency().equals("RUB")) {
            return MOEX;
        } else {
            return SPBEXCHANGE;
        }
    }

    private InstrumentTypes defineType(ru.isg.invest.helper.dto.tinkoff.Instrument tinkoffInstrument) {
        if (tinkoffInstrument.getType() == Bond) {
            return BOND;
        } else if (tinkoffInstrument.getType() == Etf) {
            return FUND;
        } else if (tinkoffInstrument.getType() == Stock) {
            return STOCK;
        } else if (tinkoffInstrument.getType() == Currency) {
            return CURRENCY;
        } else {
            throw new IllegalArgumentException("Tinkoff instrument type not supported");
        }
    }

    private Instrument findEqualInstrument(ru.isg.invest.helper.dto.tinkoff.Instrument tinkoffInstrument) {
        return instrumentRepository.findByTicker(tinkoffInstrument.getTicker()).orElse(null);
    }

    public Instrument getInstrument(UUID instrumentId) {
        return instrumentRepository.findById(instrumentId).orElseThrow();
    }
}
