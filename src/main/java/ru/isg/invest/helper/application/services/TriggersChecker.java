package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.domain.model.DateIdeaTrigger;
import ru.isg.invest.helper.domain.model.Idea;
import ru.isg.invest.helper.domain.model.PriceIdeaTrigger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static ru.isg.invest.helper.domain.model.IdeaConceptTypes.RISE;
import static ru.isg.invest.helper.domain.model.TimeFrames.ONE_HOUR;

/**
 * Created by s.ivanov on 29.05.2022.
 */
@Service
@RequiredArgsConstructor
public class TriggersChecker {

    private final CandlesAnalyzer candlesAnalyzer;

    public TriggerActivationNeededResults checkActivationNeeded(DateIdeaTrigger trigger) {

        // Активация идеи по дате: дата активации идеи == дате из триггера (если она наступила),
        // цена активации идеи == наиболее актуальной цене
        // (дата закрытия свеч или дата открытия свечи, независимо от того, была закрыта свеча или нет)
        // перед наступлением даты активации триггера.
        // Текущая дата может быть сильно больше даты активации,
        // поэтому при посылке сообщения необходимо на это указывать.

        Optional<BigDecimal> activatedPrice = candlesAnalyzer.getLastPrice(trigger.getIdea().getInstrument(),
                ONE_HOUR, trigger.getDate());

        if (LocalDateTime.now(UTC).compareTo(trigger.getDate()) >= 0) {
            return new TriggerActivationNeededResults(true, trigger.getDate(), activatedPrice.orElse(null));
        }

        return new TriggerActivationNeededResults(false, null, null);
    }

    public TriggerActivationNeededResults checkActivationNeeded(PriceIdeaTrigger trigger) {

        // Активация идеи по цене (если не нужен анализ на ретест): дата активации == дате закрытия последней свечи,
        // цена в которой стала больше или меньше установленного значения, цена активации == цене закрытия этой свечи.

        Idea idea = trigger.getIdea();

        if (trigger.isWithRetest()) {
            PriceCrossedTheValueWithRetestCheckingResults checkingResults = candlesAnalyzer
                    .checkPriceCrossedTheValueWithRetest(idea.getInstrument(), trigger.getMonitoringTimeFrame(),
                            idea.getGeneratedDate(), LocalDateTime.now(UTC), idea.getConceptType() == RISE,
                            trigger.getPrice(), trigger.getDelta());
            if (checkingResults.getConfirmCandle() != null) {
                return new TriggerActivationNeededResults(true, checkingResults.getConfirmCandle().getCloseDate(),
                        checkingResults.getConfirmCandle().getClose());
            }
        } else {
            PriceCrossedTheValueCheckingResults checkingResults = candlesAnalyzer
                    .checkPriceCrossedTheValue(idea.getInstrument(), trigger.getMonitoringTimeFrame(),
                            idea.getGeneratedDate(), LocalDateTime.now(UTC), idea.getConceptType() == RISE,
                            trigger.getPrice());
            if (checkingResults.getCrossCandle() != null) {
                return new TriggerActivationNeededResults(true, checkingResults.getCrossCandle().getCloseDate(),
                        checkingResults.getCrossCandle().getClose());
            }
        }

        return new TriggerActivationNeededResults(false, null, null);
    }
}
