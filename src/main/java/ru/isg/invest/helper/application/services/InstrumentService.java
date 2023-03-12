package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.application.dtos.InstrumentDto;
import ru.isg.invest.helper.domain.model.Currencies;
import ru.isg.invest.helper.domain.model.Instrument;
import ru.isg.invest.helper.domain.model.InstrumentTypes;
import ru.isg.invest.helper.infrastructure.repositories.InstrumentRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by s.ivanov on 24.11.2021.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public Instrument createInstrument(InstrumentTypes type, String ticker, String name, String currencyCode, String figi) {

        Instrument instrument = new Instrument(type, ticker, name, Currencies.valueOf(currencyCode), figi, "");

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
                .setCurrencyCode(instrument.getCurrency().name())
                .setId(instrument.getId())
                .setName(instrument.getName())
                .setType(instrument.getType())
                .setTicker(instrument.getTicker());
    }

    public Instrument getInstrument(UUID instrumentId) {
        return instrumentRepository.findById(instrumentId).orElseThrow();
    }
}
