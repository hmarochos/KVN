package ua.uhk.mois.chatbot.validation;

import ua.uhk.mois.chatbot.support.TextTestSupport;
import io.vavr.control.Validation;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class QuestionValidationSupportTest {

    @Test
    void validateQuestionHds() {
        log.info("Question validation test.");

        // Data preparation
        String question = "Hi, how are you ? . (1 + 2) * [3 / 4] {|} 123 - _ ! ěČ:";

        // Execution
        Validation<String, String> validation = QuestionValidationSupport.validateQuestion(question);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(question, validation.get());
    }

    @Test
    void validateQuestionNullOrEmptyOrMinLength() {
        log.info("Question validation test. Validation of null value and empty values (white characters).");

        // Data preparation
        String question2 = "             ";
        String question3 = "X";

        // Execution
        Validation<String, String> validation1 = QuestionValidationSupport.validateQuestion(null);
        Validation<String, String> validation2 = QuestionValidationSupport.validateQuestion(question2);
        Validation<String, String> validation3 = QuestionValidationSupport.validateQuestion(question3);

        // Verification
        assertTrue(validation1.isInvalid());
        assertTrue(validation2.isInvalid());

        assertTrue(validation3.isValid());
        assertEquals(question3, validation3.get());

        String expectedMessage = String.format("Question must contain at least %s characters.", 1);
        assertEquals(expectedMessage, validation1.getError());
        assertEquals(expectedMessage, validation2.getError());
    }

    @Test
    void validateQuestionMaxLength() {
        log.info("Question validation test. Maximal character validation test.");

        // Data preparation
        String question = TextTestSupport.generateInvalidText(250L, "Some long question?");

        // Execution
        Validation<String, String> validation = QuestionValidationSupport.validateQuestion(question);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = String.format("Question can contain up to %s characters.", 250);
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateQuestionInvalidCharacters() {
        log.info("Question validation test. Validation test of prohibited (/ disallowed) characters");

        // Data preparation
        String question = ";`~`;°¨=ˇp%§<> ";

        // Execution
        Validation<String, String> validation = QuestionValidationSupport.validateQuestion(question);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = String.format("Question contains invalid characters: '%s'.", "%;<=>`~§°");
        assertEquals(expectedMessage, validation.getError());
    }
}
