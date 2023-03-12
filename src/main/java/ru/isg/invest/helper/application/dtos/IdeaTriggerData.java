package ru.isg.invest.helper.application.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;
import ru.isg.invest.helper.domain.model.TimeFrames;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class IdeaTriggerData {

    public IdeaTriggerData(LocalDateTime date, TimeFrames monitoringTimeFrame) {
        this.date = date;
        this.monitoringTimeFrame = monitoringTimeFrame;
    }

    public IdeaTriggerData(BigDecimal price, TimeFrames monitoringTimeFrame) {
        this.price = price;
        this.monitoringTimeFrame = monitoringTimeFrame;
    }

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime date;

    BigDecimal price;
    BigDecimal delta;
    Boolean withRetest;
    TimeFrames monitoringTimeFrame;
}
