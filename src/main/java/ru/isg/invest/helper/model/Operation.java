package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

/**
 * Created by s.ivanov on 13.06.2022.
 */
@Entity
@Table(name = "operations")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Operation {

    public Operation(Portfolio portfolio, Instrument instrument, String type, LocalDateTime date, long count,
            BigDecimal payment, Currencies currency, String externalId) {
        this.portfolio = portfolio;
        this.instrument = instrument;
        this.type = type;
        this.date = date;
        this.payment = payment;
        this.currency = currency;
        this.externalId = externalId;
        this.count = count;
    }

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private BigDecimal payment;

    @Column(nullable = false)
    @Enumerated(STRING)
    private Currencies currency;

    @Column(nullable = false)
    private String externalId;

    @Column(nullable = false)
    private long count;

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
}
