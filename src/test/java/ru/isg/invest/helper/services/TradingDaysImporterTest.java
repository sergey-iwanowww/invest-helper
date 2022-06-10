package ru.isg.invest.helper.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Created by s.ivanov on 11.06.2022.
 */
@Disabled
@SpringBootTest
@Slf4j
public class TradingDaysImporterTest {

    @Autowired
    private TinkoffTradingDaysImporter tinkoffTradingDaysImporter;

    @Test
    public void test() {
        tinkoffTradingDaysImporter.importTradingDays();
    }
}