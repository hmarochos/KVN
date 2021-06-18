package ua.uhk.mois.financialplanning.validation.wish;

import ua.uhk.mois.financialplanning.model.entity.wish.Currency;
import io.vavr.collection.CharSeq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author KVN
 * @since 23.03.2021 17:20
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class WishValidationSupport {

    private static final int NAME_MIN_LENGTH = 2;
    private static final int NAME_MAX_LENGTH = 50;
    private static final String NAME_REG_EXP = "^[\\p{L}\\w]+[\\p{L}\\w\\s,.?!_\\-]{" + NAME_MIN_LENGTH + "," + NAME_MAX_LENGTH + "}$";
    private static final String NAME_REG_EXP_ALLOWED_CHARACTERS = "[\\p{L}\\w\\s,.?!_\\-]";

    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final String DESCRIPTION_REG_EXP = "^[\\p{L}\\w\\s,.?!_\\-]{0," + DESCRIPTION_MAX_LENGTH + "}$";
    private static final String DESCRIPTION_REG_EXP_ALLOWED_CHARACTERS = "[\\p{L}\\w\\s,.?!_\\-]";

    static Validation<String, Long> validateId(Long id) {
        if (id == null) {
            return Validation.valid(null);
        }
        if (id < 0) {
            return Validation.invalid("Id must be a positive number.");
        }
        return Validation.valid(id);
    }

    static Validation<String, BigDecimal> validatePrice(BigDecimal price) {
        if (price == null) {
            return Validation.invalid("Price not specified.");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return Validation.invalid("The price must be a positive number.");
        }
        return Validation.valid(price);
    }

    static Validation<String, Currency> validateCurrency(Currency currency) {
        return Validation.valid(Currency.uaK);
    }

    static Validation<String, String> validateName(String name) {
        if (name == null || name.replaceAll("\\s", "").isEmpty() || name.trim().length() < NAME_MIN_LENGTH) {
            String message = String.format("Name must contain at least %s characters.", NAME_MIN_LENGTH);
            return Validation.invalid(message);
        }
        String tmpName = name.trim();
        if (tmpName.length() > NAME_MAX_LENGTH) {
            String message = String.format("Name can contain up to %s characters.", NAME_MAX_LENGTH);
            return Validation.invalid(message);
        }
        if (!tmpName.matches(NAME_REG_EXP)) {
            return CharSeq.of(tmpName)
                          .replaceAll(NAME_REG_EXP_ALLOWED_CHARACTERS, "")
                          .transform(seq -> seq.isEmpty()
                                            ? Validation.invalid("The name should start with a letter or number and can contain uppercase and lowercase letters with or without diacritics, numbers, hyphens, underscores, and similar characters to write a sentence.")
                                            : Validation.invalid(String.format("The name contains illegal characters: '%s'.", seq.distinct().sorted())));
        }
        return Validation.valid(tmpName);
    }

    static Validation<String, String> validateDescription(String description) {
        if (description == null) {
            return Validation.valid(null);
        }
        if (description.replaceAll("\\s", "").isEmpty()) {
            return Validation.valid(null);
        }

        String tmpDescription = description.trim();
        if (tmpDescription.length() > DESCRIPTION_MAX_LENGTH) {
            String message = String.format("Description can contain up to %s characters.", DESCRIPTION_MAX_LENGTH);
            return Validation.invalid(message);
        }
        if (!tmpDescription.matches(DESCRIPTION_REG_EXP)) {
            return CharSeq.of(tmpDescription)
                          .replaceAll(DESCRIPTION_REG_EXP_ALLOWED_CHARACTERS, "")
                          .transform(seq -> seq.isEmpty()
                                            ? Validation.invalid("The description can contain uppercase and lowercase letters with or without diacritics, numbers, hyphens, underscores, and similar characters to write a sentence or question.")
                                            : Validation.invalid(String.format("The description contains illegal characters: '%s'.", seq.distinct().sorted())));
        }
        return Validation.valid(tmpDescription);
    }

    static Validation<String, Integer> validatePriority(Integer priority) {
        if (priority == null) {
            return Validation.invalid("Priority not specified.");
        }
        if (priority < 1) {
            return Validation.invalid("Priority must be a positive number.");
        }
        return Validation.valid(priority);
    }
}
