package ua.uhk.mois.chatbot.service;

import ua.uhk.mois.chatbot.dto.QuestionDtoIn;
import ua.uhk.mois.chatbot.dto.QuestionDtoOut;
import ua.uhk.mois.chatbot.response.Failure;
import ua.uhk.mois.chatbot.response.Success;
import io.vavr.control.Either;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@WebAppConfiguration
@Log4j2
class ChatBotServiceImplTest {

    @Autowired
    private ChatBotServiceImpl chatBotService;

    @Test
    void getAnswerHds() {
        log.info("Test to get a response from chat bot.");

        // Data preparation
        QuestionDtoIn dtoIn = new QuestionDtoIn();
        dtoIn.setQuestion("Hi, how are you?");

        // Execution
        Either<Failure, Success<QuestionDtoOut>> answer = chatBotService.getAnswer(dtoIn);

        // Verification
        assertTrue(answer.isRight());
        assertEquals(HttpStatus.OK, answer.get().getHttpStatus());

        QuestionDtoOut dtoOut = answer.get().getBody();
        assertNotNull(dtoOut);
        assertNotNull(dtoOut.getAnswer());
        assertFalse(dtoOut.getAnswer().isEmpty());

        log.info("Response from chat bot: {}", dtoOut.getAnswer());
    }

    @Test
    void getAnswerInvalidDtoIn() {
        log.info("Test to get a response from chat bot. Input dtoIn will not be specified in valid syntax (input validation violation test).");

        // Data preparation
        QuestionDtoIn dtoIn = new QuestionDtoIn();

        // Execution
        Either<Failure, Success<QuestionDtoOut>> answer = chatBotService.getAnswer(dtoIn);

        // Verification
        assertTrue(answer.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, answer.getLeft().getHttpStatus());

        String expectedMessage = "Question must contain at least 1 characters.";
        assertEquals(expectedMessage, answer.getLeft().getMessage());
    }
}
