package ru.isg.invest.helper.exceptions;

/**
 * Created by s.ivanov on 14.11.2021.
 */
public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String pMessage) {
        super(pMessage);
    }
}
