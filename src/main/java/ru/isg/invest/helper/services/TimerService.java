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

    private final MonitoredInstrumentsChecker monitoredInstrumentsChecker;
    private final CandlesImportTasksChecker candlesImportTasksChecker;
    private final IdeasChecker ideasChecker;

//    @Scheduled(cron = "")
    public void checkMonitoredCandles() {
        log.info("Запуск проверки отслеживаемых инструментов");
        monitoredInstrumentsChecker.check();
    }

//    @Scheduled(cron = "")
    public void checkCandlesImportTasks() {
        log.info("Запуск проверки задач на импорт свечей");
        candlesImportTasksChecker.check();
    }

    //    @Scheduled(cron = "")
    public void checkIdeas() {
        log.info("Запуск проверки идей");
        ideasChecker.check();
    }
}
