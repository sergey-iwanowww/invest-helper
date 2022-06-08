package ru.isg.invest.helper.services.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.isg.invest.helper.dto.TelegramCommand;
import ru.isg.invest.helper.dto.StartTelegramCommand;
import ru.isg.invest.helper.exceptions.TelegramUserBlockedException;
import ru.isg.invest.helper.exceptions.TelegramUserOnModerationYetException;
import ru.isg.invest.helper.model.TelegramUser;
import ru.isg.invest.helper.model.TelegramUserStatus;
import ru.isg.invest.helper.repositories.TelegramUserRepository;

import java.util.Optional;

import static ru.isg.invest.helper.model.TelegramUserStatus.ON_MODERATION;

/**
 * Created by s.ivanov on 08.06.2022.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotAuthService {

    private final TelegramUserRepository telegramUserRepository;

    public void checkAuth(Update update) {

        User from = update.getMessage().getFrom();

        Optional<TelegramUser> telegramUserOpt = telegramUserRepository.findTelegramUserByExternalId(from.getId());
        if (telegramUserOpt.isPresent()) {
            TelegramUser telegramUser = telegramUserOpt.get();
            switch (telegramUser.getStatus()) {
                case ON_MODERATION -> throw new TelegramUserOnModerationYetException(
                        from.getFirstName(), from.getLastName(), from.getId());
                case BLOCKED -> throw new TelegramUserBlockedException(
                        from.getFirstName(), from.getLastName(), from.getId());
            }
        } else {
            Optional<TelegramCommand> telegramCommand = TelegramCommandParser.parseCommand(update.getMessage());
            if (telegramCommand.isEmpty() || !(telegramCommand.get() instanceof StartTelegramCommand)) {
                throw new IllegalStateException();
            }
        }
    }
}
