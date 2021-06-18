package cz.uhk.mois.financialplanning.validation.wish;

import cz.uhk.mois.financialplanning.configuration.TextTestSupport;
import cz.uhk.mois.financialplanning.model.entity.wish.Currency;
import io.vavr.control.Validation;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class WishValidationSupportTest {

    @Test
    void validateId_Hds() {
        log.info("Validation test id. Test valid numbers and null values.");

        // Data preparation
        // Execution
        Validation<String, Long> validationNumber = WishValidationSupport.validateId(0L);
        Validation<String, Long> validationNull = WishValidationSupport.validateId(null);

        // Verification
        assertTrue(validationNumber.isValid());
        assertEquals(0L, validationNumber.get());

        assertTrue(validationNull.isValid());
        assertNull(validationNull.get());
    }

    @Test
    void validateId_NegativeNumber() {
        log.info("Validation test id. Negative number test.");

        // Data preparation
        // Execution
        Validation<String, Long> validation = WishValidationSupport.validateId(-1L);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Id must be a positive number.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validatePrice_Hds() {
        log.info("Price validation test.");

        // Data preparation
        // Execution
        BigDecimal price = BigDecimal.valueOf(1L);
        Validation<String, BigDecimal> validation = WishValidationSupport.validatePrice(price);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(0, price.compareTo(validation.get()));
    }

    @Test
    void validatePrice_Null() {
        log.info("Price validation test. Price will not be specified (null).");

        // Data preparation
        // Execution
        Validation<String, BigDecimal> validation = WishValidationSupport.validatePrice(null);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Price not specified.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validatePrice_NegativeNumber() {
        log.info("Price validation test. A negative number will be entered.");

        // Data preparation
        // Execution
        Validation<String, BigDecimal> validation = WishValidationSupport.validatePrice(BigDecimal.valueOf(-1L));

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "The price must be a positive number.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateCurrency_Hds() {
        log.info("Currency validation test.");

        // Data preparation
        // Execution
        Validation<String, Currency> validation = WishValidationSupport.validateCurrency(Currency.CZK);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(Currency.CZK, validation.get());
    }

    @Test
    void validateCurrency_Null() {
        log.info("Currency validation test. A null value will be given.");

        // Data preparation
        // Execution
        Validation<String, Currency> validation = WishValidationSupport.validateCurrency(null);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(Currency.CZK, validation.get());
    }

    @Test
    void validateName_Hds() {
        log.info("Test of validation of wish name (/ goal).");

        // Data preparation
        String name = "Some valid name.";

        // Execution
        Validation<String, String> validation = WishValidationSupport.validateName(name);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(name, validation.get());
    }

    @Test
    void validateName_NullOrEmptyOrMinLength() {
        log.info("Test of validation of wish name (/ goal). Validation test for null value, blank value (white characters) or insufficient number of characters.");

        // Data preparation
        // Execution
        Validation<String, String> validationNull = WishValidationSupport.validateName(null);
        Validation<String, String> validationEmpty = WishValidationSupport.validateName("      ");
        Validation<String, String> validationMinLength = WishValidationSupport.validateName("X");

        // Verification
        assertTrue(validationNull.isInvalid());
        assertTrue(validationEmpty.isInvalid());
        assertTrue(validationMinLength.isInvalid());

        String expectedMessage = String.format("Name must contain at least %s characters.", 2);

        assertEquals(expectedMessage, validationNull.getError());
        assertEquals(expectedMessage, validationEmpty.getError());
        assertEquals(expectedMessage, validationMinLength.getError());
    }

    @Test
    void validateName_MaxLength() {
        log.info("Test of validation of wish name (/ goal). Validation violation test exceeded the maximum number of characters allowed.");

        // Data preparation
        String name = TextTestSupport.generateInvalidText(50L, "Some name.");

        // Execution
        Validation<String, String> validation = WishValidationSupport.validateName(name);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = String.format("Name can contain up to %s characters.", 50);
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateName_InvalidSyntax() {
        log.info("Test of validation of wish name (/ goal). Validation violation test for non-compliance with the allowed syntax (the beginning of the name should begin with a letter or number, or an underscore.");

        // Data preparation
        // Execution
        Validation<String, String> validation = WishValidationSupport.validateName("-123 ěščřĚŠČŘ   Invalid Start.");

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "The name should start with a letter or number and can contain uppercase and lowercase letters with or without diacritics, numbers, hyphens, underscores, and similar characters to write a sentence.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateName_InvalidCharacters() {
        log.info("Test of validation of wish name (/ goal). Validation violation test in the presence of illegal characters.");

        // Data preparation
        // Execution
        Validation<String, String> validation = WishValidationSupport.validateName("¨´= _-ŠČ §'\"|/:;`~");

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = String.format("The name contains illegal characters: '%s'.", "\"'/:;=`|~§¨´");
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateDescription_Hds() {
        log.info("Test of validation of wish description.");

        // Data preparation
        String description = "Some valid wish description.";

        // Execution
        Validation<String, String> validation = WishValidationSupport.validateDescription(description);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(description, validation.get());
    }

    @Test
    void validateDescription_Null() {
        log.info("Test of validation of wish description.");

        // Data preparation
        // Execution
        Validation<String, String> validation = WishValidationSupport.validateDescription(null);

        // Verification
        assertTrue(validation.isValid());
        assertNull(validation.get());
    }

    @Test
    void validateDescription_Empty() {
        log.info("Test of validation of wish description.");

        // Data preparation
        // Execution
        Validation<String, String> validation = WishValidationSupport.validateDescription("         ");

        // Verification
        assertTrue(validation.isValid());
        assertNull(validation.get());
    }

    @Test
    void validateDescription_MaxLength() {
        log.info("Test of validation of wish description.");

        // Data preparation
        String description = TextTestSupport.generateInvalidText(500L, "Some wish description.");

        // Execution
        Validation<String, String> validation = WishValidationSupport.validateDescription(description);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = String.format("Description can contain up to %s characters.", 500);
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateDescription_InvalidCharacters() {
        log.info("Test of validation of wish description.");

        // Data preparation
        // Execution
        Validation<String, String> validation = WishValidationSupport.validateDescription("¨´=()[]{}ú==-\\|/'\";°`=-");

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = String.format("The description contains illegal characters: '%s'.", "\"'()/;=[\\]`{|}¨°´");
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validatePriority_Hds() {
        log.info("Priority validation test.");

        // Data preparation
        // Execution
        Validation<String, Integer> validation = WishValidationSupport.validatePriority(1);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(1, validation.get());
    }

    @Test
    void validatePriorityNull() {
        log.info("Priority validation test. Priority will not be specified (null).");

        // Data preparation
        // Execution
        Validation<String, Integer> validation = WishValidationSupport.validatePriority(null);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Priority not specified.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validatePriority_NegativeNumber() {
        log.info("Priority validation test. A negative number will be specified.");

        // Data preparation
        // Execution
        Validation<String, Integer> validation = WishValidationSupport.validatePriority(-1);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Priority must be a positive number.";
        assertEquals(expectedMessage, validation.getError());
    }
}
