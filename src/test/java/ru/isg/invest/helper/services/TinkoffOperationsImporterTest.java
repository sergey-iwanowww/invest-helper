package ru.isg.invest.helper.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Created by s.ivanov on 13.06.2022.
 */
@SpringBootTest
@Slf4j
public class TinkoffOperationsImporterTest {

    @Autowired
    private TinkoffOperationsImporter tinkoffOperationsImporter;

    @Test
    public void test() {
        tinkoffOperationsImporter.importOperations();
    }
}