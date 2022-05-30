package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.isg.invest.helper.services.IdeaTriggerVisitor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.InheritanceType.SINGLE_TABLE;
import static lombok.AccessLevel.PROTECTED;
import static ru.isg.invest.helper.model.IdeaTriggerStatuses.NEW;
import static ru.isg.invest.helper.model.IdeaTriggerStatuses.WAITING_FOR_ACTIVATION;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Entity
@Table(name = "idea_triggers")
@Accessors(chain = true)
@Getter
@Inheritance(strategy = SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@NoArgsConstructor(access = PROTECTED)
public abstract class IdeaTrigger {

    @Id
    protected UUID id = UUID.randomUUID();

    @Setter
    @OneToOne
    @JoinColumn(name = "idea_id", nullable = false)
    protected Idea idea;

    @Column(nullable = false)
    @Enumerated(STRING)
    protected IdeaTriggerStatuses status = NEW;

    protected LocalDateTime waitingForActivationSettedDate;

    protected LocalDateTime preactivatedDate;

    protected LocalDateTime activatedDate;

    public abstract void acceptVisitor(IdeaTriggerVisitor ideaTriggerVisitor);

    public void setWaitingForActivation() {
        checkState(this.status == NEW,
                "Неподходящий статус для перевода в статус ожидания активации: %s", this.status);
        this.waitingForActivationSettedDate = LocalDateTime.now();
        this.status = WAITING_FOR_ACTIVATION;
    }
}
