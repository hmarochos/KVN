package cz.uhk.mois.financialplanning.validation.transaction;

import cz.uhk.mois.financialplanning.configuration.TextTestSupport;
import cz.uhk.mois.financialplanning.model.dto.transaction.AdditionalInfoDomestic;
import cz.uhk.mois.financialplanning.model.dto.transaction.Direction;
import cz.uhk.mois.financialplanning.model.dto.transaction.Value;
import cz.uhk.mois.financialplanning.model.dto.transaction.bank.TransactionType;
import cz.uhk.mois.financialplanning.model.entity.wish.Currency;
import io.vavr.control.Validation;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class TransactionValidationSupportTest {

    private static void assertInvalidValue(Value value, String expectedMessage) {
        // Execution
        Validation<String, Value> validation = TransactionValidationSupport.validateValue(value);

        // Verification
        assertTrue(validation.isInvalid());
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateAccountId_Hds() {
        log.info("Account number validation test.");

        // Data preparation
        Long accountId = 123L;

        // Execution
        Validation<String, Long> validation = TransactionValidationSupport.validateAccountId(accountId);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(accountId, validation.get());
    }

    @Test
    void validateAccountId_Null() {
        log.info("Account number validation test. The account number will not be specified, it will be null.");

        // Data preparation
        // Execution
        Validation<String, Long> validation = TransactionValidationSupport.validateAccountId(null);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Account number is not specified.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateAccountId_NegativeNumber() {
        log.info("Account number validation test. The account number will be a negative (invalid) number.");

        // Data preparation
        Long accountId = -456L;

        // Execution
        Validation<String, Long> validation = TransactionValidationSupport.validateAccountId(accountId);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Account number must be a positive number greater than zero.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateValue_Hds() {
        log.info("Value object validation test to specify the amount for money transfer.");

        // Data preparation
        Value value = new Value(BigDecimal.valueOf(1000L), Currency.CZK);

        // Execution
        Validation<String, Value> validation = TransactionValidationSupport.validateValue(value);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(value, validation.get());
    }

    @Test
    void validateValue_Null() {
        log.info("Value object validation test to specify the amount for money transfer. Value will not be specified (will be null).");

        // Data preparation
        // Execution
        // Verification
        String expectedMessage = "Financial information is not specified.";
        assertInvalidValue(null, expectedMessage);
    }

    @Test
    void validateValue_AmountIsNull() {
        log.info("Value object validation test to specify the amount for money transfer. The amount to be transferred will not be shown.");

        // Data preparation
        Value value = new Value(null, Currency.CZK);

        // Execution
        // Verification
        String expectedMessage = "Transfer amount not specified.";
        assertInvalidValue(value, expectedMessage);
    }

    @Test
    void validateValue_InvalidAmount() {
        log.info("Value object validation test to specify the amount for money transfer. The amount to be converted will be a negative value.");

        // Data preparation
        Value value = new Value(BigDecimal.valueOf(-1000L), Currency.CZK);

        // Execution
        // Verification
        String expectedMessage = "Amount must be a positive number.";
        assertInvalidValue(value, expectedMessage);
    }

    @Test
    void validateValue_InvalidCurrency() {
        log.info("Value object validation test to specify the amount for money transfer. Currency will not be specified.");

        // Data preparation
        Value value = new Value(BigDecimal.valueOf(1000L), null);

        // Execution
        // Verification
        String expectedMessage = "Currency not specified.";
        assertInvalidValue(value, expectedMessage);
    }

    @Test
    void validateBankCode_Hds() {
        log.info("Bank code validation test.");

        // Data preparation
        Long bankCode = 1L;

        // Execution
        Validation<String, Long> validation = TransactionValidationSupport.validateBankCode(bankCode);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(bankCode, validation.get());
    }

    @Test
    void validateBankCode_Null() {
        log.info("Bank code validation test. Your bank code will not be specified.");

        // Data preparation
        // Execution
        Validation<String, Long> validation = TransactionValidationSupport.validateBankCode(null);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Bank code not specified.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateBankCode_NegativeNumber() {
        log.info("Bank code validation test. The bank code will be a negative number.");

        // Data preparation
        // Execution
        Validation<String, Long> validation = TransactionValidationSupport.validateBankCode(-1L);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Bank code must be a positive number.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validatePartyDescription_Hds() {
        log.info("Party description validation test.");

        // Data preparation
        String partyDescription = "Validation test 1, 2, 3. ? ! _-";

        // Execution
        Validation<String, String> validation = TransactionValidationSupport.validatePartyDescription(partyDescription);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(partyDescription, validation.get());
    }

    @Test
    void validatePartyDescription_Null() {
        log.info("Party description validation test. The description will not be listed (will be null).");

        // Data preparation
        // Execution
        Validation<String, String> validation = TransactionValidationSupport.validatePartyDescription(null);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Description not specified.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validatePartyDescription_Empty() {
        log.info("Party description validation test. The description will be blank (not specified).");

        // Data preparation
        // Execution
        Validation<String, String> validation = TransactionValidationSupport.validatePartyDescription("     ");

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Description not specified.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validatePartyDescription_MaxLength() {
        log.info("Party description validation test. The description will contain more characters than allowed.");

        // Data preparation
        String partyDescription = TextTestSupport.generateInvalidText(500L, "Party description.");

        // Execution
        Validation<String, String> validation = TransactionValidationSupport.validatePartyDescription(partyDescription);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = String.format("Description can contain up to %s characters.", 500);
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validatePartyDescription_InvalidCharacters() {
        log.info("Party description validation test. The description will contain prohibited characters.");

        // Data preparation
        String partyDescription = ";`~°<>_- §'\" 1 ()[]{} éíáý|/\\\\";

        // Execution
        Validation<String, String> validation = TransactionValidationSupport.validatePartyDescription(partyDescription);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = String.format("The party description contains illegal characters: '%s'.", "\"'()/;<>[\\]`{|}~§°");
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateDirection_Hds() {
        log.info("Test of transaction direction validation (money transfer).");

        // Data preparation
        // Execution
        Validation<String, Direction> validation = TransactionValidationSupport.validateDirection(Direction.INCOMING);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(Direction.INCOMING, validation.get());
    }

    @Test
    void validateDirection_Null() {
        log.info("Test of transaction direction validation (money transfer). Direction will not be given.");

        // Data preparation
        // Execution
        Validation<String, Direction> validation = TransactionValidationSupport.validateDirection(null);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Transaction direction not specified.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateTransactionType_Hds() {
        log.info("Transaction type validation test.");

        // Data preparation
        // Execution
        Validation<String, TransactionType> validation = TransactionValidationSupport.validateTransactionType(TransactionType.DIRECT_DEBIT);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(TransactionType.DIRECT_DEBIT, validation.get());
    }

    @Test
    void validateTransactionType_Null() {
        log.info("Transaction type validation test. Transaction type will not be specified (will be null).");

        // Data preparation
        // Execution
        Validation<String, TransactionType> validation = TransactionValidationSupport.validateTransactionType(null);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Transaction type not specified.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateAdditionalInfoDomestic_Hds() {
        log.info("Validation test for additional transaction information.");

        // Data preparation
        AdditionalInfoDomestic additionalInfoDomestic = new AdditionalInfoDomestic();
        additionalInfoDomestic.setConstantSymbol("123");
        additionalInfoDomestic.setVariableSymbol("123456789");
        additionalInfoDomestic.setSpecificSymbol("987654321");

        // Execution
        Validation<String, AdditionalInfoDomestic> validation = TransactionValidationSupport.validateAdditionalInfoDomestic(additionalInfoDomestic);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(additionalInfoDomestic, validation.get());
    }

    @Test
    void validateAdditionalInfoDomestic_Null() {
        log.info("Validation test for additional transaction information. Additional information will not be provided (will be null).");

        // Data preparation
        // Execution
        Validation<String, AdditionalInfoDomestic> validation = TransactionValidationSupport.validateAdditionalInfoDomestic(null);

        // Verification
        assertTrue(validation.isValid());
        assertNull(validation.get());
    }

    @Test
    void validateAdditionalInfoDomestic_NullValues() {
        log.info("Validation test for additional transaction information. All values inside the object with additional information will be null.");

        // Data preparation
        AdditionalInfoDomestic additionalInfoDomestic = new AdditionalInfoDomestic();

        // Execution
        Validation<String, AdditionalInfoDomestic> validation = TransactionValidationSupport.validateAdditionalInfoDomestic(additionalInfoDomestic);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(additionalInfoDomestic, validation.get());
    }

    @Test
    void validateAdditionalInfoDomestic_InvalidCharacters() {
        log.info("Validation test for additional transaction information. All values inside the object with additional information will be null.");

        // Data preparation
        AdditionalInfoDomestic additionalInfoDomestic = new AdditionalInfoDomestic();
        additionalInfoDomestic.setConstantSymbol("-123");
        additionalInfoDomestic.setVariableSymbol("abc _ -! ...");
        additionalInfoDomestic.setSpecificSymbol("éíáý");

        // Execution
        Validation<String, AdditionalInfoDomestic> validation = TransactionValidationSupport.validateAdditionalInfoDomestic(additionalInfoDomestic);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "The constant symbol can only be 1 - 10 numbers.";
        assertEquals(expectedMessage, validation.getError());
    }
}
