package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Created by s.ivanov on 04.12.2021.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TimerService {

    private final MonitoredInstrumentsChecker monitoredInstrumentsChecker;

    public void checkMonitoredInstruments() {

        log.info("Запуск проверки отслеживаемых инструментов");

        monitoredInstrumentsChecker.check();
    }
}
