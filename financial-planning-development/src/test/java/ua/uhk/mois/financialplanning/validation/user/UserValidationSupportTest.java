package ua.uhk.mois.financialplanning.validation.user;

import ua.uhk.mois.financialplanning.configuration.TextTestSupport;
import ua.uhk.mois.financialplanning.configuration.user.UserTestSupport;
import ua.uhk.mois.financialplanning.model.dto.user.AddressDto;
import io.vavr.control.Validation;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class UserValidationSupportTest {

    private static void assertInvalidFirstName(String firstName, String expectedMessage) {
        // Data preparation
        // Execution
        Validation<String, String> validation = UserValidationSupport.validateFirstName(firstName);

        // Verification
        assertTrue(validation.isInvalid());
        assertEquals(expectedMessage, validation.getError());
    }

    private static void assertInvalidLastName(String lastName, String expectedMessage) {
        // Data preparation
        // Execution
        Validation<String, String> validation = UserValidationSupport.validateLastName(lastName);

        // Verification
        assertTrue(validation.isInvalid());
        assertEquals(expectedMessage, validation.getError());
    }

    private static void assertInvalidEmail(String email, String expectedMessage) {
        // Execution
        Validation<String, String> validation = UserValidationSupport.validateEmail(email);

        // Verification
        assertTrue(validation.isInvalid());
        assertEquals(expectedMessage, validation.getError());
    }

    private static void assertInvalidPassword(String password, String expectedMessage) {
        // Execution
        Validation<String, String> validation = UserValidationSupport.validatePassword(password);

        // Verification
        assertTrue(validation.isInvalid());
        assertEquals(expectedMessage, validation.getError());
    }

    private static void assertInvalidAccountId(Long accountId, String expectedMessage) {
        // Execution
        Validation<String, Long> validation = UserValidationSupport.validateAccountId(accountId);

        // Verification
        assertTrue(validation.isInvalid());
        assertEquals(expectedMessage, validation.getError());
    }

    private static void assertInvalidTelephoneNumber(String telephoneNumber, String expectedMessage) {
        // Execution
        Validation<String, String> validation = UserValidationSupport.validateTelephoneNumber(telephoneNumber);

        // Verification
        assertTrue(validation.isInvalid());
        assertEquals(expectedMessage, validation.getError());
    }

    private static void assertInvalidAddress(AddressDto addressDto, String expectedMessage) {
        // Execution
        Validation<String, AddressDto> validation = UserValidationSupport.validateAddress(addressDto);

        // Verification
        assertTrue(validation.isInvalid());
        assertEquals(expectedMessage, validation.getError());
    }

    @Test
    public void validateFirstName_Hds() {
        log.info("Username validation test.");

        // Data preparation
        // Execution
        Validation<String, String> validation = UserValidationSupport.validateFirstName("FirstName");
        Validation<String, String> validationTrim = UserValidationSupport.validateFirstName("FirstName                                                              ");

        // Verification
        assertTrue(validation.isValid());
        assertTrue(validationTrim.isValid());
    }

    @Test
    public void validateFirstName_Null() {
        log.info("Username validation test. The first name will be null.");

        String message = "First name must contain at least 3 characters.";
        assertInvalidFirstName(null, message);
    }

    @Test
    public void validateFirstName_Empty() {
        log.info("Username validation test. First name will not be given, respectively. only white space will be listed.");

        String message = "First name must contain at least 3 characters.";
        assertInvalidFirstName("     ", message);
    }

    @Test
    public void validateFirstName_MinLength() {
        log.info("Username validation test. The first name will not contain enough (minimum) characters.");

        String message = "First name must contain at least 3 characters.";
        assertInvalidFirstName("mn                             ", message);
    }

    @Test
    public void validateFirstName_MaxLength() {
        log.info("Username validation test. The first name will contain an excess number of characters (more characters than allowed).");

        String message = "First name can contain up to 30 characters.";
        assertInvalidFirstName(TextTestSupport.generateInvalidText(30L, "FirstName"), message);
    }

    @Test
    public void validateFirstName_InvalidSyntax() {
        log.info("Username validation test. The first name will not be in the required syntax.");

        String message = "First name must start with a capital letter, must be a single word with uppercase and lowercase letters with or without diacritics.";
        assertInvalidFirstName("firstSmallLetter", message);
    }

    @Test
    public void validateFirstName_ForbiddenCharacters() {
        log.info("Username validation test. The first name will contain invalid / prohibited characters.");

        String message = "First name contains invalid characters: '!#$%&'()*-1;<=>?@^_`|~¨´'.";
        assertInvalidFirstName("1-_?!''¨¨´´=)(*&^%$#@!~`;<>(|)", message);
    }

    @Test
    public void validateLastName_Hds() {
        log.info("User surname validation test.");

        // Data preparation
        // Execution
        Validation<String, String> validation = UserValidationSupport.validateLastName("LastName");
        Validation<String, String> validationTrim = UserValidationSupport.validateLastName("LastName                                                               ");

        // Verification
        assertTrue(validation.isValid());
        assertTrue(validationTrim.isValid());
    }

    @Test
    public void validateLastName_Null() {
        log.info("User surname validation test. The last name will not contain the specified value (will be null).");

        String message = "Last name must contain at least 4 characters.";
        assertInvalidLastName(null, message);
    }

    @Test
    public void validateLastName_Empty() {
        log.info("User surname validation test. The last name will not be given, respectively. only white space will be listed.");

        String message = "Last name must contain at least 4 characters.";
        assertInvalidLastName("       ", message);
    }

    @Test
    public void validateLastName_MinLength() {
        log.info("User surname validation test. The last name will not contain sufficient (minimum) characters.");

        String message = "Last name must contain at least 4 characters.";
        assertInvalidLastName("mn", message);
    }

    @Test
    public void validateLastName_MaxLength() {
        log.info("User surname validation test. The last name will contain more characters than allowed.");

        String message = "Last name can contain up to 50 characters.";
        assertInvalidLastName(TextTestSupport.generateInvalidText(50L, "LastName"), message);
    }

    @Test
    public void validateLastName_InvalidSyntax() {
        log.info("User surname validation test. The surname will be given in invalid syntax.");

        String message = "Last name must start with a capital letter, must be a single word with uppercase and lowercase letters with or without diacritics.";
        assertInvalidLastName("smallFirstLetter", message);
    }

    @Test
    public void validateLastName_ForbiddenCharacters() {
        log.info("User surname validation test. The surname will contain invalid / prohibited characters.");

        String message = "Last name contains invalid characters: ' !\"#$%&'()*-1;<=>?@[\\]^_`{|}~¨´'.";
        assertInvalidLastName("1-_?!''¨¨´´=)(*&^%$#@!~`;<>(|)\\ \"[]{}", message);
    }

    @Test
    public void validateEmail_Hds() {
        log.info("Email address validation test.");

        // Data preparation
        String email = "dummyEmail@gmail.com";
        String emailTrim = "                                                                  dummyEmail@gmail.com";

        // Execution
        Validation<String, String> validation = UserValidationSupport.validateEmail(email);
        Validation<String, String> validationTrim = UserValidationSupport.validateEmail(emailTrim);

        // Verification
        assertTrue(validation.isValid());
        assertTrue(validationTrim.isValid());
    }

    @Test
    public void validateEmail_Null() {
        log.info("Email address validation test. Email will be null.");

        // Data preparation
        // Execution
        // Verification
        String expectedMessage = String.format("Email must contain at least %s characters.", 10);
        assertInvalidEmail(null, expectedMessage);
    }

    @Test
    public void validateEmail_Empty() {
        log.info("Email address validation test. Email will be blank (white space).");

        // Data preparation
        String email = "    ";

        // Execution
        // Verification
        String expectedMessage = String.format("Email must contain at least %s characters.", 10);
        assertInvalidEmail(email, expectedMessage);
    }

    @Test
    public void validateEmail_MinLength() {
        log.info("Email address validation test. Email will not meet the minimum required length (number of characters).");

        // Data preparation
        String email = "minChars";

        // Execution
        // Verification
        String expectedMessage = String.format("Email must contain at least %s characters.", 10);
        assertInvalidEmail(email, expectedMessage);
    }

    @Test
    public void validateEmail_MaxLength() {
        log.info("Email address validation test. The email will not meet the maximum required length (number of characters).");

        // Data preparation
        String email = TextTestSupport.generateInvalidText(150L, "email@sezam.ua");

        // Execution
        // Verification
        String expectedMessage = String.format("Email can contain up to %s characters.", 150);
        assertInvalidEmail(email, expectedMessage);
    }

    @Test
    public void validateEmail_InvalidSyntax() {
        log.info("Email address validation test. The email will be entered in the wrong syntax (it will not match the regular expression).");

        // Data preparation
        String email = "@email@sezam.ua";

        // Execution
        // Verification
        String expectedMessage = "Email can contain numbers, underscores, decimal points or uppercase and lowercase letters without diacritics.";
        assertInvalidEmail(email, expectedMessage);
    }

    @Test
    public void validateEmail_InvalidCharacters() {
        log.info("Email address validation test. Email will contain prohibited characters.");

        // Data preparation
        String email = "?!()[]'=-`_123éíáemail@sezam.ua";

        // Execution
        // Verification
        String expectedMessage = String.format("Email contains invalid characters: '%s'.", "!'()=?[]`áéí");
        assertInvalidEmail(email, expectedMessage);
    }

    @Test
    public void validatePassword_Hds() {
        log.info("Password validation test.");

        // Data preparation
        String password = "Passwd_123@ě";
        String passwordTrim = "                                                    Passwd_123@ě     ";

        // Execution
        Validation<String, String> validation = UserValidationSupport.validatePassword(password);
        Validation<String, String> validationTrim = UserValidationSupport.validatePassword(passwordTrim);

        // Verification
        assertTrue(validation.isValid());
        assertTrue(validationTrim.isValid());
    }

    @Test
    public void validatePassword_Null() {
        log.info("Password validation test. Password will be null.");

        // Data preparation
        // Execution
        // Verification
        String expectedMessage = String.format("Password must contain at least %s characters.", 8);
        assertInvalidPassword(null, expectedMessage);
    }

    @Test
    public void validatePassword_Empty() {
        log.info("Password validation test. Password will be blank (white space).");

        // Data preparation
        String password = "        ";

        // Execution
        // Verification
        String expectedMessage = String.format("Password must contain at least %s characters.", 8);
        assertInvalidPassword(password, expectedMessage);
    }

    @Test
    public void validatePassword_MinLength() {
        log.info("Password validation test. Password will not meet the minimum required length (number of characters).");

        // Data preparation
        String password = "minLen";

        // Execution
        // Verification
        String expectedMessage = String.format("Password must contain at least %s characters.", 8);
        assertInvalidPassword(password, expectedMessage);
    }

    @Test
    public void validatePassword_MaxLength() {
        log.info("Password validation test. The password will not meet the maximum required length (number of characters).");

        // Data preparation
        String password = TextTestSupport.generateInvalidText(50L, "Password_123@é");

        // Execution
        // Verification
        String expectedMessage = String.format("Password can contain up to %s characters.", 50);
        assertInvalidPassword(password, expectedMessage);
    }

    @Test
    public void validatePassword_InvalidSyntax() {
        log.info("Password validation test. The password will not meet the required syntax (it will not match regular expressions - for character requests).");

        // Data preparation
        String password = "password";

        // Execution
        // Verification
        String expectedMessage = "Password must contain at least one digit, a case-sensitive character, accented letter, and a special character, such as '@#$%^&+=!/\\\\*|'-;_():<>{}[]' etc.";
        assertInvalidPassword(password, expectedMessage);
    }

    @Test
    void validateAccountId_Hds() {
        log.info("Account number validation test.");

        // Data preparation
        Long accountId = 123L;

        // Execution
        Validation<String, Long> validation = UserValidationSupport.validateAccountId(accountId);

        // Verification
        assertTrue(validation.isValid());
    }

    @Test
    void validateAccountId_Null() {
        log.info("Account number validation test. Number will not be provided (optional).");

        // Data preparation
        // Execution
        Validation<String, Long> validation = UserValidationSupport.validateAccountId(null);

        // Verification
        assertTrue(validation.isValid());
    }

    @Test
    void validateAccountId_Negative() {
        log.info("Account number validation test. A negative number will be reported.");

        // Data preparation
        Long accountId = -123L;

        // Execution
        // Verification
        String expectedMessage = "Account number must be a positive number greater than zero.";
        assertInvalidAccountId(accountId, expectedMessage);
    }

    @Test
    void validateTelephoneNumber_Hds() {
        log.info("Phone number validation test.");

        // Data preparation
        String number1 = "789 456 123";
        // Execution
        Validation<String, String> validation1 = UserValidationSupport.validateTelephoneNumber(number1);
        // Verification
        assertTrue(validation1.isValid());

        // Data preparation
        String number2 = "+420 123 456 789";
        // Execution
        Validation<String, String> validation2 = UserValidationSupport.validateTelephoneNumber(number2);
        // Verification
        assertTrue(validation2.isValid());
    }

    @Test
    void validateTelephoneNumber_Null() {
        log.info("Phone number validation test. The number will not be given (number will be null).");

        // Data preparation
        // Execution
        Validation<String, String> validation = UserValidationSupport.validateTelephoneNumber(null);
        // Verification
        assertTrue(validation.isValid());
        assertNull(validation.get());
    }

    @Test
    void validateTelephoneNumber_Empty() {
        log.info("Phone number validation test. The number will not be given (number will be empty String).");

        // Data preparation
        // Execution
        Validation<String, String> validation = UserValidationSupport.validateTelephoneNumber("     ");
        // Verification
        assertTrue(validation.isValid());
        assertNull(validation.get());
    }

    @Test
    void validateTelephoneNumber_InvalidSyntax() {
        log.info("Phone number validation test. The number will be in invalid syntax.");

        // Data preparation
        String number = "123";

        // Execution
        // Verification
        String expectedMessage = "The phone number can be either '+ xxx xxx xxx xxx' or 'xxx xxx xxx'.";
        assertInvalidTelephoneNumber(number, expectedMessage);
    }

    @Test
    void validateTelephoneNumber_IllegalCharacters() {
        log.info("Phone number validation test. The number will contain prohibited characters.");

        // Data preparation
        String number = "!_éíáýžřčšě+-?";

        // Execution
        // Verification
        String expectedMessage = String.format("The phone number contains illegal characters: '%s'.", "!-?_áéíýčěřšž");
        assertInvalidTelephoneNumber(number, expectedMessage);
    }

    @Test
    void validateAddress_Hds() {
        log.info("Address validation test.");

        // Data preparation
        AddressDto addressDto = UserTestSupport.createAddressDto();

        // Execution
        Validation<String, AddressDto> validation = UserValidationSupport.validateAddress(addressDto);

        // Verification
        assertTrue(validation.isValid());
    }

    @Test
    void validateAddress_Null() {
        log.info("Address validation test. All values will be null.");

        // Data preparation
        AddressDto addressDto = UserTestSupport.createAddressDto();
        addressDto.setStreet(null);
        addressDto.setCity(null);
        addressDto.setPsc(null);

        // Execution
        // Verification
        String expectedMessage = "Street must contain at least 3 characters., City must contain at least 2 characters., Zip code not specified.";
        assertInvalidAddress(addressDto, expectedMessage);
    }

    @Test
    void validateAddress_MinLength() {
        log.info("Address validation test. Values do not meet the minimum required length (number of characters).");

        // Data preparation
        AddressDto addressDto = UserTestSupport.createAddressDto();
        addressDto.setStreet("          mn");
        addressDto.setCity("m         ");
        addressDto.setPsc(-1);

        // Execution
        // Verification
        String expectedMessage = "Street must contain at least 3 characters., City must contain at least 2 characters., The zip code does not match the required syntax.";
        assertInvalidAddress(addressDto, expectedMessage);
    }

    @Test
    void validateAddress_MaxLength() {
        log.info("Address validation test. Values exceed the maximum number of characters allowed.");

        // Data preparation
        AddressDto addressDto = UserTestSupport.createAddressDto();
        addressDto.setStreet(TextTestSupport.generateInvalidText(100L, "Street 123"));
        addressDto.setCity(TextTestSupport.generateInvalidText(100L, "City Name"));

        // Execution
        // Verification
        String expectedMessage = "Street can contain up to 100 characters., City can contain up to 100 characters.";
        assertInvalidAddress(addressDto, expectedMessage);
    }

    @Test
    void validateAddress_InvalidSyntax() {
        log.info("Address validation test. Values do not match valid syntax.");

        // Data preparation
        AddressDto addressDto = UserTestSupport.createAddressDto();
        addressDto.setStreet("  1234");

        // Execution
        // Verification
        String expectedMessage = "The street must be in syntax 'Street 123'.";
        assertInvalidAddress(addressDto, expectedMessage);
    }

    @Test
    void validateAddress_InvalidCharacters() {
        log.info("Address validation test. Values contain disabled characters.");

        // Data preparation
        AddressDto addressDto = UserTestSupport.createAddressDto();
        addressDto.setStreet("  !?-:´;°¨_-()|[]{}");
        addressDto.setCity("123 MN _-_. () []{}|/ !?;");

        // Execution
        // Verification
        String expectedMessage = "The street contains illegal characters: '!()-:;?[]_{|}¨°´'., The city contains illegal characters: '!()-./123;?[]_{|}'.";
        assertInvalidAddress(addressDto, expectedMessage);
    }
}
