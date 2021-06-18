package ua.uhk.mois.financialplanning.validation.user;

import ua.uhk.mois.financialplanning.model.dto.user.ChangePasswordDtoIn;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author KVN
 * @since 22.03.2021 8:27
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangePasswordDtoInValidator {

    public static Validation<Seq<String>, ChangePasswordDtoIn> validate(ChangePasswordDtoIn dtoIn) {
        return Validation.combine(UserValidationSupport.validateEmail(dtoIn.getEmail()),
                                  UserValidationSupport.validatePassword(dtoIn.getOriginalPassword(), "Original password"),
                                  UserValidationSupport.validatePassword(dtoIn.getNewPassword(), "New password"),
                                  UserValidationSupport.validatePassword(dtoIn.getConfirmationPassword(), "Confirmation password"))
                         .combine(validateConfirmationPasswords(dtoIn.getNewPassword(), dtoIn.getConfirmationPassword()))
                         .ap((email, originalPassword, newPassword, confirmationPassword, passwordEqualsCheck) -> new ChangePasswordDtoIn(email, originalPassword, newPassword, confirmationPassword));
    }

    private static Validation<String, String> validateConfirmationPasswords(String newPassword, String newPasswordAgain) {
        if (!StringUtils.equals(newPassword, newPasswordAgain)) {
            return Validation.invalid("The new password and the confirmation of the new password are different.");
        }
        return Validation.valid(newPassword);
    }
}
