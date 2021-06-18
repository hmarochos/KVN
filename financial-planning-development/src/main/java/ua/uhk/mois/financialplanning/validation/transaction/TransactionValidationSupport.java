package ua.uhk.mois.financialplanning.validation.transaction;

import ua.uhk.mois.financialplanning.model.dto.transaction.AdditionalInfoDomestic;
import ua.uhk.mois.financialplanning.model.dto.transaction.Direction;
import ua.uhk.mois.financialplanning.model.dto.transaction.Value;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.TransactionType;
import io.vavr.collection.CharSeq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author KVN
 * @since 04.04.2021 3:38
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionValidationSupport {

    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final String DESCRIPTION_REG_EXP = "^[\\p{L}\\w\\s,.?!_\\-]{0," + DESCRIPTION_MAX_LENGTH + "}$";
    private static final String DESCRIPTION_REG_EXP_ALLOWED_CHARACTERS = "[\\p{L}\\w\\s,.?!_\\-]";

    private static final String ADDITIONAL_INFO_DOMESTIC_REG_EX = "^\\d{0,10}$";

    static Validation<String, Long> validateAccountId(Long accountId) {
        if (accountId == null) {
            return Validation.invalid("Account number is not specified.");
        }
        if (accountId < 0) {
            return Validation.invalid("Account number must be a positive number greater than zero.");
        }
        return Validation.valid(accountId);
    }

    static Validation<String, Value> validateValue(Value value) {
        if (value == null) {
            return Validation.invalid("Financial information is not specified.");
        }

        if (value.getAmount() == null) {
            return Validation.invalid("Transfer amount not specified.");
        }
        if (value.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            return Validation.invalid("Amount must be a positive number.");
        }

        if (value.getCurrency() == null) {
            return Validation.invalid("Currency not specified.");
        }

        return Validation.valid(value);
    }

    static Validation<String, Long> validateBankCode(Long bankCode) {
        if (bankCode == null) {
            return Validation.invalid("Bank code not specified.");
        }
        if (bankCode < 0) {
            return Validation.invalid("Bank code must be a positive number.");
        }
        return Validation.valid(bankCode);
    }

    static Validation<String, String> validatePartyDescription(String partyDescription) {
        if (partyDescription == null || partyDescription.replaceAll("\\s", "").isEmpty()) {
            return Validation.invalid("Description not specified.");
        }
        String tmpPartyDescription = partyDescription.trim();
        if (tmpPartyDescription.length() > DESCRIPTION_MAX_LENGTH) {
            String message = String.format("Description can contain up to %s characters.", DESCRIPTION_MAX_LENGTH);
            return Validation.invalid(message);
        }
        if (!tmpPartyDescription.matches(DESCRIPTION_REG_EXP)) {
            return CharSeq.of(tmpPartyDescription)
                          .replaceAll(DESCRIPTION_REG_EXP_ALLOWED_CHARACTERS, "")
                          .transform(seq -> seq.isEmpty()
                                            ? Validation.invalid("The party description can contain uppercase and lowercase letters with or without diacritics, numbers, hyphens, underscores, and similar characters to write a sentence or question.")
                                            : Validation.invalid(String.format("The party description contains illegal characters: '%s'.", seq.distinct().sorted())));
        }
        return Validation.valid(tmpPartyDescription);
    }

    static Validation<String, Direction> validateDirection(Direction direction) {
        if (direction == null) {
            return Validation.invalid("Transaction direction not specified.");
        }
        return Validation.valid(direction);
    }

    static Validation<String, TransactionType> validateTransactionType(TransactionType transactionType) {
        if (transactionType == null) {
            return Validation.invalid("Transaction type not specified.");
        }
        return Validation.valid(transactionType);
    }

    static Validation<String, AdditionalInfoDomestic> validateAdditionalInfoDomestic(AdditionalInfoDomestic additionalInfoDomestic) {
        // additionalInfoDomestic can be null, but not its values, to the highest empty text
        if (additionalInfoDomestic == null) {
            return Validation.valid(null);
        }

        // According to API call attempts, there must be no null value, only empty text
        if (additionalInfoDomestic.getConstantSymbol() == null) {
            additionalInfoDomestic.setConstantSymbol("");
        }
        if (additionalInfoDomestic.getVariableSymbol() == null) {
            additionalInfoDomestic.setVariableSymbol("");
        }
        if (additionalInfoDomestic.getSpecificSymbol() == null) {
            additionalInfoDomestic.setSpecificSymbol("");
        }

        String constantSymbol = additionalInfoDomestic.getConstantSymbol().replaceAll("\\s", "");
        String variableSymbol = additionalInfoDomestic.getVariableSymbol().replaceAll("\\s", "");
        String specificSymbol = additionalInfoDomestic.getSpecificSymbol().replaceAll("\\s", "");
        if (!constantSymbol.matches(ADDITIONAL_INFO_DOMESTIC_REG_EX)) {
            return Validation.invalid("The constant symbol can only be 1 - 10 numbers.");
        }
        if (!variableSymbol.matches(ADDITIONAL_INFO_DOMESTIC_REG_EX)) {
            return Validation.invalid("The variable symbol can only be 1 - 10 numbers.");
        }
        if (!specificSymbol.matches(ADDITIONAL_INFO_DOMESTIC_REG_EX)) {
            return Validation.invalid("The specific symbol can only be 1 - 10 numbers.");
        }

        return Validation.valid(additionalInfoDomestic);
    }
}
