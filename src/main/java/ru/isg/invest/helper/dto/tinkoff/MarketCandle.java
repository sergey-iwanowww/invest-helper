package ru.isg.invest.helper.dto.tinkoff;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by s.ivanov on 20.11.2021.
 */
@Setter
@Getter
@ToString(callSuper = true)
public class MarketCandle {
    private BigDecimal o;
    private BigDecimal c;
    private BigDecimal h;
    private BigDecimal l;
    private BigDecimal v;
    private LocalDateTime time;
    private String interval;
    private String figi;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarketCandle that = (MarketCandle) o;

        if (!time.equals(that.time)) return false;
        if (!interval.equals(that.interval)) return false;
        return figi.equals(that.figi);
    }

    @Override
    public int hashCode() {
        int result = time.hashCode();
        result = 31 * result + interval.hashCode();
        result = 31 * result + figi.hashCode();
        return result;
    }
}
