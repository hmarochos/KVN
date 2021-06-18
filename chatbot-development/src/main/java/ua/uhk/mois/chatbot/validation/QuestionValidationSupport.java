package ua.uhk.mois.chatbot.validation;

import io.vavr.collection.CharSeq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @author KVN
 * @since 26.03.2021 19:47
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
class QuestionValidationSupport {

    private static final int QUESTION_MIN_LENGTH = 1;
    private static final int QUESTION_MAX_LENGTH = 250;

    private static final String QUESTION_REG_EXP_ALLOWED_CHARACTERS = "[\\p{L}\\d\\w?!,.+¨*\\\\/|\\-\\s(){}\\[\\]'\":]";
    private static final String QUESTION_REG_EXP = "^" + QUESTION_REG_EXP_ALLOWED_CHARACTERS + "{1," + QUESTION_MAX_LENGTH + "}$";

    static Validation<String, String> validateQuestion(String question) {
        log.info("Validation of question '{}'.", question);

        if (question == null || question.replaceAll("\\s", "").isEmpty() || question.trim().length() < QUESTION_MIN_LENGTH) {
            String message = String.format("Question must contain at least %s characters.", QUESTION_MIN_LENGTH);
            return Validation.invalid(message);
        }

        String tmpQuestion = question.trim();
        if (tmpQuestion.length() > QUESTION_MAX_LENGTH) {
            String message = String.format("Question can contain up to %s characters.", QUESTION_MAX_LENGTH);
            return Validation.invalid(message);
        }

        if (!tmpQuestion.matches(QUESTION_REG_EXP)) {
            return CharSeq.of(tmpQuestion)
                          .replaceAll(QUESTION_REG_EXP_ALLOWED_CHARACTERS, "")
                          .transform(seq -> seq.isEmpty()
                                            ? Validation.invalid("The syntax of a question should match, for example, a sentence or a question, etc. It can contain uppercase and lowercase letters, numbers, underscores, hyphens, and other sentences that can be used in the sentence.")
                                            : Validation.invalid(String.format("Question contains invalid characters: '%s'.", seq.distinct().sorted())));
        }

        return Validation.valid(tmpQuestion);
    }
}
