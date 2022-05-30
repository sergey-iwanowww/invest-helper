package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.isg.invest.helper.services.IdeaTriggerVisitor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static lombok.AccessLevel.PROTECTED;
import static ru.isg.invest.helper.model.IdeaTriggerStatuses.ACTIVATED;
import static ru.isg.invest.helper.model.IdeaTriggerStatuses.WAITING_FOR_ACTIVATION;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Entity
@Accessors(chain = true)
@Getter
@NoArgsConstructor(access = PROTECTED)
@DiscriminatorValue("DATE")
public class DateIdeaTrigger extends IdeaTrigger {

    public DateIdeaTrigger(LocalDateTime date) {
        this.date = date;
    }

    @Column(nullable = false)
    private LocalDateTime date;

    @Override
    public void acceptVisitor(IdeaTriggerVisitor ideaTriggerVisitor) {
        ideaTriggerVisitor.visitDataIdeaTrigger(this);
    }

    public void activate(Consumer<Idea> ideaConsumer) {

        checkState(this.status == WAITING_FOR_ACTIVATION,
                "Неподходящий статус для активации: %s", this.status);

        this.activatedDate = LocalDateTime.now();
        this.status = ACTIVATED;

        ideaConsumer.accept(this.idea);
    }
}
