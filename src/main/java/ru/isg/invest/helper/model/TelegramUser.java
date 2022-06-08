package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;
import static ru.isg.invest.helper.model.TelegramUserStatus.ON_MODERATION;

/**
 * Created by s.ivanov on 08.06.2022.
 */
@Entity
@Table(name = "telegram_users")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class TelegramUser {

    public TelegramUser(long externalId, long chatId, String lastName, String firstName) {
        this.externalId = externalId;
        this.chatId = chatId;
        this.lastName = lastName;
        this.firstName = firstName;
    }

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private long externalId;

    @Column(nullable = false)
    private long chatId;

    private String lastName;

    private String firstName;

    @Setter
    @Column(nullable = false)
    @Enumerated(STRING)
    private TelegramUserStatus status = ON_MODERATION;
}
