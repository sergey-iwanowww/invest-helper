package ru.isg.invest.helper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.model.TradingDay;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by s.ivanov on 10.06.2022.
 */
public interface TradingDayRepository extends JpaRepository<TradingDay, Integer> {

    Optional<TradingDay> findTradingDayByExchangeAndDate(String exchange, LocalDate date);
}
