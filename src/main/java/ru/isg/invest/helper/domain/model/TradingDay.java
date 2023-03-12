package ru.isg.invest.helper.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

/**
 * Created by s.ivanov on 10.06.2022.
 */
@Entity
@Table(name = "trading_days")
@Getter
@Accessors(chain = true)
@NoArgsConstructor(access = PROTECTED)
public class TradingDay {

    public TradingDay(String exchange, LocalDate date, boolean tradingDay, LocalDateTime startDate,
            LocalDateTime endDate) {
        this.exchange = exchange;
        this.date = date;
        this.tradingDay = tradingDay;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    String exchange;

    @Column(nullable = false)
    LocalDate date;

    @Setter
    @Column(nullable = false)
    boolean tradingDay;

    @Setter
    @Column(nullable = false)
    LocalDateTime startDate;

    @Setter
    @Column(nullable = false)
    LocalDateTime endDate;
}
