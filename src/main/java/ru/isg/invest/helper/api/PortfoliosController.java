package ru.isg.invest.helper.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.isg.invest.helper.application.dtos.CreatePortfolioRequest;
import ru.isg.invest.helper.application.dtos.PortfolioDto;
import ru.isg.invest.helper.application.services.PortfolioService;

import javax.validation.Valid;
import java.util.UUID;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@RestController
@RequestMapping("/portfolios")
public class PortfoliosController {

    @Autowired
    private PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<Void> createPortfolio(@RequestBody @Valid CreatePortfolioRequest createPortfolioRequest) {
        portfolioService.createPortfolio(createPortfolioRequest.getBroker(), createPortfolioRequest.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioDto> getPortfolio(@PathVariable("portfolioId") UUID portfolioId) {
        return ResponseEntity.ok(portfolioService.getPortfolio(portfolioId));
    }
}
