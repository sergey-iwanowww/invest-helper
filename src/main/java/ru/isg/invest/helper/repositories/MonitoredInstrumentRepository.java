package ru.isg.invest.helper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.model.MonitoredCandle;

import java.util.UUID;

/**
 * Created by s.ivanov on 25.05.2022.
 */
public interface MonitoredInstrumentRepository extends JpaRepository<MonitoredCandle, UUID> {

}
