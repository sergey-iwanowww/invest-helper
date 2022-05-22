package ru.isg.invest.helper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.model.Portfolio;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by s.ivanov on 14.11.2021.
 */
public interface PortfolioRepository extends JpaRepository<Portfolio, UUID>{

    Optional<Portfolio> findByName(String name);
}
