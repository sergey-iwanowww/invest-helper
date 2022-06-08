package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.model.Idea;
import ru.isg.invest.helper.repositories.IdeaRepository;
import ru.isg.invest.helper.repositories.TelegramUserRepository;
import ru.isg.invest.helper.services.telegram.TelegramBotService;

import java.util.UUID;

import static ru.isg.invest.helper.model.TelegramUserStatus.APPROVED;

/**
 * Created by s.ivanov on 09.06.2022.
 */
@Service
@RequiredArgsConstructor
public class Notifier {

    private final IdeaRepository ideaRepository;
    private final TelegramUserRepository telegramUserRepository;
    private final TelegramBotService telegramBotService;

    public void notifyUsersAboutIdeaActivation(UUID ideaId) {

        Idea idea = ideaRepository.findById(ideaId).get();

        String conceptTypeStr = switch (idea.getConceptType()) {
            case DIVIDENDS -> "дивиденды";
            case RISE -> "лонг";
            case FALL -> "шорт";
        };

        telegramUserRepository.findTelegramUsersByStatus(APPROVED).forEach(u -> {

            String text = String.format("""
                            Идея: %s, %s
                            Дата / цена активации: %s / %s
                            Автор: %s (%s) от %s
                            """,
                    idea.getInstrument().getTicker(), conceptTypeStr,
                    idea.getActivatedDate(), idea.getActivatedPrice(),
                    idea.getAuthor().getName(), idea.getSource().getName(), idea.getGeneratedDate());

            telegramBotService.sendMessage(u.getChatId(), text);
        });
    }

    public void notifyUsersAboutIdeaFinishing(UUID ideaId) {

        Idea idea = ideaRepository.findById(ideaId).get();

        String conceptTypeStr = switch (idea.getConceptType()) {
            case DIVIDENDS -> "дивиденды";
            case RISE -> "лонг";
            case FALL -> "шорт";
        };

        telegramUserRepository.findTelegramUsersByStatus(APPROVED).forEach(u -> {

            String text = String.format("""
                            Идея реализована: %s, %s
                            Дата / цена активации: %s / %s
                            Автор: %s (%s) от %s
                            """,
                    idea.getInstrument().getTicker(), conceptTypeStr,
                    idea.getActivatedDate(), idea.getActivatedPrice(),
                    idea.getAuthor().getName(), idea.getSource().getName(), idea.getGeneratedDate());

            telegramBotService.sendMessage(u.getChatId(), text);
        });
    }
}
