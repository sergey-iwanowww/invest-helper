package ru.isg.invest.helper.application.services;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by s.ivanov on 05.06.2022.
 */
@Value
public class TriggerActivationNeededResults {
    boolean activationNeeded;
    LocalDateTime date;
    BigDecimal price;
}
