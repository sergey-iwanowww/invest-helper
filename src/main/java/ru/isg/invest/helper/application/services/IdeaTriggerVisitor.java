package ru.isg.invest.helper.application.services;

import ru.isg.invest.helper.domain.model.DateIdeaTrigger;
import ru.isg.invest.helper.domain.model.PriceIdeaTrigger;

/**
 * Created by s.ivanov on 29.05.2022.
 */
public interface IdeaTriggerVisitor {

    default void visitDataIdeaTrigger(DateIdeaTrigger ideaTrigger) {
    }

    default void visitPriceIdeaTrigger(PriceIdeaTrigger ideaTrigger) {
    }
}
