package ru.isg.invest.helper.dto.tinkoff;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by s.ivanov on 20.11.2021.
 */
@Setter
@Getter
@ToString(callSuper = true)
public class InstrumentsResponse {
    private String trackingId;
    private InstrumentsPayload payload;
    private String status;
}
