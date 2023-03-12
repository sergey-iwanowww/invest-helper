package ru.isg.invest.helper.infrastructure.telegrambot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isg.invest.helper.application.services.ServiceRegistry;

/**
 * Created by s.ivanov on 08.06.2022.
 */
@Slf4j
public class InvestmentStorytellerBot extends TelegramLongPollingBot {

    private String username;
    private String token;

    public InvestmentStorytellerBot(String username, String token) {
        this.username = username;
        this.token = token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onRegister() {
        log.info("Bot is registered");
    }

    @Override
    public void onUpdateReceived(Update update) {
        ServiceRegistry.getTelegramBotService().processUpdate(update);
    }
}
