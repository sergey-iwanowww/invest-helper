package ru.isg.invest.helper.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;
import ru.isg.invest.helper.model.IdeaTriggerStatuses;
import ru.isg.invest.helper.model.TimeFrames;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by s.ivanov on 22.05.2022.
 */
@Data
@Accessors(chain = true)
public class IdeaTriggerDto {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime date;

    BigDecimal price;
    BigDecimal delta;
    Boolean withRetest;
    TimeFrames monitoringTimeFrame;

    IdeaTriggerStatuses status;

    LocalDateTime waitingForActivationSettedDate;
    LocalDateTime activatedDate;
}
