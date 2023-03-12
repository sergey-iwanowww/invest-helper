package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by s.ivanov on 04.12.2021.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TimerService {

    private final MonitoredCandlesChecker monitoredCandlesChecker;
    private final CandlesImportTasksChecker candlesImportTasksChecker;
    private final IdeasChecker ideasChecker;
    private final TinkoffTradingDaysImporter tinkoffTradingDaysImporter;
    private final TinkoffInstrumentsImporter tinkoffInstrumentsImporter;
    private final TinkoffOperationsImporter tinkoffOperationsImporter;

    // TODO: Важно стартовать проверку наблюдаемых инструментов не сразу в начале 5-тиминутки, а чуть позже,
    // чтобы дать время на приход обновления по свече в стрим от tinkoff stream api.
    // Если этого не сделать, будут ситуации, когда свеча еще не поступила в стрим,
    // это будет расценено как отсутствие исторической свечи
    // и будет сгенерирована задача на обновление исторических данных.
    // Пока сделано для упрощения, в перспективе можно от этого уйти, при создании задач
    // проверяя, нет ли наблюдения за стримом свеч.
    @Scheduled(cron = "0 1/5 * * * *")
    public void checkMonitoredCandles() {
        monitoredCandlesChecker.check();
    }

    @Scheduled(cron = "10 1/5 * * * *")
    public void checkCandlesImportTasks() {
        candlesImportTasksChecker.check();
    }

    //    @Scheduled(cron = "")
    public void checkIdeas() {
        log.info("Запуск проверки идей");
        ideasChecker.check();
    }

    @Scheduled(cron = "0 0 */4 * * *")
    public void checkTradingDays() {
        tinkoffTradingDaysImporter.importTradingDays();
    }

    @Scheduled(cron = "0 5 */4 * * *")
    public void checkInstruments() {
        tinkoffInstrumentsImporter.importInstruments();
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void checkOperations() {
        tinkoffOperationsImporter.importOperations();
    }
}
