package ru.isg.invest.helper.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.isg.invest.helper.application.dtos.CreateInstrumentRequest;
import ru.isg.invest.helper.application.dtos.InstrumentDto;
import ru.isg.invest.helper.infrastructure.repositories.InstrumentRepository;
import ru.isg.invest.helper.application.services.CandlesImporter;
import ru.isg.invest.helper.application.services.IdeasChecker;
import ru.isg.invest.helper.application.services.InstrumentService;

import javax.validation.Valid;
import java.util.List;

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
                createInstrumentRequest.getName(), createInstrumentRequest.getCurrencyCode(),
                createInstrumentRequest.getFigi());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<InstrumentDto>> listInstruments() {
        return ResponseEntity.ok(instrumentService.listInstruments());
    }

    @PostMapping(path = "/ideas")
    public ResponseEntity<Void> ideas() {

        ideasChecker.check();

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/fake")
    public ResponseEntity<Void> fake() {

        ideasChecker.fake();

        return ResponseEntity.ok().build();
    }
}
