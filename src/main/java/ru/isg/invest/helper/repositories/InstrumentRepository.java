package ru.isg.invest.helper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.model.Instrument;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by s.ivanov on 14.11.2021.
 */
public interface InstrumentRepository extends JpaRepository<Instrument, UUID> {

    Optional<Instrument> findByTicker(String ticker);

    Optional<Instrument> findByFigi(String figi);
}
