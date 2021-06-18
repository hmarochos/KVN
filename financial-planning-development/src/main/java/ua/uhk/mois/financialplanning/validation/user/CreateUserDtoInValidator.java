package ua.uhk.mois.financialplanning.validation.user;

import ua.uhk.mois.financialplanning.model.dto.user.CreateUserDtoIn;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author KVN
 * @since 15.03.2021 20:48
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateUserDtoInValidator {

    public static Validation<Seq<String>, CreateUserDtoIn> validate(CreateUserDtoIn dtoIn) {
        return Validation.combine(UserValidationSupport.validateFirstName(dtoIn.getFirstName()),
                                  UserValidationSupport.validateLastName(dtoIn.getLastName()),
                                  UserValidationSupport.validateEmail(dtoIn.getEmail()),
                                  UserValidationSupport.validatePassword(dtoIn.getPassword(), "Password"),
                                  UserValidationSupport.validatePassword(dtoIn.getPasswordConfirmation(), "Confirmation password"),
                                  UserValidationSupport.validateAccountId(dtoIn.getAccountId()),
                                  UserValidationSupport.validateTelephoneNumber(dtoIn.getTelephoneNumber()),
                                  UserValidationSupport.validateAddress(dtoIn.getAddress()))
                         .ap((firstName, lastName, email, password, passwordConfirmation, accountId, telephoneNumber, addressDto) -> new CreateUserDtoIn(firstName, lastName, email, password, passwordConfirmation, accountId, telephoneNumber, addressDto, dtoIn.getRoles()));
    }
}
