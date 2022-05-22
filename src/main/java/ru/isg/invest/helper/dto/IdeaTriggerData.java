package ru.isg.invest.helper.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@Data
public class IdeaTriggerData {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate date;

    BigDecimal price;
    Boolean withRetest;
}
