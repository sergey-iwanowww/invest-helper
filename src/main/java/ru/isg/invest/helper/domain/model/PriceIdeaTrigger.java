package ru.isg.invest.helper.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.isg.invest.helper.application.services.IdeaTriggerVisitor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkState;
import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;
import static ru.isg.invest.helper.domain.model.TimeFrames.ONE_HOUR;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Entity
@Accessors(chain = true)
@Getter
@NoArgsConstructor(access = PROTECTED)
@DiscriminatorValue("PRICE")
public class PriceIdeaTrigger extends IdeaTrigger {

    public PriceIdeaTrigger(BigDecimal price, BigDecimal delta, boolean withRetest, TimeFrames monitoringTimeFrame) {
        this.price = price;
        this.delta = delta != null ? delta : BigDecimal.ZERO;
        this.withRetest = withRetest;
        this.monitoringTimeFrame = monitoringTimeFrame != null ? monitoringTimeFrame : ONE_HOUR;
    }

    @Column
    private BigDecimal price;

    @Column
    private BigDecimal delta = BigDecimal.ZERO;

    @Column
    private boolean withRetest;

    @Column
    @Enumerated(STRING)
    private TimeFrames monitoringTimeFrame;

    @Override
    public void acceptVisitor(IdeaTriggerVisitor ideaTriggerVisitor) {
        ideaTriggerVisitor.visitPriceIdeaTrigger(this);
    }
}
