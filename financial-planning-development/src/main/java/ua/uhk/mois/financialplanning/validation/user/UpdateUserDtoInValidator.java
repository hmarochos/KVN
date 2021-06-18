package ua.uhk.mois.financialplanning.validation.user;

import ua.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoIn;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author KVN
 * @since 16.03.2021 12:44
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateUserDtoInValidator {

    public static Validation<Seq<String>, UpdateUserDtoIn> validate(UpdateUserDtoIn dtoIn) {
        return Validation.combine(UserValidationSupport.validateFirstName(dtoIn.getFirstName()),
                                  UserValidationSupport.validateLastName(dtoIn.getLastName()),
                                  validateOriginalEmail(dtoIn.getOriginalEmail(), "Original email"),
                                  validateOriginalEmail(dtoIn.getUpdatedEmail(), "New email"),
                                  UserValidationSupport.validateAccountId(dtoIn.getAccountId()),
                                  UserValidationSupport.validateTelephoneNumber(dtoIn.getTelephoneNumber()),
                                  UserValidationSupport.validateAddress(dtoIn.getAddress()))
                         .ap(UpdateUserDtoIn::new);
    }

    private static Validation<String, String> validateOriginalEmail(String originalEmail, String replacement) {
        Validation<String, String> validation = UserValidationSupport.validateEmail(originalEmail);
        if (validation.isInvalid()) {
            String error = validation.getError();
            String msgOriginalUsername = error.replaceFirst("Email", replacement);
            return Validation.invalid(msgOriginalUsername);
        }
        return validation;
    }
}
