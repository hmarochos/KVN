package ua.uhk.mois.chatbot.service;

import ua.uhk.mois.chatbot.dto.QuestionDtoIn;
import ua.uhk.mois.chatbot.dto.QuestionDtoOut;
import ua.uhk.mois.chatbot.response.Failure;
import ua.uhk.mois.chatbot.response.Success;
import io.vavr.control.Either;

/**
 * @author KVN
 * @since 26.03.2021 20:09
 */

public interface ChatBotService {

    /**
     * Getting the answer to the question. <br/>
     * <i>Ask a chat bot</i>
     *
     * @param dtoIn
     *         input data with a question to chat bot
     *
     * @return right with the answer to the question or left with information about the error
     */
    Either<Failure, Success<QuestionDtoOut>> getAnswer(QuestionDtoIn dtoIn);
}
