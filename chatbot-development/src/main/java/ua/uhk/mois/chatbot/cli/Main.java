package ua.uhk.mois.chatbot.cli;

import ua.uhk.mois.chatbot.model.Bot;
import ua.uhk.mois.chatbot.model.Chat;
import ua.uhk.mois.chatbot.utils.IOUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Main {

    //    public static final String BOT_NAME = "alice2";
    public static final String BOT_NAME = "super";

    public static void main(String[] args) {
        String path = System.getProperty("user.dir");
        String action = "chat";

        Bot bot = new Bot(BOT_NAME, path, action);
        bot.brain.nodeStats();

        Chat chatSession = new Chat(bot);

        while (true) {
            log.debug("Human: ");
            String textLine = IOUtils.readInputTextLine();

            if (textLine == null || textLine.equals("q")) {
                bot.writeQuit();
                System.exit(0);
            }

            String response = chatSession.multiSentenceRespond(textLine);
            log.debug(BOT_NAME + ": '{}'", response);
        }
    }
}
