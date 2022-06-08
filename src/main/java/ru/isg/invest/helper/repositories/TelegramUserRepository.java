package ru.isg.invest.helper.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.isg.invest.helper.model.TelegramUser;
import ru.isg.invest.helper.model.TelegramUserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by s.ivanov on 08.06.2022.
 */
public interface TelegramUserRepository extends JpaRepository<TelegramUser, UUID> {

    Optional<TelegramUser> findTelegramUserByExternalId(long externalId);

    List<TelegramUser> findTelegramUsersByStatus(TelegramUserStatus status);
}
