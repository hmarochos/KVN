package ua.uhk.mois.financialplanning.validation.pagination;

import io.vavr.control.Validation;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class PageAnsSortValidationSupportTest {

    private static final List<String> ALLOWED_VALUES = Collections.unmodifiableList(Arrays.asList("priority", "name"));

    @Test
    void validateOrder_Hds() {
        log.info("Item validation test for sorting.");

        // Data preparation
        // Execution
        Validation<String, String> validation1 = PageAnsSortValidationSupport.validateOrder("ASC");
        Validation<String, String> validation2 = PageAnsSortValidationSupport.validateOrder("desc");

        // Verification
        assertTrue(validation1.isValid());
        assertTrue(validation2.isValid());

        assertEquals("ASC", validation1.get());
        assertEquals("DESC", validation2.get());
    }

    @Test
    void validateOrder_EmptyOrNull() {
        log.info("Item validation test for sorting. Text null values and blank values (whitespace only).");

        // Data preparation
        // Execution
        Validation<String, String> validation1 = PageAnsSortValidationSupport.validateOrder(null);
        Validation<String, String> validation2 = PageAnsSortValidationSupport.validateOrder("     ");

        // Verification
        assertTrue(validation1.isInvalid());
        assertTrue(validation2.isInvalid());

        String expectedMessage = "The order of items in which they are to be sorted (ascending or descending) has not been defined.";
        assertEquals(expectedMessage, validation1.getError());
        assertEquals(expectedMessage, validation2.getError());
    }

    @Test
    void validateOrder_UnknownValue() {
        log.info("Item validation test for sorting. An unknown value will be entered.");

        // Data preparation
        // Execution
        Validation<String, String> validation = PageAnsSortValidationSupport.validateOrder("abc");

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Only 'ASC' and 'DESC' values are allowed to define ascending or descending order.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateOrderBy_Hds() {
        log.info("Validation test for sorting items by column.");

        // Data preparation
        String column = "priority";

        // Execution
        Validation<String, String> validation = PageAnsSortValidationSupport.validateOrderBy(column, ALLOWED_VALUES);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(column, validation.get());
    }

    @Test
    void validateOrderBy_EmptyOrNull() {
        log.info("Validation test for sorting items by column. Text null values and blank values (whitespace only).");

        // Data preparation
        // Execution
        Validation<String, String> validation1 = PageAnsSortValidationSupport.validateOrderBy("    ", ALLOWED_VALUES);
        Validation<String, String> validation2 = PageAnsSortValidationSupport.validateOrderBy(null, ALLOWED_VALUES);

        // Verification
        assertTrue(validation1.isInvalid());
        assertTrue(validation2.isInvalid());

        String expectedMessage = "No column to sort by has been defined.";
        assertEquals(expectedMessage, validation1.getError());
        assertEquals(expectedMessage, validation2.getError());
    }

    @Test
    void validateOrderBy_UnknownColumn() {
        log.info("Validation test for sorting items by column. An unknown column for sorting values will be specified.");

        // Data preparation
        // Execution
        Validation<String, String> validation = PageAnsSortValidationSupport.validateOrderBy("Unknown Column", ALLOWED_VALUES);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "The values can only be sorted by the following columns [priority, name].";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validatePage_Hds() {
        log.info("Page index validation test.");

        // Data preparation
        // Execution
        Validation<String, Integer> validation = PageAnsSortValidationSupport.validatePage(0);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(0, validation.get());
    }

    @Test
    void validatePage_Null() {
        log.info("Page index validation test. Page index will not be listed (will be null).");

        // Data preparation
        // Execution
        Validation<String, Integer> validation = PageAnsSortValidationSupport.validatePage(null);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Page index not specified.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validatePage_NegativeNumber() {
        log.info("Page index validation test. A negative page index will be specified.");

        // Data preparation
        // Execution
        Validation<String, Integer> validation = PageAnsSortValidationSupport.validatePage(-1);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "Page index must be zero or greater.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateSize_Hds() {
        log.info("Test validation of the number of items per page.");

        // Data preparation
        // Execution
        Validation<String, Integer> validation = PageAnsSortValidationSupport.validateSize(1);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(1, validation.get());
    }

    @Test
    void validateSize_Null() {
        log.info("Test validation of the number of items per page. The number of items on the page will not be specified (a null value will be specified).");

        // Data preparation
        // Execution
        Validation<String, Integer> validation = PageAnsSortValidationSupport.validateSize(null);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "The number of items per page was not specified.";
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    void validateSize_NegativeNumber() {
        log.info("Test validation of the number of items per page. A negative number of items per page will be entered.");

        // Data preparation
        // Execution
        Validation<String, Integer> validation = PageAnsSortValidationSupport.validateSize(-5);

        // Verification
        assertTrue(validation.isInvalid());

        String expectedMessage = "The number of items on the page must be zero or greater.";
        assertEquals(expectedMessage, validation.getError());
    }
}
