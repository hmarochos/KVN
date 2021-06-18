package ua.uhk.mois.financialplanning.validation.transaction;

import ua.uhk.mois.financialplanning.model.dto.transaction.AddTransactionDtoIn;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * @author KVN
 * @since 03.04.2021 2:10
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddTransactionDtoInValidator {

    public static Validation<Seq<String>, AddTransactionDtoIn> validate(AddTransactionDtoIn dtoIn) {
        return Validation.combine(TransactionValidationSupport.validateAccountId(dtoIn.getAccountId()),
                                  TransactionValidationSupport.validateValue(dtoIn.getValue()),
                                  TransactionValidationSupport.validateBankCode(dtoIn.getBankCode()),
                                  TransactionValidationSupport.validatePartyDescription(dtoIn.getPartyDescription()),
                                  TransactionValidationSupport.validateDirection(dtoIn.getDirection()),
                                  TransactionValidationSupport.validateTransactionType(dtoIn.getTransactionType()),
                                  validatePaymentDate(dtoIn.getPaymentDate()),
                                  TransactionValidationSupport.validateAdditionalInfoDomestic(dtoIn.getAdditionalInfoDomestic()))
                         .ap(AddTransactionDtoIn::new);
    }

    private static Validation<String, ZonedDateTime> validatePaymentDate(ZonedDateTime paymentDate) {
        if (paymentDate == null) {
            return Validation.invalid("Payment date not specified.");
        }
        return Validation.valid(paymentDate);
    }
}
