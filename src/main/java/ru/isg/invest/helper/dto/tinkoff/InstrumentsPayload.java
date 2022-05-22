package ru.isg.invest.helper.dto.tinkoff;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Created by s.ivanov on 20.11.2021.
 */
@Setter
@Getter
@ToString(callSuper = true)
public class InstrumentsPayload {
    private List<Instrument> instruments;
    private Integer total;
}
