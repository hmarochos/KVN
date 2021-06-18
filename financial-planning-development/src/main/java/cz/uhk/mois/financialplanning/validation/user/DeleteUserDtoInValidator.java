package cz.uhk.mois.financialplanning.validation.user;

import cz.uhk.mois.financialplanning.model.dto.user.DeleteUserDtoIn;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 16.03.2020 12:19
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteUserDtoInValidator {

    public static Validation<String, DeleteUserDtoIn> validate(DeleteUserDtoIn dtoIn) {
        return UserValidationSupport.validateEmail(dtoIn.getEmail())
                                    .map(email -> dtoIn);
    }
}
