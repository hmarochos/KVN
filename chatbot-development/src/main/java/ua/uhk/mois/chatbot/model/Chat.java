package ua.uhk.mois.chatbot.model;

import ua.uhk.mois.chatbot.utils.IOUtils;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedWriter;
import java.io.FileWriter;
/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org

        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.

        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.

        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/

/**
 * Class encapsulating a chat session between a bot and a client
 */

@Log4j2
public class Chat {

    private static final String AN_ERROR_OCCURRED = "An error occurred.";
    static String matchTrace = "";
    final History<History> thatHistory = new History<>("that");
    final History<String> requestHistory = new History<>("request");
    final History<String> responseHistory = new History<>("response");
    final History<String> inputHistory = new History<>("input");
    final Predicates predicates = new Predicates();
    Bot bot;
    String customerId;

    /**
     * Constructor  (defualt customer ID)
     *
     * @param bot
     *         the bot to chat with
     */
    public Chat(Bot bot) {
        this(bot, "0");
    }

    /**
     * Constructor
     *
     * @param bot
     *         bot to chat with
     * @param customerId
     *         unique customer identifier
     */
    public Chat(Bot bot, String customerId) {
        this.customerId = customerId;
        this.bot = bot;
        History<String> contextThatHistory = new History<>();
        contextThatHistory.add(MagicStrings.default_that);
        thatHistory.add(contextThatHistory);
        addPredicates();
        predicates.put("topic", MagicStrings.default_topic);
    }

    public static void setMatchTrace(String newMatchTrace) {
        matchTrace = newMatchTrace;
    }

    /**
     * Load all predicate defaults
     */
    void addPredicates() {
        try {
            predicates.getPredicateDefaults(ResourceFilePaths.CONFIG_ROOT_PATH + ResourceFilePaths.CONFIG_ROOT_PATH_PREDICATES);
        } catch (Exception ex) {
            log.error(AN_ERROR_OCCURRED, ex);
        }
    }

    /**
     * Chat session terminal interaction
     */
    public void chat() {
        String logFile = MagicStrings.log_path + "/log_" + customerId + ".txt";
        //Construct the bw object
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            String request = "SET PREDICATES";
            String response;
            while (!"quit".equals(request)) {
                log.info("Human: ");
                request = IOUtils.readInputTextLine();
                response = multiSentenceRespond(request);
                log.info("Robot: " + response);
                bw.write("Human: " + request);
                bw.newLine();
                bw.write("Robot: " + response);
                bw.newLine();
                bw.flush();
            }
        } catch (Exception ex) {
            log.error(AN_ERROR_OCCURRED, ex);
        }
    }

    /**
     * Return bot response to a single sentence input given conversation context
     *
     * @param input
     *         client input
     * @param that
     *         bot's last sentence
     * @param topic
     *         current topic
     * @param contextThatHistory
     *         history of "that" values for this request/response interaction
     *
     * @return bot's reply
     */
    String respond(String input, String that, String topic, History contextThatHistory) {
        String response;
        inputHistory.add(input);
        response = AIMLProcessor.respond(input, that, topic, this);
        String normResponse = bot.preProcessor.normalize(response);
        normResponse = JapaneseTokenizer.morphSentence(normResponse);
        String[] sentences = PreProcessor.sentenceSplit(normResponse);
        for (String sentence : sentences) {
            that = sentence;
            if (that.trim().isEmpty())
                that = MagicStrings.default_that;
            contextThatHistory.add(that);
        }
        return response.trim() + "  ";
    }

    /**
     * Return bot response given an input and a history of "that" for the current conversational interaction
     *
     * @param input
     *         client input
     * @param contextThatHistory
     *         history of "that" values for this request/response interaction
     *
     * @return bot's reply
     */
    String respond(String input, History<String> contextThatHistory) {
        History hist = thatHistory.get(0);
        String that;
        that = hist == null ? MagicStrings.default_that : hist.getString(0);
        return respond(input, that, predicates.get("topic"), contextThatHistory);
    }

    /**
     * return a compound response to a multiple-sentence request. "Multiple" means one or more.
     *
     * @param request
     *         client's multiple-sentence input
     *
     * @return
     */
    public String multiSentenceRespond(String request) {
        StringBuilder response = new StringBuilder();
        matchTrace = "";
        try {
            String norm = bot.preProcessor.normalize(request);
            norm = JapaneseTokenizer.morphSentence(norm);
            log.debug("normalized = " + norm);
            String[] sentences = PreProcessor.sentenceSplit(norm);
            History<String> contextThatHistory = new History<>("contextThat");
            for (String sentence : sentences) {
                AIMLProcessor.trace_count = 0;
                String reply = respond(sentence, contextThatHistory);
                response.append("  ").append(reply);
            }
            requestHistory.add(request);
            responseHistory.add(response.toString());
            thatHistory.add(contextThatHistory);
        } catch (Exception ex) {
            log.error(AN_ERROR_OCCURRED, ex);
            return MagicStrings.error_bot_response;
        }

        bot.writeLearnfIFCategories();
        return response.toString().trim();
    }
}
