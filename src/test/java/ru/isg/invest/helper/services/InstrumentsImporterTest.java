package ru.isg.invest.helper.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.isg.invest.helper.application.services.TinkoffInstrumentsImporter;

/**
 * Created by s.ivanov on 12.06.2022.
 */
@Disabled
@SpringBootTest
@Slf4j
public class InstrumentsImporterTest {

    @Autowired
    private TinkoffInstrumentsImporter tinkoffInstrumentsImporter;

    @Test
    public void test() {
        tinkoffInstrumentsImporter.importInstruments();
    }
}