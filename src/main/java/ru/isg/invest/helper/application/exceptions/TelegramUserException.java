package ru.isg.invest.helper.application.exceptions;

import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * Created by s.ivanov on 08.06.2022.
 */
@Getter
public class TelegramUserException extends RuntimeException {

    private String userFirstName;
    private String userLastName;
    private long userId;

    public TelegramUserException(String userFirstName, String userLastName, long userId) {
        super();
        this.userLastName = userLastName;
        this.userFirstName = userFirstName;
        this.userId = userId;
    }

    public String getUserName() {
        return (StringUtils.hasText(userFirstName) ? userFirstName : " ") +
                (StringUtils.hasText(userLastName) ? userLastName : "");
    }
}
