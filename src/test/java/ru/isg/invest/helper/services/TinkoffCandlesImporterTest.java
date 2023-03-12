package ru.isg.invest.helper.services;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.isg.invest.helper.application.services.TinkoffCandlesImporter;
import ru.isg.invest.helper.domain.model.Instrument;
import ru.isg.invest.helper.infrastructure.repositories.InstrumentRepository;

import java.time.LocalDateTime;

import static java.time.Month.JUNE;
import static ru.isg.invest.helper.domain.model.TimeFrames.ONE_MONTH;

/**
 * Created by s.ivanov on 13.06.2022.
 */
@Disabled
@SpringBootTest
public class TinkoffCandlesImporterTest {

    @Autowired
    private TinkoffCandlesImporter tinkoffCandlesImporter;
    @Autowired
    private InstrumentRepository instrumentRepository;

    @Test
    public void test() {

        Instrument instrument = instrumentRepository.findByTicker("SBER").get();

        tinkoffCandlesImporter.importCandles(instrument, ONE_MONTH,
                LocalDateTime.of(2022, JUNE, 10, 0, 0), LocalDateTime.of(2022, JUNE, 12, 16, 0), false);
    }
}
