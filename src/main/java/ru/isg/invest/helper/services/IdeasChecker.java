package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.isg.invest.helper.model.Candle;
import ru.isg.invest.helper.model.DateIdeaTrigger;
import ru.isg.invest.helper.model.Idea;
import ru.isg.invest.helper.model.IdeaConceptTypes;
import ru.isg.invest.helper.model.IdeaStatuses;
import ru.isg.invest.helper.model.IdeaTrigger;
import ru.isg.invest.helper.model.PriceIdeaTrigger;
import ru.isg.invest.helper.repositories.CandleRepository;
import ru.isg.invest.helper.repositories.IdeaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.isg.invest.helper.model.IdeaConceptTypes.FALL;
import static ru.isg.invest.helper.model.IdeaConceptTypes.RISE;
import static ru.isg.invest.helper.model.IdeaStatuses.ACTIVE;
import static ru.isg.invest.helper.model.IdeaTriggerStatuses.PRE_ACTIVATED;
import static ru.isg.invest.helper.model.IdeaTriggerStatuses.WAITING_FOR_ACTIVATION;
import static ru.isg.invest.helper.model.TimeFrames.ONE_HOUR;

/**
 * Created by s.ivanov on 29.05.2022.
 */
@Service
@RequiredArgsConstructor
public class IdeasChecker {

    private final IdeaRepository ideaRepository;
    private final CandleRepository candleRepository;

    @Transactional
    public void check() {
        ideaRepository.getIdeasByStatusIn(List.of(IdeaStatuses.WAITING_FOR_ACTIVATION, ACTIVE))
                .forEach(this::processIdea);
    }

    private void processIdea(Idea idea) {

        if (idea.getStatus() == IdeaStatuses.WAITING_FOR_ACTIVATION) {
            processWaitingForActivationIdea(idea);
        }

        if (idea.getStatus() == ACTIVE) {
            processActiveIdea(idea);
        }
    }

    private void processWaitingForActivationIdea(Idea idea) {

        IdeaTrigger startTrigger = idea.getStartTrigger();

        startTrigger.acceptVisitor(new IdeaTriggerVisitor() {
            @Override
            public void visitDataIdeaTrigger(DateIdeaTrigger dateIdeaTrigger) {
                processStartDateIdeaTrigger(dateIdeaTrigger);
            }
            @Override
            public void visitPriceIdeaTrigger(PriceIdeaTrigger priceIdeaTrigger) {
                processStartPriceIdeaTrigger(priceIdeaTrigger);
            }
        });
    }

    private void processStartDateIdeaTrigger(DateIdeaTrigger trigger) {

        // Активация идеи по дате: дата активации идеи == дате из триггера (если она наступила),
        // цена активации идеи == наиболее актуальной цене
        // (дата закрытия свеч или дата открытия свечи, независимо от того, была закрыта свеча или нет)
        // перед наступлением даты активации триггера.
        // Текущая дата может быть сильно больше даты активации,
        // поэтому при посылке сообщения необходимо на это указывать.

        Optional<BigDecimal> activatedPrice = candleRepository
                .getCandles1(trigger.getIdea().getInstrument().getId(), ONE_HOUR, trigger.getDate()).stream()
                .sorted(Comparator.comparing(Candle::getOpenDate).reversed())
                .limit(1)
                .findAny()
                .map(candle -> trigger.getDate().isAfter(candle.getCloseDate()) ? candle.getClose() : candle.getOpen());

        LocalDateTime curDate = LocalDateTime.now();
        if (curDate.compareTo(trigger.getDate()) >= 0) {
            trigger.activate(idea -> idea.activate(trigger.getDate(), activatedPrice.orElse(null)));
        }
    }

    private void processStartPriceIdeaTrigger(PriceIdeaTrigger trigger) {

        // Активация идеи по цене (если не нужен анализ на ретест): дата активации == дате закрытия последней свечи,
        // цена в которой стала больше или меньше установленного значения, цена активации == цене закрытия этой свечи.

        List<Candle> candles = candleRepository
                .getCandles2(trigger.getIdea().getInstrument().getId(), trigger.getMonitoringTimeFrame(),
                        trigger.getIdea().getGeneratedDate()).stream()
                .sorted(Comparator.comparing(Candle::getOpenDate))
                .collect(Collectors.toList());

        if (trigger.getStatus() == WAITING_FOR_ACTIVATION) {
            getCandleWherePriceCrossedTheValue(trigger.getIdea().getConceptType(), trigger.getPrice(), candles)
                    .ifPresent(candle -> {
                        if (trigger.isWithRetest()) {
                            trigger.preActivate();
                        } else {
                            trigger.activate(idea -> idea.activate(candle.getCloseDate(), candle.getClose()));
                        }
                    });
        } else if (trigger.getStatus() == PRE_ACTIVATED) {
            getCandleWherePriceRetestTheRange(trigger.getIdea().getConceptType(), trigger.getPrice(), trigger.getDelta(), candles)
                    .ifPresent(candle -> trigger.activate(idea -> idea.activate(candle.getCloseDate(), candle.getClose())));
        }
    }

    private Optional<Candle> getCandleWherePriceCrossedTheValue(IdeaConceptTypes conceptType, BigDecimal value,
            List<Candle> candles) {
        for (Candle candle : candles) {
            if (conceptType == RISE && candle.getClose().compareTo(value) >= 0
                    || conceptType == FALL && candle.getClose().compareTo(value) <= 0) {
                return Optional.of(candle);
            }
        }
        return Optional.empty();
    }

    private Optional<Candle> getCandleWherePriceRetestTheRange(IdeaConceptTypes conceptType, BigDecimal value,
            BigDecimal delta, List<Candle> candles) {

        var crossed = false;
        Candle crossedCandle = null;

        var rolledBack = false;

        for (Candle candle : candles) {
            if (rolledBack) {

                if (conceptType == RISE && candle.getClose().compareTo(value.add(delta)) >= 0
                        || conceptType == FALL && candle.getClose().compareTo(value.subtract(delta)) <= 0) {

                    // если в состоянии 'rolledBack' свеча пробила уровень значение+дельта, считаем, что ретест произошел

                    return Optional.of(candle);

                } else if (conceptType == RISE && candle.getClose().compareTo(crossedCandle.getOpen()) < 0
                        || conceptType == FALL && candle.getClose().compareTo(crossedCandle.getOpen()) > 0) {

                    // если в состоянии 'rolledBack' (а значит и 'crossed') цена опустилась ниже уровня открытия свечи, которая пробила уровень, начинаем сначала

                    crossed = false;
                    crossedCandle = null;
                }

            } else if (crossed) {

                if (conceptType == RISE && candle.getClose().compareTo(value.add(delta)) <= 0
                        || conceptType == FALL && candle.getClose().compareTo(value.subtract(delta)) >= 0) {

                    // если в состоянии 'crossed' цена откатилась в диапазон, переходим в состояние 'rolledBack'

                    rolledBack = true;

                } else if (conceptType == RISE && candle.getClose().compareTo(crossedCandle.getOpen()) < 0
                        || conceptType == FALL && candle.getClose().compareTo(crossedCandle.getOpen()) > 0) {

                    // если в состоянии 'crossed' цена опустилась ниже уровня открытия свечи, которая пробила уровень, начинаем сначала

                    crossed = false;
                    crossedCandle = null;
                }
            } else {

                if (conceptType == RISE && candle.getClose().compareTo(value) >= 0
                        || conceptType == FALL && candle.getClose().compareTo(value) <= 0) {

                    // если свеча пробила уровень, переходим в состояние 'crossed', запоминаем свечу

                    crossed = true;
                    crossedCandle = candle;
                }
            }
        }
        return Optional.empty();
    }

    private void processActiveIdea(Idea idea) {

        IdeaTrigger finishTrigger = idea.getFinishTrigger();
        if (finishTrigger != null) {
            finishTrigger.acceptVisitor(new IdeaTriggerVisitor() {
                @Override
                public void visitDataIdeaTrigger(DateIdeaTrigger dateIdeaTrigger) {
                    processFinishDateIdeaTrigger(dateIdeaTrigger);
                }

                @Override
                public void visitPriceIdeaTrigger(PriceIdeaTrigger priceIdeaTrigger) {
                    processFinishPriceIdeaTrigger(priceIdeaTrigger);
                }
            });
        }
    }

    private void processFinishDateIdeaTrigger(DateIdeaTrigger trigger) {

        Optional<BigDecimal> activatedPrice = candleRepository
                .getCandles1(trigger.getIdea().getInstrument().getId(), ONE_HOUR, trigger.getDate()).stream()
                .sorted(Comparator.comparing(Candle::getOpenDate).reversed())
                .limit(1)
                .findAny()
                .map(candle -> trigger.getDate().isAfter(candle.getCloseDate()) ? candle.getClose() : candle.getOpen());

        LocalDateTime curDate = LocalDateTime.now();
        if (curDate.compareTo(trigger.getDate()) >= 0) {
            trigger.activate(idea -> idea.finish(trigger.getDate(), activatedPrice.orElse(null)));
        }
    }

    private void processFinishPriceIdeaTrigger(PriceIdeaTrigger trigger) {

        // Активация идеи по цене (если не нужен анализ на ретест): дата активации == дате закрытия последней свечи,
        // цена в которой стала больше или меньше установленного значения, цена активации == цене закрытия этой свечи.

        List<Candle> candles = candleRepository
                .getCandles2(trigger.getIdea().getInstrument().getId(), trigger.getMonitoringTimeFrame(),
                        trigger.getIdea().getGeneratedDate()).stream()
                .sorted(Comparator.comparing(Candle::getOpenDate))
                .collect(Collectors.toList());

        if (trigger.getStatus() == WAITING_FOR_ACTIVATION) {
            getCandleWherePriceCrossedTheValue(trigger.getIdea().getConceptType(), trigger.getPrice(), candles)
                    .ifPresent(candle -> {
                        trigger.activate(idea -> idea.activate(candle.getCloseDate(), candle.getClose()));
                    });
        }
    }
}
