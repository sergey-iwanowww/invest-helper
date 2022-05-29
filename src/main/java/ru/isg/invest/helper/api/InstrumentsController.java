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
import ru.isg.invest.helper.repositories.InstrumentRepository;
import ru.isg.invest.helper.services.CandlesImporter;
import ru.isg.invest.helper.services.IdeasChecker;
import ru.isg.invest.helper.services.InstrumentService;

import javax.validation.Valid;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static ru.isg.invest.helper.model.TimeFrames.FIVE_MINUTES;
import static ru.isg.invest.helper.model.TimeFrames.ONE_DAY;
import static ru.isg.invest.helper.model.TimeFrames.ONE_HOUR;
import static ru.isg.invest.helper.model.TimeFrames.ONE_MONTH;
import static ru.isg.invest.helper.model.TimeFrames.ONE_WEEK;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@RestController
@RequestMapping("/instruments")
public class InstrumentsController {

    @Autowired
    private InstrumentService instrumentService;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private CandlesImporter candlesImporter;

    @Autowired
    private IdeasChecker ideasChecker;

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

    @PostMapping(path = "/import")
    public ResponseEntity<Void> importInstruments() {

        candlesImporter.importCandles(instrumentRepository.findByTicker("SBER").get(), ONE_HOUR,
                LocalDateTime.of(2022, 5, 1, 0, 0, 0),
                LocalDateTime.of(2022, 6, 1, 0, 0, 0));

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/ideas")
    public ResponseEntity<Void> ideas() {

        ideasChecker.check();

        return ResponseEntity.ok().build();
    }
}
