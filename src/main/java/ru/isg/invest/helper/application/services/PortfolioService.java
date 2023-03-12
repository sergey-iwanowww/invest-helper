package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.application.dtos.PortfolioDto;
import ru.isg.invest.helper.domain.model.Brokers;
import ru.isg.invest.helper.domain.model.Portfolio;
import ru.isg.invest.helper.infrastructure.repositories.PortfolioRepository;

import java.util.UUID;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public Portfolio createPortfolio(Brokers broker, String name) {
        Portfolio portfolio = new Portfolio()
                .setBroker(broker)
                .setName(name);
        return portfolioRepository.save(portfolio);
    }

    public PortfolioDto getPortfolio(UUID portfolioId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);
        return portfolioToDto(portfolio);
    }

    private PortfolioDto portfolioToDto(Portfolio portfolio) {

        PortfolioDto portfolioDto = new PortfolioDto()
                .setBroker(portfolio.getBroker())
                .setId(portfolio.getId())
                .setName(portfolio.getName());

        return portfolioDto;
    }

    public Portfolio getPortfolioEntity(UUID portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow();
    }
}
