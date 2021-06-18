package cz.uhk.mois.financialplanning.validation.user;

import cz.uhk.mois.financialplanning.model.dto.user.AddressDto;
import cz.uhk.mois.financialplanning.validation.ValidationSupport;
import io.vavr.collection.CharSeq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 16.03.2020 12:19
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class UserValidationSupport {

    private static final String REG_EX_NAME = "^\\p{Lu}\\p{L}+$";
    private static final String REG_EX_NAME_ALLOWED_CHARACTERS = "[\\p{L}]";

    private static final String REG_EX_EMAIL = "^[\\w-+]+(\\.[\\w-]+)*@[A-Za-z\\d-]+(\\.[A-Za-z\\d]+)*(\\.[A-Za-z]{2,})$";
    private static final String REG_EX_EMAIL_ALLOWED_CHARACTERS = "[\\w-+.@]";

    private static final int FIRST_NAME_MIN_LENGTH = 3;
    private static final int FIRST_NAME_MAX_LENGTH = 30;

    private static final int LAST_NAME_MIN_LENGTH = 4;
    private static final int LAST_NAME_MAX_LENGTH = 50;

    private static final int EMAIL_MIN_LENGTH = 10;
    private static final int EMAIL_MAX_LENGTH = 150;

    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 50;

    private static final String REG_EX_PASSWORD = "^(?=.*[\\d])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\p{L}&&[^a-zA-Z]])(?=.*[@#$%^&+=¨ˇ!\"/\\\\*|'-;_():<>{}\\[\\]])(?=\\S+$).{" + PASSWORD_MIN_LENGTH + "," + PASSWORD_MAX_LENGTH + "}$";

    private static final String REG_EX_TELEPHONE_NUMBER = "^(\\+\\d{12}|\\d{9})$";
    private static final String REG_EX_TELEPHONE_NUMBER_ALLOWED_CHARACTERS = "[\\+\\d]";

    /**
     * Street in the form 'Name1 name2 123'.
     */
    private static final String REG_EX_ADDRESS_STREET = "^([\\p{L}]+|[\\p{L}]+\\s[\\p{L}]+|[\\p{L}]+[\\s+\\p{L}]+)\\s+\\d+$";
    private static final String REG_EX_ADDRESS_STREET_ALLOWED_CHARACTERS = "[\\p{L}\\d\\s]";
    private static final int ADDRESS_STREET_MIN_LENGTH = 3;
    private static final int ADDRESS_STREET_MAX_LENGTH = 100;
    /**
     * The city will be at least one word.
     */
    private static final String REG_EX_ADDRESS_CITY = "^([\\p{L}]+|[\\p{L}]+\\s+[\\p{L}]+|[\\p{L}]+[\\s+\\p{L}]+)$";
    private static final String REG_EX_ADDRESS_CITY_ALLOWED_CHARACTERS = "[\\p{L}\\s]";
    private static final int ADDRESS_CITY_MIN_LENGTH = 2;
    private static final int ADDRESS_CITY_MAX_LENGTH = 100;

    private static final String REG_EX_PSC = "^\\d{1,10}$";

    static Validation<String, String> validateFirstName(String firstName) {
        if (firstName == null || firstName.replaceAll("\\s", "").isEmpty() || firstName.trim().length() < FIRST_NAME_MIN_LENGTH) {
            String message = String.format("First name must contain at least %s characters.", FIRST_NAME_MIN_LENGTH);
            return Validation.invalid(message);
        }
        String tmpFirstName = firstName.trim();
        if (tmpFirstName.length() > FIRST_NAME_MAX_LENGTH) {
            String message = String.format("First name can contain up to %s characters.", FIRST_NAME_MAX_LENGTH);
            return Validation.invalid(message);
        }
        if (!tmpFirstName.matches(REG_EX_NAME)) {
            return CharSeq.of(tmpFirstName)
                          .replaceAll(REG_EX_NAME_ALLOWED_CHARACTERS, "")
                          .transform(seq -> seq.isEmpty()
                                            ? Validation.invalid("First name must start with a capital letter, must be a single word with uppercase and lowercase letters with or without diacritics.")
                                            : Validation.invalid(String.format("First name contains invalid characters: '%s'.", seq.distinct().sorted())));
        }
        return Validation.valid(tmpFirstName);
    }

    static Validation<String, String> validateLastName(String lastName) {
        if (lastName == null || lastName.replaceAll("\\s", "").isEmpty() || lastName.trim().length() < LAST_NAME_MIN_LENGTH) {
            String message = String.format("Last name must contain at least %s characters.", LAST_NAME_MIN_LENGTH);
            return Validation.invalid(message);
        }
        String tmpLastName = lastName.trim();
        if (tmpLastName.length() > LAST_NAME_MAX_LENGTH) {
            String message = String.format("Last name can contain up to %s characters.", LAST_NAME_MAX_LENGTH);
            return Validation.invalid(message);
        }
        if (!tmpLastName.matches(REG_EX_NAME)) {
            return CharSeq.of(tmpLastName)
                          .replaceAll(REG_EX_NAME_ALLOWED_CHARACTERS, "")
                          .transform(seq -> seq.isEmpty()
                                            ? Validation.invalid("Last name must start with a capital letter, must be a single word with uppercase and lowercase letters with or without diacritics.")
                                            : Validation.invalid(String.format("Last name contains invalid characters: '%s'.", seq.distinct().sorted())));
        }
        return Validation.valid(tmpLastName);
    }

    static Validation<String, String> validateEmail(String email) {
        if (email == null || email.replaceAll("\\s", "").isEmpty() || email.trim().length() < EMAIL_MIN_LENGTH) {
            String message = String.format("Email must contain at least %s characters.", EMAIL_MIN_LENGTH);
            return Validation.invalid(message);
        }
        String tmpEmail = email.trim();
        if (tmpEmail.length() > EMAIL_MAX_LENGTH) {
            String message = String.format("Email can contain up to %s characters.", EMAIL_MAX_LENGTH);
            return Validation.invalid(message);
        }
        if (!tmpEmail.matches(REG_EX_EMAIL)) {
            return CharSeq.of(tmpEmail).replaceAll(REG_EX_EMAIL_ALLOWED_CHARACTERS, "").transform(seq -> seq.isEmpty()
                                                                                                         ? Validation.invalid("Email can contain numbers, underscores, decimal points or uppercase and lowercase letters without diacritics.")
                                                                                                         : Validation.invalid(String.format("Email contains invalid characters: '%s'.", seq.distinct().sorted())));
        }
        return Validation.valid(tmpEmail);
    }

    static Validation<String, String> validatePassword(String password) {
        if (password == null || password.replaceAll("\\s", "").isEmpty() || password.replaceAll("\\s", "").length() < PASSWORD_MIN_LENGTH) {
            String message = String.format("Password must contain at least %s characters.", PASSWORD_MIN_LENGTH);
            return Validation.invalid(message);
        }
        String tmpPassword = password.trim();
        if (tmpPassword.length() > PASSWORD_MAX_LENGTH) {
            String message = String.format("Password can contain up to %s characters.", PASSWORD_MAX_LENGTH);
            return Validation.invalid(message);
        }
        if (!tmpPassword.matches(REG_EX_PASSWORD)) {
            return Validation.invalid("Password must contain at least one digit, a case-sensitive character, accented letter, and a special character, such as '@#$%^&+=!/\\\\*|'-;_():<>{}[]' etc.");
        }
        return Validation.valid(tmpPassword);
    }

    static Validation<String, Long> validateAccountId(Long accountId) {
        if (accountId == null) {
            return Validation.valid(null);
        }
        if (accountId < 0) {
            return Validation.invalid("Account number must be a positive number greater than zero.");
        }
        return Validation.valid(accountId);
    }

    static Validation<String, String> validateTelephoneNumber(String telephoneNumber) {
        if (telephoneNumber == null || telephoneNumber.replaceAll("\\s", "").isEmpty()) {
            return Validation.valid(null);
        }
        String tmpTelephoneNumber = telephoneNumber.trim();
        if (!tmpTelephoneNumber.replaceAll("\\s", "").matches(REG_EX_TELEPHONE_NUMBER)) {
            return CharSeq.of(tmpTelephoneNumber)
                          .replaceAll(REG_EX_TELEPHONE_NUMBER_ALLOWED_CHARACTERS, "")
                          .transform(seq -> seq.isEmpty()
                                            ? Validation.invalid("The phone number can be either '+ xxx xxx xxx xxx' or 'xxx xxx xxx'.")
                                            : Validation.invalid(String.format("The phone number contains illegal characters: '%s'.", seq.distinct().sorted())));
        }
        return Validation.valid(tmpTelephoneNumber);
    }

    static Validation<String, AddressDto> validateAddress(AddressDto addressDto) {
        return Validation.combine(validateStreet(addressDto.getStreet()),
                                  validateCity(addressDto.getCity()),
                                  validatePsc(addressDto.getPsc()))
                         .ap(AddressDto::new)
                         .mapError(validationViolations -> ValidationSupport.removeListText(validationViolations.toString()));
    }

    private static Validation<String, String> validateStreet(String street) {
        if (street == null || street.trim().length() < ADDRESS_STREET_MIN_LENGTH) {
            String message = String.format("Street must contain at least %s characters.", ADDRESS_STREET_MIN_LENGTH);
            return Validation.invalid(message);
        }
        String tmpStreet = street.trim();
        if (tmpStreet.length() > ADDRESS_STREET_MAX_LENGTH) {
            String message = String.format("Street can contain up to %s characters.", ADDRESS_STREET_MAX_LENGTH);
            return Validation.invalid(message);
        }
        if (!tmpStreet.matches(REG_EX_ADDRESS_STREET)) {
            return CharSeq.of(tmpStreet)
                          .replaceAll(REG_EX_ADDRESS_STREET_ALLOWED_CHARACTERS, "")
                          .transform(seq -> seq.isEmpty()
                                            ? Validation.invalid("The street must be in syntax 'Street 123'.")
                                            : Validation.invalid(String.format("The street contains illegal characters: '%s'.", seq.distinct().sorted())));
        }
        return Validation.valid(tmpStreet);
    }

    private static Validation<String, String> validateCity(String city) {
        if (city == null || city.trim().length() < ADDRESS_CITY_MIN_LENGTH) {
            String message = String.format("City must contain at least %s characters.", ADDRESS_CITY_MIN_LENGTH);
            return Validation.invalid(message);
        }
        String tmpCity = city.trim();
        if (tmpCity.length() > ADDRESS_CITY_MAX_LENGTH) {
            String message = String.format("City can contain up to %s characters.", ADDRESS_CITY_MAX_LENGTH);
            return Validation.invalid(message);
        }
        if (!tmpCity.matches(REG_EX_ADDRESS_CITY)) {
            return CharSeq.of(tmpCity)
                          .replaceAll(REG_EX_ADDRESS_CITY_ALLOWED_CHARACTERS, "")
                          .transform(seq -> seq.isEmpty()
                                            ? Validation.invalid("The city must be in syntax 'City Name'.")
                                            : Validation.invalid(String.format("The city contains illegal characters: '%s'.", seq.distinct().sorted())));
        }
        return Validation.valid(tmpCity);
    }

    private static Validation<String, Integer> validatePsc(Integer psc) {
        if (psc == null) {
            return Validation.invalid("Zip code not specified.");
        }
        if (!psc.toString().trim().matches(REG_EX_PSC)) {
            return Validation.invalid("The zip code does not match the required syntax.");
        }
        return Validation.valid(psc);
    }

    static Validation<String, String> validatePassword(String password, String replacement) {
        Validation<String, String> validation = validatePassword(password);
        if (validation.isInvalid()) {
            String error = validation.getError();
            String msgOriginalUsername = error.replaceFirst("Password", replacement);
            return Validation.invalid(msgOriginalUsername);
        }
        return validation;
    }
}
