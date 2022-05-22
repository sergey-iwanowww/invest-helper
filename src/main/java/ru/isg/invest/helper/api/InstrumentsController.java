package ru.isg.invest.helper.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.isg.invest.helper.dto.CreateInstrumentRequest;
import ru.isg.invest.helper.dto.InstrumentDto;
import ru.isg.invest.helper.services.InstrumentService;

import javax.validation.Valid;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@RestController
@RequestMapping("/instruments")
public class InstrumentsController {

    @Autowired
    private InstrumentService instrumentService;

    @PostMapping
    public ResponseEntity<Void> createInstrument(@RequestBody @Valid CreateInstrumentRequest createInstrumentRequest) {
        instrumentService.createInstrument(createInstrumentRequest.getType(), createInstrumentRequest.getTicker(),
                createInstrumentRequest.getName(), createInstrumentRequest.getMarket(),
                createInstrumentRequest.getTradingMode(), createInstrumentRequest.getCurrencyCode(),
                createInstrumentRequest.getFigi(), createInstrumentRequest.getSector());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<InstrumentDto>> listInstruments() {
        return ResponseEntity.ok(instrumentService.listInstruments());
    }

    @PostMapping(path = "/load")
    public ResponseEntity<Void> loadInstruments() throws IOException {

        instrumentService.loadInstrumentsFromTinkoffJson(new FileInputStream("/data/tinkoff-bonds.json"));
        instrumentService.loadInstrumentsFromTinkoffJson(new FileInputStream("/data/tinkoff-currencies.json"));
        instrumentService.loadInstrumentsFromTinkoffJson(new FileInputStream("/data/tinkoff-etfs.json"));
        instrumentService.loadInstrumentsFromTinkoffJson(new FileInputStream("/data/tinkoff-stocks.json"));

        return ResponseEntity.ok().build();
    }
}
