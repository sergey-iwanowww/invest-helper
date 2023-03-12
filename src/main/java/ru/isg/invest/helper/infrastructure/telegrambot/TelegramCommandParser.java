package ru.isg.invest.helper.infrastructure.telegrambot;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.isg.invest.helper.application.dtos.StartTelegramCommand;
import ru.isg.invest.helper.application.dtos.TelegramCommand;

import java.util.Optional;

import static org.telegram.telegrambots.meta.api.objects.EntityType.BOTCOMMAND;

/**
 * Created by s.ivanov on 08.06.2022.
 */
public class TelegramCommandParser {

    public static Optional<TelegramCommand> parseCommand(Message message) {

        if (!message.isCommand() || message.getEntities() == null || message.getEntities().isEmpty()
            || !BOTCOMMAND.equals(message.getEntities().get(0).getType())) {
            return Optional.empty();
        }

        if ("/start".equalsIgnoreCase(message.getEntities().get(0).getText())) {
            return Optional.of(new StartTelegramCommand());
        } else {
            return Optional.empty();
        }
    }
}
