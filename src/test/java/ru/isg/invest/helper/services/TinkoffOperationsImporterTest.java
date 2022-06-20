package ru.isg.invest.helper.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.isg.invest.helper.model.Instrument;
import ru.isg.invest.helper.repositories.OperationRepository;
import ru.isg.invest.helper.repositories.PortfolioRepository;
import ru.isg.invest.helper.repositories.PositionRepository;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by s.ivanov on 13.06.2022.
 */
@SpringBootTest
@Slf4j
public class TinkoffOperationsImporterTest {

    @Autowired
    private TinkoffOperationsImporter tinkoffOperationsImporter;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Test
    public void test() {
        tinkoffOperationsImporter.importOperations();
    }

    @Test
    public void testRecalc() {
        positionRepository.deleteAll();

        Set<UUID> instrs = operationRepository.findByPortfolioId(UUID.fromString("1b385db2-e16f-440c-8601-ee87f3944350")).stream()
                .map(o -> o.getInstrument())
                .filter(Objects::nonNull)
                .map(Instrument::getId)
                .collect(Collectors.toSet());

        tinkoffOperationsImporter.recalcPositions(
                portfolioRepository.findById(UUID.fromString("1b385db2-e16f-440c-8601-ee87f3944350")).get(),
                instrs);
    }
}