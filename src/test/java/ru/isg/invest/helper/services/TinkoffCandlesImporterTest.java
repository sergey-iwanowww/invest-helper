package ru.isg.invest.helper.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.repositories.InstrumentRepository;

import java.time.LocalDateTime;

import static java.time.Month.JUNE;
import static ru.isg.invest.helper.model.TimeFrames.ONE_MONTH;

/**
 * Created by s.ivanov on 13.06.2022.
 */
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
                LocalDateTime.of(2022, JUNE, 10, 0, 0), LocalDateTime.of(2022, JUNE, 12, 16, 0));
    }
}
