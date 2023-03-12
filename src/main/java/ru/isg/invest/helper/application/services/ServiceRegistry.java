package ru.isg.invest.helper.application.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.infrastructure.telegrambot.TelegramBotService;

import javax.annotation.PostConstruct;

/**
 * Created by s.ivanov on 08.06.2022.
 */
@Service
@RequiredArgsConstructor
public class ServiceRegistry {

    private static ServiceRegistry instance;

    private final TelegramBotService telegramBotService;
    private final IdeasService ideasService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static TelegramBotService getTelegramBotService() {
        return instance.telegramBotService;
    }

    public static IdeasService getIdeasService() {
        return instance.ideasService;
    }

    public static ApplicationEventPublisher getApplicationEventPublisher() {
        return instance.applicationEventPublisher;
    }
}
