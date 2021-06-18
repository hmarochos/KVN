package ua.uhk.mois.financialplanning.service;

import ua.uhk.mois.financialplanning.configuration.AbsTestConfiguration;
import ua.uhk.mois.financialplanning.configuration.TextTestSupport;
import ua.uhk.mois.financialplanning.configuration.TransactionSupport;
import ua.uhk.mois.financialplanning.model.dto.transaction.Direction;
import ua.uhk.mois.financialplanning.model.dto.user.ChangePasswordDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.ChangePasswordDtoOut;
import ua.uhk.mois.financialplanning.model.dto.user.CreateUserDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.DeleteUserDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.ProfileDtoOut;
import ua.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoOut;
import ua.uhk.mois.financialplanning.model.dto.user.UserDtoOut;
import ua.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import ua.uhk.mois.financialplanning.model.entity.user.Role;
import ua.uhk.mois.financialplanning.model.entity.user.User;
import ua.uhk.mois.financialplanning.response.Failure;
import ua.uhk.mois.financialplanning.response.Success;
import io.vavr.control.Either;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.ACCOUNT_ID;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.CHANGED_PASSWORD;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.CITY;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.EMAIL;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.FIRST_NAME;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.LAST_NAME;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.PSC;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.STREET;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.TELEPHONE_NUMBER;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.createAddressDto;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.createChangePasswordDtoIn;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.createCreateUserDtoIn;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.createUpdateUserDtoIn;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.createUser;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.signInUser;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.signOutUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class UserServiceImplTest extends AbsTestConfiguration {

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        log.info("Clear database before test.");
        clearDatabase();

        log.info("Logout user before test (if any).");
        signOutUser();
    }

    @AfterEach
    void tearDown() {
        log.info("Clear database after test.");
        clearDatabase();

        log.info("Logout user after test.");
        signOutUser();
    }

    @Test
    void add_Hds() {
        log.info("Test adding a user to the database. Account number will not be entered. Therefore, no transactions will be copied.");

        // Data preparation
        CreateUserDtoIn dtoIn = createCreateUserDtoIn();
        dtoIn.setAccountId(null);
        assertEmptyDatabase();

        // Execution
        Either<Failure, Success<UserDtoOut>> add = userService.add(dtoIn);

        // Verification
        assertTrue(add.isRight());
        assertDatabaseSize(1, 0);

        assertEquals(HttpStatus.CREATED, add.get().getHttpStatus());

        UserDtoOut userDtoOut = add.get().getBody();
        assertEquals(FIRST_NAME, userDtoOut.getFirstName());
        assertEquals(LAST_NAME, userDtoOut.getLastName());
        assertEquals(EMAIL, userDtoOut.getEmail());
        assertNull(userDtoOut.getAccountId());
        assertEquals(TELEPHONE_NUMBER, userDtoOut.getTelephoneNumber());
        assertEquals(STREET, userDtoOut.getAddress().getStreet());
        assertEquals(CITY, userDtoOut.getAddress().getCity());
        assertEquals(PSC, userDtoOut.getAddress().getPsc());
        assertThat(userDtoOut.getRoles(), hasSize(1));
        assertThat(userDtoOut.getRoles(), contains(Role.USER));
    }

    @Test
    void add_InvalidDtoIn() {
        log.info("Test user creation. Test of validation violation in dtoIn.");

        assertEmptyDatabase();

        // Data preparation
        CreateUserDtoIn dtoIn = createCreateUserDtoIn();
        dtoIn.setFirstName("12355");
        dtoIn.setLastName("smallFirstLetter");
        dtoIn.setEmail("¨'=´;homer.simpson@gmail.com");
        dtoIn.setPassword(null);
        dtoIn.setAccountId(-123456L);
        dtoIn.setTelephoneNumber("  ab c   ");
        dtoIn.setAddress(createAddressDto());
        dtoIn.getAddress().setStreet("_strEE+ť _ 123!");
        dtoIn.getAddress().setCity("_123éíá!+-");
        dtoIn.getAddress().setPsc(-1);

        // Execution
        Either<Failure, Success<UserDtoOut>> add = userService.add(dtoIn);

        // Verification
        assertTrue(add.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, add.getLeft().getHttpStatus());
        String expectedMessage = "First name contains invalid characters: '1235'., Last name must start with a capital letter, must be a single word with uppercase and lowercase letters with or without diacritics., Email contains invalid characters: '';=¨´'., Password must contain at least 8 characters., Account number must be a positive number greater than zero., The phone number contains illegal characters: ' abc'., The street contains illegal characters: '!+_'., The city contains illegal characters: '!+-123_'., The zip code does not match the required syntax.";
        assertEquals(expectedMessage, add.getLeft().getMessage());

        assertEmptyDatabase();
    }

    @Test
    void add_DuplicateEmail() {
        log.info("Test user creation. Test duplicate email in database. If you create a user with duplicate email.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);
        assertDatabaseSize(1, 0);

        CreateUserDtoIn dtoIn = createCreateUserDtoIn();
        // Execution
        Either<Failure, Success<UserDtoOut>> addDuplicate = userService.add(dtoIn);

        // Verification
        assertTrue(addDuplicate.isLeft());
        assertEquals(HttpStatus.CONFLICT, addDuplicate.getLeft().getHttpStatus());
        String expectedMessage = "Email already used.";
        assertEquals(expectedMessage, addDuplicate.getLeft().getMessage());

        assertDatabaseSize(1, 0);
    }

    @Test
    void add_DifferentPasswords() {
        log.info("Test user creation. The password and the confirmation password will differ.");

        // Data preparation
        assertEmptyDatabase();

        CreateUserDtoIn dtoIn = createCreateUserDtoIn();
        dtoIn.setPasswordConfirmation(dtoIn.getPasswordConfirmation() + 1);

        // Execution
        Either<Failure, Success<UserDtoOut>> addDuplicate = userService.add(dtoIn);

        // Verification
        assertTrue(addDuplicate.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, addDuplicate.getLeft().getHttpStatus());
        String expectedMessage = "The passwords are different.";
        assertEquals(expectedMessage, addDuplicate.getLeft().getMessage());

        assertEmptyDatabase();
    }

    @Test
    void getProfile_Hds() {
        log.info("Get the signed-in user's profile.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);
        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        // Execution
        Either<Failure, Success<ProfileDtoOut>> profile = userService.getProfile();

        // Verification
        assertTrue(profile.isRight());
        assertDatabaseSize(1, 0);

        assertEquals(HttpStatus.OK, profile.get().getHttpStatus());

        ProfileDtoOut profileDtoOut = profile.get().getBody();
        assertEquals(FIRST_NAME, profileDtoOut.getFirstName());
        assertEquals(LAST_NAME, profileDtoOut.getLastName());
        assertEquals(EMAIL, profileDtoOut.getEmail());
        assertEquals(ACCOUNT_ID, profileDtoOut.getAccountId());
        assertEquals(TELEPHONE_NUMBER, profileDtoOut.getTelephoneNumber());
        assertEquals(STREET, profileDtoOut.getAddress().getStreet());
        assertEquals(CITY, profileDtoOut.getAddress().getCity());
        assertEquals(PSC, profileDtoOut.getAddress().getPsc());
    }

    @Test
    void getProfile_NoUserSignedIn() {
        log.info("Get the signed-in user's profile. No user will be signed-in (in the Spring context).");

        // Data preparation
        User user = createUser();
        userRepository.save(user);
        assertDatabaseSize(1, 0);

        // Execution
        Either<Failure, Success<ProfileDtoOut>> profile = userService.getProfile();

        // Verification
        assertTrue(profile.isLeft());
        assertDatabaseSize(1, 0);

        assertEquals(HttpStatus.UNAUTHORIZED, profile.getLeft().getHttpStatus());

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, profile.getLeft().getMessage());
    }

    @Test
    void getProfile_UserNotFoundById() {
        log.info("Get the signed-in user's profile. The user will not be found in the id database. Thus, a simulation that the token will contain a user id that is no longer in the database (should not occur).");

        // Data preparation
        User user = createUser();
        userRepository.save(user);
        assertDatabaseSize(1, 0);

        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setPasswordHash(user.getPasswordHash());
        user1.setRoles(user.getRoles());

        // Sign-in user
        signInUser(user1);

        // Execution
        Either<Failure, Success<ProfileDtoOut>> profile = userService.getProfile();

        // Verification
        assertTrue(profile.isLeft());
        assertDatabaseSize(1, 0);

        assertEquals(HttpStatus.NOT_FOUND, profile.getLeft().getHttpStatus());

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, profile.getLeft().getMessage());
    }

    @Test
    void update_Hds() {
        log.info("Test the user (profile) update.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);
        assertDatabaseSize(1, 0);

        ZonedDateTime createdAt = user.getCreatedAt();

        // Sign-in user
        signInUser(user);

        UpdateUserDtoIn dtoIn = createUpdateUserDtoIn();

        // Execution
        Either<Failure, Success<UpdateUserDtoOut>> update = userService.update(dtoIn);

        // Verification
        assertTrue(update.isRight());
        assertDatabaseSize(1, 0);

        assertEquals(HttpStatus.OK, update.get().getHttpStatus());

        UpdateUserDtoOut userDtoOut = update.get().getBody();
        assertEquals("Ned", userDtoOut.getFirstName());
        assertEquals("Flanders", userDtoOut.getLastName());
        assertEquals("ned.flanders@seznam.ua", userDtoOut.getEmail());
        assertEquals(ACCOUNT_ID, userDtoOut.getAccountId());
        assertEquals("987 654 321", userDtoOut.getTelephoneNumber());
        assertEquals("Changed Street 123", userDtoOut.getAddress().getStreet());
        assertEquals("Changed City", userDtoOut.getAddress().getCity());
        assertEquals(987654, userDtoOut.getAddress().getPsc());

        Optional<User> userById = userRepository.findById(user.getId());
        assertTrue(userById.isPresent());
        assertTrue(createdAt.isBefore(userById.get().getUpdatedAt()));
    }

    @Test
    void update_InvalidDtoIn() {
        log.info("Test the user (profile) update. DtoIn will not contain valid values.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);
        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        UpdateUserDtoIn dtoIn = createUpdateUserDtoIn();
        dtoIn.setFirstName("123456");
        dtoIn.setLastName("        ");
        dtoIn.setOriginalEmail("@invalid-syntax.ua");
        dtoIn.setUpdatedEmail("!éíáýžřč");
        dtoIn.setAccountId(-555555555555555L);
        dtoIn.setTelephoneNumber("invalid syntax");
        dtoIn.getAddress().setStreet("123 invalid");
        dtoIn.getAddress().setCity(null);
        dtoIn.getAddress().setPsc(-1);

        // Execution
        Either<Failure, Success<UpdateUserDtoOut>> update = userService.update(dtoIn);

        // Verification
        assertTrue(update.isLeft());
        assertDatabaseSize(1, 0);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, update.getLeft().getHttpStatus());

        String expectedMessage = "First name contains invalid characters: '123456'., Last name must contain at least 4 characters., Original email can contain numbers, underscores, decimal points or uppercase and lowercase letters without diacritics., New email must contain at least 10 characters., Account number must be a positive number greater than zero., The phone number contains illegal characters: ' adilnstvxy'., The street must be in syntax 'Street 123'., City must contain at least 2 characters., The zip code does not match the required syntax.";
        assertEquals(expectedMessage, update.getLeft().getMessage());
    }

    @Test
    void update_UserNotSignedIn() {
        log.info("Test the user (profile) update. An error occurs when getting the signed-in user. The error occurs because no user will be signed-in, but this should not occur because the unregistered user will not be able to call the appropriate method on the controller.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);
        assertDatabaseSize(1, 0);

        UpdateUserDtoIn dtoIn = createUpdateUserDtoIn();

        // Execution
        Either<Failure, Success<UpdateUserDtoOut>> update = userService.update(dtoIn);

        // Verification
        assertTrue(update.isLeft());
        assertDatabaseSize(1, 0);

        assertEquals(HttpStatus.UNAUTHORIZED, update.getLeft().getHttpStatus());

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, update.getLeft().getMessage());
    }

    @Test
    void update_UserNotFoundById() {
        log.info("Test the user (profile) update. The user in the database will not be found by the id obtained from the user in the Spring context.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);
        assertDatabaseSize(1, 0);

        // Sign-in user
        User differentUser = new User();
        differentUser.setId(user.getId() + 1);
        differentUser.setPasswordHash(user.getPasswordHash());
        differentUser.setRoles(user.getRoles());
        signInUser(differentUser);

        UpdateUserDtoIn dtoIn = createUpdateUserDtoIn();

        // Execution
        Either<Failure, Success<UpdateUserDtoOut>> update = userService.update(dtoIn);

        // Verification
        assertTrue(update.isLeft());
        assertDatabaseSize(1, 0);

        assertEquals(HttpStatus.NOT_FOUND, update.getLeft().getHttpStatus());

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, update.getLeft().getMessage());
    }

    @Test
    void update_SignedInUserIsDifferentFromModifiedUserInDtoIn() {
        log.info("Test the user (profile) update. The signed-in user (in the Spring context) is different from the one whose data is modified, which should not occur, the user can only edit his data.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);
        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        UpdateUserDtoIn dtoIn = createUpdateUserDtoIn();
        dtoIn.setOriginalEmail("different.email.from.signedIn.user@gmail.com");

        // Execution
        Either<Failure, Success<UpdateUserDtoOut>> update = userService.update(dtoIn);

        // Verification
        assertTrue(update.isLeft());
        assertDatabaseSize(1, 0);

        assertEquals(HttpStatus.UNAUTHORIZED, update.getLeft().getHttpStatus());

        String expectedMessage = "The signed-in user has a different email than specified in dtoIn as the user to whom the data in the profile should be changed.";
        assertEquals(expectedMessage, update.getLeft().getMessage());
    }

    @Test
    void update_DuplicateEmail() {
        log.info("Test the user (profile) update. This will include updating (among other things) an email address to an email that already exists.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        String duplicityEmail = "test.duplicity@gmail.com";
        User user2 = createUser();
        user2.setEmail(duplicityEmail);
        userRepository.save(user2);

        assertDatabaseSize(2, 0);

        // Sign-in user
        signInUser(user);

        UpdateUserDtoIn dtoIn = createUpdateUserDtoIn();
        dtoIn.setUpdatedEmail(duplicityEmail);

        // Execution
        Either<Failure, Success<UpdateUserDtoOut>> update = userService.update(dtoIn);

        // Verification
        assertTrue(update.isLeft());
        assertDatabaseSize(2, 0);

        assertEquals(HttpStatus.CONFLICT, update.getLeft().getHttpStatus());

        String expectedMessage = "Email already used.";
        assertEquals(expectedMessage, update.getLeft().getMessage());
    }

    @Test
    void delete_Hds() {
        log.info("Test deleting user (include his / her transactions).");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        List<Transaction> transactionList = TransactionSupport.createTransactionList(1, 6, ACCOUNT_ID, Direction.INCOMING);
        transactionList.addAll(TransactionSupport.createTransactionList(5, 10, ACCOUNT_ID, Direction.OUTGOING));
        transactionRepository.saveAll(transactionList);

        assertDatabaseSize(1, 3, 10);

        // Sign-in user
        signInUser(user);

        DeleteUserDtoIn dtoIn = new DeleteUserDtoIn();
        dtoIn.setEmail(user.getEmail());
        // Execution
        Either<Failure, Success<UserDtoOut>> delete = userService.delete(dtoIn);

        // Verification
        assertTrue(delete.isRight());
        assertEmptyDatabase();
    }

    @Test
    void delete_WrongEmail() {
        log.info("User deletion test, a non-existent email will be entered, resp. the user with the specified email will not be in the database.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        DeleteUserDtoIn dtoIn = new DeleteUserDtoIn();
        dtoIn.setEmail("nonExistentEmail@gmail.com");
        // Execution
        Either<Failure, Success<UserDtoOut>> delete = userService.delete(dtoIn);

        // Verification
        assertTrue(delete.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, delete.getLeft().getHttpStatus());
        String expectedMessage = "The signed-in user has a different email than the user to be deleted according to the email address.";
        assertEquals(expectedMessage, delete.getLeft().getMessage());

        assertDatabaseSize(1, 0);
    }

    @Test
    void delete_InvalidDtoIn() {
        log.info("Test deleting user by email. Email (dtoIn) will not be valid.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        String email = "_!?:@gmail.com";

        DeleteUserDtoIn dtoIn = new DeleteUserDtoIn();
        dtoIn.setEmail(email);
        // Execution
        Either<Failure, Success<UserDtoOut>> delete = userService.delete(dtoIn);

        // Verification
        assertTrue(delete.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, delete.getLeft().getHttpStatus());
        String expectedMessage = "Email contains invalid characters: '!:?'.";
        assertEquals(expectedMessage, delete.getLeft().getMessage());

        assertDatabaseSize(1, 0);
    }

    @Test
    void delete_NoUserSignedIn() {
        log.info("Test deleting user. No user signed-in. The user can only delete the user who is signed-in (delete himself) or admin, but it is not implemented.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        DeleteUserDtoIn dtoIn = new DeleteUserDtoIn();
        dtoIn.setEmail(user.getEmail());
        // Execution
        Either<Failure, Success<UserDtoOut>> delete = userService.delete(dtoIn);

        // Verification
        assertTrue(delete.isLeft());
        assertDatabaseSize(1, 0);

        assertEquals(HttpStatus.UNAUTHORIZED, delete.getLeft().getHttpStatus());

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, delete.getLeft().getMessage());

        assertDatabaseSize(1, 0);
    }

    @Test
    void delete_UserNotFoundById() {
        log.info("Test deleting user. The user will not be found in the database according to the id obtained from the Spring context (token).");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setEmail(user.getEmail());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        DeleteUserDtoIn dtoIn = new DeleteUserDtoIn();
        dtoIn.setEmail(user.getEmail());
        // Execution
        Either<Failure, Success<UserDtoOut>> delete = userService.delete(dtoIn);

        // Verification
        assertTrue(delete.isLeft());
        assertDatabaseSize(1, 0);

        assertEquals(HttpStatus.NOT_FOUND, delete.getLeft().getHttpStatus());

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, delete.getLeft().getMessage());

        assertDatabaseSize(1, 0);
    }

    @Test
    void changePassword_Hds() {
        log.info("Test the password change of the signed-in user.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        ChangePasswordDtoIn dtoIn = createChangePasswordDtoIn();
        // Execution
        Either<Failure, Success<ChangePasswordDtoOut>> changePassword = userService.changePassword(dtoIn);

        // Verification
        assertTrue(changePassword.isRight());
        assertEquals(HttpStatus.OK, changePassword.get().getHttpStatus());

        String expectedMessage = "Password changed successfully.";
        assertEquals(expectedMessage, changePassword.get().getBody().getMessage());

        assertDatabaseSize(1, 0);

        Optional<User> optUserById = userRepository.findById(user.getId());
        assertTrue(optUserById.isPresent());

        User userPwd = optUserById.get();
        assertTrue(passwordEncoder.matches(CHANGED_PASSWORD, userPwd.getPasswordHash()));
        assertTrue(userPwd.getCreatedAt().isBefore(userPwd.getUpdatedAt()));
    }

    @Test
    void changePassword_InvalidDtoIn() {
        log.info("Test the password change of the signed-in user. The input dtoIn will contain invalid values.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        ChangePasswordDtoIn dtoIn = createChangePasswordDtoIn();
        dtoIn.setEmail(null);
        dtoIn.setOriginalPassword("123456789");
        dtoIn.setNewPassword("          ");
        dtoIn.setConfirmationPassword(TextTestSupport.generateInvalidText(50L, "Password"));

        // Execution
        Either<Failure, Success<ChangePasswordDtoOut>> changePassword = userService.changePassword(dtoIn);

        // Verification
        assertTrue(changePassword.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, changePassword.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "Email must contain at least 10 characters., Original password must contain at least one digit, a case-sensitive character, accented letter, and a special character, such as '@#$%^&+=!/\\\\*|'-;_():<>{}[]' etc., New password must contain at least 8 characters., Confirmation password can contain up to 50 characters., The new password and the confirmation of the new password are different.";
        assertEquals(expectedMessage, changePassword.getLeft().getMessage());
    }

    @Test
    void changePassword_UserNotSignedIn() {
        log.info("Test the password change of the signed-in user. The user will not be signed-in to change the password. Only signed-in users can change their password only.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        ChangePasswordDtoIn dtoIn = createChangePasswordDtoIn();

        // Execution
        Either<Failure, Success<ChangePasswordDtoOut>> changePassword = userService.changePassword(dtoIn);

        // Verification
        assertTrue(changePassword.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, changePassword.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, changePassword.getLeft().getMessage());
    }

    @Test
    void changePassword_UserNotFoundById() {
        log.info("Test the password change of the signed-in user. The user will not be found in the database by the id obtained from the Spring context (token).");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setPasswordHash(user.getPasswordHash());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        ChangePasswordDtoIn dtoIn = createChangePasswordDtoIn();

        // Execution
        Either<Failure, Success<ChangePasswordDtoOut>> changePassword = userService.changePassword(dtoIn);

        // Verification
        assertTrue(changePassword.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, changePassword.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, changePassword.getLeft().getMessage());
    }

    @Test
    void changePassword_DifferentEmail() {
        log.info("Test the password change of the signed-in user. A different email will be entered in dtoIn than the email of the signed-in user. This is a simulation that a user is trying to change a foreign password.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        ChangePasswordDtoIn dtoIn = createChangePasswordDtoIn();
        dtoIn.setEmail("different.email@seznam.ua");

        // Execution
        Either<Failure, Success<ChangePasswordDtoOut>> changePassword = userService.changePassword(dtoIn);

        // Verification
        assertTrue(changePassword.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, changePassword.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "The signed-in user has a different email than the one specified in dtoIn whose password is to be changed.";
        assertEquals(expectedMessage, changePassword.getLeft().getMessage());
    }

    @Test
    void changePassword_DifferentPasswords_UserAndDtoIn() {
        log.info("Test the password change of the signed-in user. A password other than the current password of the signed-in user will be entered in dtoIn. Simulating a user trying to change another user's password.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        ChangePasswordDtoIn dtoIn = createChangePasswordDtoIn();
        dtoIn.setOriginalPassword("Pwd-ě_123@/!-Unknown");

        // Execution
        Either<Failure, Success<ChangePasswordDtoOut>> changePassword = userService.changePassword(dtoIn);

        // Verification
        assertTrue(changePassword.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, changePassword.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "The current password does not match the signed-in user's password.";
        assertEquals(expectedMessage, changePassword.getLeft().getMessage());
    }

    @Test
    void changePassword_DifferentPasswords_InDtoIn() {
        log.info("Test the password change of the signed-in user. In the input dtoIn there are different passwords for entering new password and its confirmation.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        ChangePasswordDtoIn dtoIn = createChangePasswordDtoIn();
        dtoIn.setNewPassword(CHANGED_PASSWORD + "Unknown");

        // Execution
        Either<Failure, Success<ChangePasswordDtoOut>> changePassword = userService.changePassword(dtoIn);

        // Verification
        assertTrue(changePassword.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, changePassword.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "The new password and the confirmation of the new password are different.";
        assertEquals(expectedMessage, changePassword.getLeft().getMessage());
    }
}
