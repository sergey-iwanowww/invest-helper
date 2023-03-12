package ru.isg.invest.helper.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.domain.model.TradingDay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by s.ivanov on 10.06.2022.
 */
public interface TradingDayRepository extends JpaRepository<TradingDay, UUID> {

    Optional<TradingDay> findTradingDayByExchangeAndDate(String exchange, LocalDate date);

    List<TradingDay> findTradingDayByExchangeAndDateGreaterThanEqualAndDateLessThanOrderByDate(String exchange,
            LocalDate dateFrom, LocalDate dateto);
}
