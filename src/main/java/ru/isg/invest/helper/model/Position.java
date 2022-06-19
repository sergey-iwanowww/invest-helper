package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

/**
 * Created by s.ivanov on 19.06.2022.
 */
@Entity
@Table(name = "positions")
@Getter
@NoArgsConstructor(access = PROTECTED)
@Accessors(chain = true)
public class Position {

    public Position(Instrument instrument, Portfolio portfolio, long balanceCount, BigDecimal balanceAverage,
            BigDecimal result, BigDecimal commission, BigDecimal dividends, BigDecimal dividendsTax,
            BigDecimal coupons, BigDecimal couponsTax) {
        this.instrument = instrument;
        this.portfolio = portfolio;
        this.balanceCount = balanceCount;
        this.balanceAverage = balanceAverage;
        this.result = result;
        this.commission = commission;
        this.dividends = dividends;
        this.dividendsTax = dividendsTax;
        this.coupons = coupons;
        this.couponsTax = couponsTax;
    }

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Setter
    @Column(nullable = false)
    private long balanceCount;

    @Setter
    @Column(nullable = false)
    private BigDecimal balanceAverage;

    @Setter
    @Column(nullable = false)
    private BigDecimal result;

    @Setter
    @Column(nullable = false)
    private BigDecimal commission;

    @Setter
    @Column(nullable = false)
    private BigDecimal dividends;

    @Setter
    @Column(nullable = false)
    private BigDecimal dividendsTax;

    @Setter
    @Column(nullable = false)
    private BigDecimal coupons;

    @Setter
    @Column(nullable = false)
    private BigDecimal couponsTax;
}
