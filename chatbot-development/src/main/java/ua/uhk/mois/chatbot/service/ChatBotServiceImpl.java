package ua.uhk.mois.chatbot.service;

import ua.uhk.mois.chatbot.dto.QuestionDtoIn;
import ua.uhk.mois.chatbot.dto.QuestionDtoOut;
import ua.uhk.mois.chatbot.model.Chat;
import ua.uhk.mois.chatbot.response.Failure;
import ua.uhk.mois.chatbot.response.Success;
import ua.uhk.mois.chatbot.validation.QuestionDtoInValidator;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author KVN
 * @since 26.03.2021 20:12
 */

@Component
@Log4j2
public class ChatBotServiceImpl implements ChatBotService {

    private final Chat chatSession;

    public ChatBotServiceImpl(Chat chatSession) {
        this.chatSession = chatSession;
    }

    private static Failure getAnswerValidationFailure(String validationViolation, QuestionDtoIn dtoIn) {
        String logMessage = String.format("The question %s does not match the required syntax. %s", dtoIn, validationViolation);
        log.error(logMessage);
        return Failure.createFailure(HttpStatus.UNPROCESSABLE_ENTITY, validationViolation);
    }

    private static Failure getAnswerFromChatBotFailure(Throwable throwable, QuestionDtoIn dtoIn) {
        String logMessage = String.format("There was an error asking the chat bot the question. %s", dtoIn);
        log.error(logMessage, throwable);
        String message = "There was an error asking the chat bot the question.";
        return Failure.createFailure(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    private static Success<QuestionDtoOut> getAnswerSuccess(QuestionDtoOut dtoOut) {
        log.info("Chat bot has answered the question. {}", dtoOut);
        return Success.<QuestionDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    @Override
    public Either<Failure, Success<QuestionDtoOut>> getAnswer(QuestionDtoIn dtoIn) {
        log.info("Question on chat bot. {}", dtoIn);
        return QuestionDtoInValidator.validate(dtoIn)
                                     .mapError(validationViolation -> getAnswerValidationFailure(validationViolation, dtoIn))
                                     .toEither()
                                     .flatMap(this::getAnswerFromChatBot)
                                     .map(answer -> QuestionDtoOut.builder()
                                                                  .answer(answer)
                                                                  .build())
                                     .map(ChatBotServiceImpl::getAnswerSuccess);
    }

    private Either<Failure, String> getAnswerFromChatBot(QuestionDtoIn dtoIn) {
        log.info("Asking a chat bot a question. {}", dtoIn);
        return Try.of(() -> chatSession.multiSentenceRespond(dtoIn.getQuestion()))
                  .toEither()
                  .mapLeft(throwable -> getAnswerFromChatBotFailure(throwable, dtoIn));
    }
}
