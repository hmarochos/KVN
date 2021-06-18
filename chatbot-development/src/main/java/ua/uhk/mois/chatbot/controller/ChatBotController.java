package ua.uhk.mois.chatbot.controller;

import ua.uhk.mois.chatbot.controller.path.UrlConstant;
import ua.uhk.mois.chatbot.dto.QuestionDtoIn;
import ua.uhk.mois.chatbot.dto.QuestionDtoOut;
import ua.uhk.mois.chatbot.response.ServerResponse;
import ua.uhk.mois.chatbot.service.ChatBotService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;

/**
 * @author KVN
 * @since 26.03.2021 9:40
 */

@RestController
@RequestMapping(UrlConstant.CHAT_BOT)
@Log4j2
public class ChatBotController {

    private final ChatBotService chatBotService;

    public ChatBotController(ChatBotService chatBotService) {
        this.chatBotService = chatBotService;
    }

    @PostMapping(path = UrlConstant.CHAT_BOT_QUESTION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<QuestionDtoOut>> question(@RequestBody QuestionDtoIn dtoIn) {
        log.info("Question on chat bot. {}", dtoIn);

        return chatBotService.getAnswer(dtoIn)
                             .mapLeft(failure -> new QuestionDtoOut().createResponse(failure.getHttpStatus(), failure.getMessage()))
                             .fold(Function.identity(), questionDtoOutSuccess -> new QuestionDtoOut().createResponse(questionDtoOutSuccess.getHttpStatus(), questionDtoOutSuccess.getBody()));
    }
}
