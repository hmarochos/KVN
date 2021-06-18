package ua.uhk.mois.chatbot.validation;

import ua.uhk.mois.chatbot.dto.QuestionDtoIn;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @author KVN
 * @since 26.03.2021 19:44
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class QuestionDtoInValidator {

    public static Validation<String, QuestionDtoIn> validate(QuestionDtoIn dtoIn) {
        log.info("Validate question. {}", dtoIn);
        return QuestionValidationSupport.validateQuestion(dtoIn.getQuestion())
                                        .mapError(validationViolation -> validationViolation)
                                        .map(QuestionDtoIn::new);
    }
}
