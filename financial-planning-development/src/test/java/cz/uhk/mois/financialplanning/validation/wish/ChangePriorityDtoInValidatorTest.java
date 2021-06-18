package cz.uhk.mois.financialplanning.validation.wish;

import cz.uhk.mois.financialplanning.model.dto.wish.ChangePriorityDtoIn;
import cz.uhk.mois.financialplanning.model.dto.wish.ChangePriorityWishInfo;
import cz.uhk.mois.financialplanning.validation.ValidationSupport;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cz.uhk.mois.financialplanning.configuration.WishSupport.createChangePriorityWishInfo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class ChangePriorityDtoInValidatorTest {

    private static void assertChangePriorityDtoInValidation(ChangePriorityDtoIn dtoIn, String expectedMessage) {
        // Execution
        Validation<Seq<String>, ChangePriorityDtoIn> validation = ChangePriorityDtoInValidator.validate(dtoIn);

        // Verification
        assertTrue(validation.isInvalid());

        String validationViolations = ValidationSupport.removeListText(validation.getError().toString());
        assertEquals(expectedMessage, validationViolations);
    }

    private static ChangePriorityDtoIn createChangePriorityDtoIn(int count) {
        ChangePriorityDtoIn dtoIn = new ChangePriorityDtoIn();
        dtoIn.setChangePriorityWishInfoList(createChangePriorityWishInfoList(count));
        return dtoIn;
    }

    private static List<ChangePriorityWishInfo> createChangePriorityWishInfoList(int count) {
        return IntStream.range(0, count)
                        .mapToObj(i -> createChangePriorityWishInfo(i + 1L, i + 1))
                        .collect(Collectors.toList());
    }

    @Test
    void validate_Hds() {
        log.info("DtoIn validation test for priority change wishes.");

        // Data preparation
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(3);

        // Execution
        Validation<Seq<String>, ChangePriorityDtoIn> validation = ChangePriorityDtoInValidator.validate(dtoIn);

        // Verification
        assertTrue(validation.isValid());
        assertEquals(dtoIn.getChangePriorityWishInfoList(), validation.get().getChangePriorityWishInfoList());
    }

    @Test
    void validate_EmptyWishList() {
        log.info("DtoIn validation test for priority change wishes. The list of wishes to adjust the priority will not be listed (will be empty).");

        // Data preparation
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(0);

        // Execution
        // Verification
        String expectedMessage = "There is no information about wishes to change priority.";
        assertChangePriorityDtoInValidation(dtoIn, expectedMessage);
    }

    @Test
    void validate_NullWishList() {
        log.info("DtoIn validation test for priority change wishes. The list of customized wishes will not be listed (will be null).");

        // Data preparation
        ChangePriorityDtoIn dtoIn = new ChangePriorityDtoIn();

        // Execution
        // Verification
        String expectedMessage = "There is no information about wishes to change priority.";
        assertChangePriorityDtoInValidation(dtoIn, expectedMessage);
    }

    @Test
    void validate_InvalidWishListSize() {
        log.info("DtoIn validation test for priority change wishes. The number of priority-adjusted wishes will exceed the maximum number of items allowed in the list.");

        // Data preparation
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(10);

        // Execution
        // Verification
        String expectedMessage = String.format("The maximum allowed number of changes in wishes priorities is %s.", 9);
        assertChangePriorityDtoInValidation(dtoIn, expectedMessage);
    }

    @Test
    void validate_DuplicatePriorities() {
        log.info("DtoIn validation test for priority change wishes. There will be duplicate priorities in the wish list.");

        // Data preparation
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(3);
        dtoIn.getChangePriorityWishInfoList().parallelStream()
             .forEach(changePriorityWishInfo -> changePriorityWishInfo.setPriority(1));

        // Execution
        // Verification
        String expectedMessage = "Duplicate priorities found.";
        assertChangePriorityDtoInValidation(dtoIn, expectedMessage);
    }

    @Test
    void validate_DuplicateIds() {
        log.info("DtoIn validation test for priority change wishes. There will be duplicate ids in the wish priority adjustment list.");

        // Data preparation
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(3);
        dtoIn.getChangePriorityWishInfoList().parallelStream()
             .forEach(changePriorityWishInfo -> changePriorityWishInfo.setId(1L));

        // Execution
        // Verification
        String expectedMessage = "Duplicate id found.";
        assertChangePriorityDtoInValidation(dtoIn, expectedMessage);
    }

    @Test
    void validate_InvalidName() {
        log.info("DtoIn validation test for priority change wishes. There will be an item with an invalid wish name in the priority list.");

        // Data preparation
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(3);
        String emptyName = "     ";
        dtoIn.getChangePriorityWishInfoList().get(2).setName(emptyName);

        // Execution
        // Verification
        String validationViolation = String.format("Name must contain at least %s characters.", 2);
        String expectedMessage = String.format("The change for wish '%s' is not valid. %s", emptyName, validationViolation);
        assertChangePriorityDtoInValidation(dtoIn, expectedMessage);
    }

    @Test
    void validate_InvalidPriority() {
        log.info("DtoIn validation test for priority change wishes. There will be an invalid priority item in the priority adjustment list.");

        // Data preparation
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(5);
        dtoIn.getChangePriorityWishInfoList().get(3).setPriority(null);

        // Execution
        // Verification
        String validationViolation = "Priority not specified.";
        String expectedMessage = String.format("The change for wish '%s' is not valid. %s", "ChangePriorityWishInfo 4", validationViolation);
        assertChangePriorityDtoInValidation(dtoIn, expectedMessage);
    }

    @Test
    void validate_InvalidId_Negative() {
        log.info("DtoIn validation test for priority change wishes. There will be an invalid id in the list of priority adjustments.");

        // Data preparation
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(4);
        dtoIn.getChangePriorityWishInfoList().get(3).setId(-5L);

        // Execution
        // Verification
        String validationViolation = "Id must be a positive number.";
        String expectedMessage = String.format("The change for wish '%s' is not valid. %s", "ChangePriorityWishInfo 4", validationViolation);
        assertChangePriorityDtoInValidation(dtoIn, expectedMessage);
    }

    @Test
    void validate_InvalidId_Null() {
        log.info("DtoIn validation test for priority change wishes. There will be an invalid id in the list of priority adjustments.");

        // Data preparation
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(4);
        dtoIn.getChangePriorityWishInfoList().get(3).setId(null);

        // Execution
        // Verification
        String validationViolation = "Id not specified.";
        String expectedMessage = String.format("The change for wish '%s' is not valid. %s", "ChangePriorityWishInfo 4", validationViolation);
        assertChangePriorityDtoInValidation(dtoIn, expectedMessage);
    }
}
