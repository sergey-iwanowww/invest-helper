package ru.isg.invest.helper.exceptions;

/**
 * Created by s.ivanov on 08.06.2022.
 */
public class TelegramUserOnModerationYetException extends TelegramUserException {
    public TelegramUserOnModerationYetException(String userFirstName, String userLastName, long userId) {
        super(userFirstName, userLastName, userId);
    }
}
