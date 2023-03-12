package ru.isg.invest.helper.application.exceptions;

/**
 * Created by s.ivanov on 08.06.2022.
 */
public class TelegramUserBlockedException extends TelegramUserException {
    public TelegramUserBlockedException(String userFirstName, String userLastName, long userId) {
        super(userFirstName, userLastName, userId);
    }
}
