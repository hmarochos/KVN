package ua.uhk.mois.financialplanning.service;

import ua.uhk.mois.financialplanning.model.dto.user.ChangePasswordDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.ChangePasswordDtoOut;
import ua.uhk.mois.financialplanning.model.dto.user.CreateUserDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.DeleteUserDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.ProfileDtoOut;
import ua.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoOut;
import ua.uhk.mois.financialplanning.model.dto.user.UserDtoOut;
import ua.uhk.mois.financialplanning.model.entity.user.Role;
import ua.uhk.mois.financialplanning.model.entity.user.User;
import ua.uhk.mois.financialplanning.repository.TransactionRepository;
import ua.uhk.mois.financialplanning.repository.UserRepository;
import ua.uhk.mois.financialplanning.response.Failure;
import ua.uhk.mois.financialplanning.response.FailureSupport;
import ua.uhk.mois.financialplanning.response.Success;
import ua.uhk.mois.financialplanning.service.support.AuthSupport;
import ua.uhk.mois.financialplanning.service.support.UserSupport;
import ua.uhk.mois.financialplanning.validation.user.ChangePasswordDtoInValidator;
import ua.uhk.mois.financialplanning.validation.user.CreateUserDtoInValidator;
import ua.uhk.mois.financialplanning.validation.user.DeleteUserDtoInValidator;
import ua.uhk.mois.financialplanning.validation.user.UpdateUserDtoInValidator;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static ua.uhk.mois.financialplanning.service.support.AuthSupport.getSignedInUser;

/**
 * @author KVN
 * @since 15.03.2021 20:41
 */

@Component
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CopyTransactionService copyTransactionService;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final UserSupport userSupport;
    private final Clock clock;

    public UserServiceImpl(UserRepository userRepository, CopyTransactionService copyTransactionService, TransactionRepository transactionRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, UserSupport userSupport, Clock clock) {
        this.userRepository = userRepository;
        this.copyTransactionService = copyTransactionService;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.userSupport = userSupport;
        this.clock = clock;
    }

    private static Failure findUserByEmailFailure(Throwable throwable, String email) {
        String message = String.format("An error occurred while trying to load a user by email. %s", email);
        log.error(message, throwable);
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private static Success<UserDtoOut> createSuccessDtoOut(UserDtoOut dtoOut) {
        log.info("User successfully created. {}", dtoOut);
        return Success.<UserDtoOut>builder()
                .httpStatus(HttpStatus.CREATED)
                .body(dtoOut)
                .build();
    }

    private static Success<UpdateUserDtoOut> updateSuccessDtoOut(UpdateUserDtoOut dtoOut) {
        log.info("User successfully updated. {}", dtoOut);
        return Success.<UpdateUserDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    private static Success<ProfileDtoOut> getProfileSuccessDtoOut(ProfileDtoOut dtoOut) {
        log.info("User profile obtained successfully. {}", dtoOut);
        return Success.<ProfileDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    private static Success<UserDtoOut> deleteSuccessDtoOut(UserDtoOut dtoOut) {
        log.info("User successfully deleted. {}", dtoOut);
        return Success.<UserDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    private static Success<ChangePasswordDtoOut> changePasswordSuccessDtoOut(ChangePasswordDtoOut dtoOut) {
        log.info("Password successfully changed. {}", dtoOut);
        return Success.<ChangePasswordDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    private static Failure deleteByIdFailure(Throwable throwable, Long id) {
        String logMessage = String.format("There was an error trying to delete the user by id '%s'.", id);
        log.error(logMessage, throwable);

        String message = "An error occurred while trying to delete the user.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * Compare the signed-in user's emails with the dtoIn original email, which indicates the email of the user whose
     * data is to be changed.
     *
     * @param dtoIn
     *         input dtoIn with updated data
     * @param signedInUserEmail
     *         singed-in user email
     *
     * @return right with dtoIn in the method parameter if the signed-in user's email matches the original email in
     * dtoIn, which represents whose data is to be changed, otherwise left with the error information
     */
    private static Either<Failure, UpdateUserDtoIn> compareSignedInUserEmailWithOriginalEmail(UpdateUserDtoIn dtoIn, String signedInUserEmail) {
        if (!dtoIn.getOriginalEmail().equals(signedInUserEmail)) {
            String logMessage = String.format("The signed-in user has a different email '%s' than specified in dtoIn '%s' as the user to whom the data in the profile should be changed.", signedInUserEmail, dtoIn.getOriginalEmail());
            log.error(logMessage);
            String message = "The signed-in user has a different email than specified in dtoIn as the user to whom the data in the profile should be changed.";
            return Either.left(FailureSupport.createFailure(HttpStatus.UNAUTHORIZED, message));
        }
        return Either.right(dtoIn);
    }

    private static Failure checkUniqueEmailFailure(String email) {
        String logMessage = String.format("Unable to find user by email '%s'.", email);
        log.error(logMessage);
        String message = "Unable to find user by email.";
        return FailureSupport.createFailure(HttpStatus.NOT_FOUND, message);
    }

    private static Either<Failure, User> compareSignedInUserEmailWithUserEmailToBeDeleted(User user, DeleteUserDtoIn dtoIn) {
        if (!user.getEmail().equals(dtoIn.getEmail())) {
            String logMessage = String.format("The signed-in user has a different email '%s' than the user to be deleted according to the email address '%s'.", user.getEmail(), dtoIn.getEmail());
            log.error(logMessage);
            String message = "The signed-in user has a different email than the user to be deleted according to the email address.";
            return Either.left(FailureSupport.createFailure(HttpStatus.UNAUTHORIZED, message));
        }
        return Either.right(user);
    }

    private static Either<Failure, User> compareSignedInUserEmailWithUserEmailToChangePassword(User user, ChangePasswordDtoIn dtoIn) {
        if (!user.getEmail().equals(dtoIn.getEmail())) {
            String logMessage = String.format("The signed-in user has a different email '%s' than the one specified in dtoIn whose password is to be changed '%s'.", user.getEmail(), dtoIn.getEmail());
            log.error(logMessage);
            String message = "The signed-in user has a different email than the one specified in dtoIn whose password is to be changed.";
            return Either.left(FailureSupport.createFailure(HttpStatus.UNAUTHORIZED, message));
        }
        return Either.right(user);
    }

    private static Either<Failure, CreateUserDtoIn> checkSamePasswords(CreateUserDtoIn dtoIn) {
        if (!dtoIn.getPassword().equals(dtoIn.getPasswordConfirmation())) {
            String message = "The passwords are different.";
            log.error(message);
            return Either.left(FailureSupport.createFailure(HttpStatus.UNPROCESSABLE_ENTITY, message));
        }
        return Either.right(dtoIn);
    }

    private static Failure deleteAllByAccountIdFailure(Long accountId) {
        String logMessage = String.format("An error occurred while trying to delete last month's transactions related to account number %s.", accountId);
        log.error(logMessage);
        String message = "An error occurred while trying to delete last month's transactions.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private static Failure deleteUserTransactionsFailure(User user) {
        String logMessage = String.format("An error occurred while trying to delete user transactions from 'our' database. %s", user);
        log.error(logMessage);
        String message = "An error occurred while trying to delete your transactions.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    @Override
    public Either<Failure, Success<UserDtoOut>> add(CreateUserDtoIn dtoIn) {
        log.info("Add / Create New User. {}", dtoIn);

        return CreateUserDtoInValidator.validate(dtoIn)
                                       .mapError(validationViolations -> FailureSupport.validationFailure(validationViolations, "CreateUserDtoIn"))
                                       .toEither()
                                       .flatMap(UserServiceImpl::checkSamePasswords)
                                       .flatMap(this::checkUniqueEmail)
                                       .map(this::createUserObject)
                                       .flatMap(userSupport::saveUser)
                                       .map(this::copyAddedUserTransactions)
                                       .map(this::convertToDto)
                                       .map(UserServiceImpl::createSuccessDtoOut);
    }

    @Override
    public Either<Failure, Success<ProfileDtoOut>> getProfile() {
        log.info("Get the signed-in user's profile.");

        return getSignedInUser()
                .flatMap(AuthSupport::checkSignedInUser)
                .flatMap(AuthSupport::getIdFromSignedInUserToken)
                .flatMap(userSupport::findUserById)
                .map(user -> modelMapper.map(user, ProfileDtoOut.class))
                .map(UserServiceImpl::getProfileSuccessDtoOut);
    }

    @Override
    public Either<Failure, Success<UpdateUserDtoOut>> update(UpdateUserDtoIn dtoIn) {
        log.info("Update user data (profile). {}", dtoIn);

        return UpdateUserDtoInValidator.validate(dtoIn)
                                       .mapError(validationViolations -> FailureSupport.validationFailure(validationViolations, "UpdateUserDtoIn"))
                                       .toEither()
                                       .flatMap(validatedDtoIn -> getSignedInUser()
                                               .flatMap(AuthSupport::checkSignedInUser)
                                               .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                               .flatMap(userSupport::findUserById)
                                               .flatMap(user -> compareSignedInUserEmailWithOriginalEmail(validatedDtoIn, user.getEmail()))
                                               .flatMap(this::checkUniqueEmail)
                                               .map(user -> copyNewAccountNumberTransactions(user, validatedDtoIn))
                                               .map(user -> updateUserData(user, validatedDtoIn)))
                                       .flatMap(userSupport::saveUser)
                                       .map(savedUser -> modelMapper.map(savedUser, UpdateUserDtoOut.class))
                                       .map(UserServiceImpl::updateSuccessDtoOut);
    }

    @Override
    @Transactional
    public Either<Failure, Success<UserDtoOut>> delete(DeleteUserDtoIn dtoIn) {
        log.info("Delete user by email. {}", dtoIn);

        return DeleteUserDtoInValidator.validate(dtoIn)
                                       .mapError(validationViolation -> FailureSupport.validationFailure(validationViolation, "DeleteUserDtoIn"))
                                       .toEither()
                                       .flatMap(validatedDtoIn -> getSignedInUser()
                                               .flatMap(AuthSupport::checkSignedInUser)
                                               .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                               .flatMap(userSupport::findUserById)
                                               .flatMap(user -> compareSignedInUserEmailWithUserEmailToBeDeleted(user, validatedDtoIn)))
                                       .flatMap(this::deleteUserTransactions)
                                       .flatMap(this::deleteById)
                                       .map(this::convertToDto)
                                       .map(UserServiceImpl::deleteSuccessDtoOut);
    }

    @Override
    public Either<Failure, Success<ChangePasswordDtoOut>> changePassword(ChangePasswordDtoIn dtoIn) {
        log.info("Change the password of the signed-in user. {}", dtoIn);

        return ChangePasswordDtoInValidator.validate(dtoIn)
                                           .mapError(validationViolations -> FailureSupport.validationFailure(validationViolations, "ChangePasswordDtoIn"))
                                           .toEither()
                                           .flatMap(validatedDtoIn -> getSignedInUser()
                                                   .flatMap(AuthSupport::checkSignedInUser)
                                                   .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                                   .flatMap(userSupport::findUserById)
                                                   .flatMap(user -> compareSignedInUserEmailWithUserEmailToChangePassword(user, validatedDtoIn))
                                                   .flatMap(user -> checkCorrectPassword(user, validatedDtoIn))
                                                   .map(user -> changePassword(user, validatedDtoIn)))
                                           .flatMap(userSupport::saveUser)
                                           .map(user -> ChangePasswordDtoOut.builder().message("Password changed successfully.").build())
                                           .map(UserServiceImpl::changePasswordSuccessDtoOut);
    }

    private Either<Failure, User> checkCorrectPassword(User user, ChangePasswordDtoIn dtoIn) {
        if (!passwordEncoder.matches(dtoIn.getOriginalPassword(), user.getPasswordHash())) {
            String message = "The current password does not match the signed-in user's password.";
            log.error(message);
            return Either.left(FailureSupport.createFailure(HttpStatus.BAD_REQUEST, message));
        }
        return Either.right(user);
    }

    private User changePassword(User user, ChangePasswordDtoIn dtoIn) {
        String passwordHash = passwordEncoder.encode(dtoIn.getNewPassword());
        user.setPasswordHash(passwordHash);

        ZonedDateTime now = ZonedDateTime.now(clock);
        user.setUpdatedAt(now);

        return user;
    }

    private User updateUserData(User user, UpdateUserDtoIn dtoIn) {
        user.setFirstName(dtoIn.getFirstName());
        user.setLastName(dtoIn.getLastName());
        user.setEmail(dtoIn.getUpdatedEmail());
        user.setAccountId(dtoIn.getAccountId());
        user.setTelephoneNumber(dtoIn.getTelephoneNumber());

        user.getAddress().setStreet(dtoIn.getAddress().getStreet());
        user.getAddress().setCity(dtoIn.getAddress().getCity());
        user.getAddress().setPsc(dtoIn.getAddress().getPsc());

        ZonedDateTime now = ZonedDateTime.now(clock);
        user.setUpdatedAt(now);

        return user;
    }

    private Either<Failure, CreateUserDtoIn> checkUniqueEmail(CreateUserDtoIn dtoIn) {
        return checkUniqueEmail(dtoIn.getEmail())
                .mapLeft(failure -> failure)
                .map(s -> dtoIn);
    }

    private Either<Failure, String> checkUniqueEmail(String email) {
        Either<Failure, Optional<User>> userByEmail = findUserByEmail(email);

        if (userByEmail.isLeft()) {
            return Either.left(userByEmail.getLeft());
        }

        if (userByEmail.get().isPresent()) {
            log.error(String.format("Email '%s' is already used.", email));
            return Either.left(FailureSupport.createFailure(HttpStatus.CONFLICT, "Email already used."));
        }

        return Either.right(email);
    }

    private Either<Failure, User> checkUniqueEmail(UpdateUserDtoIn dtoIn) {
        Either<Failure, Optional<User>> userByOriginalEmail = findUserByEmail(dtoIn.getOriginalEmail());

        if (userByOriginalEmail.isLeft()) {
            return Either.left(userByOriginalEmail.getLeft());
        }

        Optional<User> optionalUserByOriginalEmail = userByOriginalEmail.get();
        if (!optionalUserByOriginalEmail.isPresent()) {
            return Either.left(checkUniqueEmailFailure(dtoIn.getOriginalEmail()));
        }

        // User whose data is to be changed
        User user = optionalUserByOriginalEmail.get();
        // User who should not be found, if it is found and it is not the same user as the above user, the email is already used by another user
        Either<Failure, Optional<User>> userByNewEmail = findUserByEmail(dtoIn.getUpdatedEmail());
        if (userByNewEmail.isLeft()) {
            return Either.left(userByNewEmail.getLeft());
        }

        Optional<User> optionalUserByNewEmail = userByNewEmail.get();
        if (!optionalUserByNewEmail.isPresent() || optionalUserByNewEmail.get().getId().equals(user.getId())) {
            // User not found, email not used or or it is the same user, then it's OK - the user wants to edit the data
            return Either.right(user);
        }

        String logMessage = String.format("Email '%s' is already used.", dtoIn.getUpdatedEmail());
        log.error(logMessage);
        return Either.left(FailureSupport.createFailure(HttpStatus.CONFLICT, "Email already used."));
    }

    private Either<Failure, Optional<User>> findUserByEmail(String email) {
        return Try.of(() -> userRepository.findByEmail(email))
                  .toEither()
                  .mapLeft(throwable -> findUserByEmailFailure(throwable, email));
    }

    private User createUserObject(CreateUserDtoIn dtoIn) {
        User user = modelMapper.map(dtoIn, User.class);
        user.setPasswordHash(passwordEncoder.encode(dtoIn.getPassword()));

        ZonedDateTime now = ZonedDateTime.now(clock);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setLastLogin(now);

        user.getAddress().setUser(user);

        if (dtoIn.getRoles() == null || dtoIn.getRoles().isEmpty()) {
            user.setRoles(Collections.singletonList(Role.USER));
        }

        return user;
    }

    private UserDtoOut convertToDto(User user) {
        return modelMapper.map(user, UserDtoOut.class);
    }

    private Either<Failure, User> deleteById(User user) {
        return Try.of(() -> {
            userRepository.deleteById(user.getId());
            return user;
        })
                  .toEither()
                  .mapLeft(throwable -> deleteByIdFailure(throwable, user.getId()));
    }

    private Either<Failure, User> deleteUserTransactions(User user) {
        log.info("Delete the transactions of the signed-in user (who is also to be deleted). {}", user);

        if (user.getAccountId() == null) {
            return Either.right(user);
        }

        return Try.of(() -> {
            transactionRepository.deleteAllByAccountId(user.getAccountId());
            return user;
        })
                  .toEither()
                  .mapLeft(throwable -> deleteUserTransactionsFailure(user));
    }

    /**
     * Copying user transactions from the bank to our database for the last month.
     *
     * @param user
     *         user whose transactions for the last month are to be copied from the bank database to our
     *
     * @return the user whose above-mentioned transactions were copied from the bank database to ours
     */
    private User copyAddedUserTransactions(User user) {
        if (user.getAccountId() != null) {
            copyTransactionService.copyTransactionsFromLastMonth(user.getAccountId());
        }
        return user;
    }

    /**
     * Check if a user has changed his / her account number, if so, it is need to copy their transactions for the last
     * month.
     *
     * @param user
     *         original / current user in the database, about which it is determined whether he has changed the account
     *         number (in dtoIn)
     * @param dtoIn
     *         new user information to determine if he has changed the account number
     *
     * @return user in the 'user' parameter (current user in the database)
     */
    private User copyNewAccountNumberTransactions(User user, UpdateUserDtoIn dtoIn) {
        // If account numbers are not entered or are the same, there is no need to copy transactions, they are already copied when the account is created or from the beginning of the month
        if (Objects.equals(user.getAccountId(), dtoIn.getAccountId())) {
            return user;
        }

        // Here the user changed the account number or entered it for the first time

        // Delete transactions with user's old accountId
        if (user.getAccountId() != null) {
            Try.run(() -> transactionRepository.deleteAllByAccountId(user.getAccountId()))
               .toEither()
               .mapLeft(throwable -> deleteAllByAccountIdFailure(user.getAccountId()));
        }

        // Copy the user's last month transactions related to the new account number
        if (dtoIn.getAccountId() != null) {
            copyTransactionService.copyTransactionsFromLastMonth(dtoIn.getAccountId());
        }

        return user;
    }
}
