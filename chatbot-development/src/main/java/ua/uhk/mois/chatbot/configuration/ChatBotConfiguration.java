package ua.uhk.mois.chatbot.configuration;

import ua.uhk.mois.chatbot.model.Bot;
import ua.uhk.mois.chatbot.model.Chat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure to set (/ create) a chat bot to be able to respond.
 *
 * @author KVN
 * @since 26.03.2021 9:46
 */

@Configuration
public class ChatBotConfiguration {

    @Value("${bot.name:super}")
    private String botName;

    @Bean
    public Chat chatSession() {
        String path = System.getProperty("user.dir");
        String action = "chat";

        Bot bot = new Bot(botName, path, action);
        bot.brain.nodeStats();

        return new Chat(bot);
    }
}
