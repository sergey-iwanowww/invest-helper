package ru.isg.invest.helper.services;

import ru.isg.invest.helper.model.DateIdeaTrigger;
import ru.isg.invest.helper.model.PriceIdeaTrigger;

/**
 * Created by s.ivanov on 29.05.2022.
 */
public interface IdeaTriggerVisitor {

    default void visitDataIdeaTrigger(DateIdeaTrigger ideaTrigger) {
    }

    default void visitPriceIdeaTrigger(PriceIdeaTrigger ideaTrigger) {
    }
}
