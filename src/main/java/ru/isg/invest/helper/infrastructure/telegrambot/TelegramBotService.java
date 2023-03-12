package ru.isg.invest.helper.infrastructure.telegrambot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.isg.invest.helper.application.dtos.StartTelegramCommand;
import ru.isg.invest.helper.application.exceptions.TelegramUserBlockedException;
import ru.isg.invest.helper.application.exceptions.TelegramUserOnModerationYetException;
import ru.isg.invest.helper.domain.model.TelegramUser;
import ru.isg.invest.helper.infrastructure.repositories.TelegramUserRepository;

import javax.annotation.PostConstruct;

/**
 * Created by s.ivanov on 08.06.2022.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {

    @Value("${telegram.bot.username}")
    private String username;

    @Value("${telegram.bot.token}")
    private String token;

    private final TelegramBotAuthService telegramBotAuthService;
    private final TelegramUserRepository telegramUserRepository;

    private InvestmentStorytellerBot investmentStorytellerBot;

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            investmentStorytellerBot = new InvestmentStorytellerBot(username, token);
            botsApi.registerBot(investmentStorytellerBot);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendMessage(long chatId, String text) {
        try {
            investmentStorytellerBot.execute(new SendMessage("" + chatId, text));
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void processUpdate(Update update) {

        try {
            telegramBotAuthService.checkAuth(update);
        } catch (TelegramUserOnModerationYetException e) {
            log.error("User {} ({}) on moderation yet", e.getUserName(), e.getUserId());
            sendMessage(update.getMessage().getChatId(), "Ожидайте, пожалуйста, в данный момент производится проверка прав доступа в чат с ботом");
            return;
        } catch (TelegramUserBlockedException e) {
            log.error("User {} ({}) blocked", e.getUserName(), e.getUserId());
            sendMessage(update.getMessage().getChatId(), "К сожалению, вы заблокированы в чате с ботом");
            return;
        }

        TelegramCommandParser.parseCommand(update.getMessage())
                .ifPresent(c -> {
                    if (c instanceof StartTelegramCommand startTelegramCommand) {
                        Message message = update.getMessage();
                        telegramUserRepository.findTelegramUserByExternalId(message.getFrom().getId())
                                .orElseGet(() -> telegramUserRepository.save(new TelegramUser(message.getFrom().getId(),
                                        message.getChatId(), message.getFrom().getLastName(), message.getFrom().getFirstName())));
                    }
                });
    }
}
