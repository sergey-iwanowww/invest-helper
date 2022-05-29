package ru.isg.invest.helper.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.isg.invest.helper.model.TimeFrames;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@Data
public class IdeaTriggerData {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime date;

    BigDecimal price;
    BigDecimal delta;
    Boolean withRetest;
    TimeFrames monitoringTimeFrame;
}
