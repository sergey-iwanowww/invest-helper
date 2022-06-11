package ru.isg.invest.helper.services;

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
    private final TinkoffInstrumentsImporter tinkoffinstrumentsImporter;

//    @Scheduled(cron = "0 */1 * * * *")
    public void checkMonitoredCandles() {
        monitoredCandlesChecker.check();
    }

    @Scheduled(cron = "0 */1 * * * *")
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
        tinkoffinstrumentsImporter.importInstruments();
    }
}
