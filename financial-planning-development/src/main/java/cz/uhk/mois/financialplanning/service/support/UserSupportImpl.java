package cz.uhk.mois.financialplanning.service.support;

import cz.uhk.mois.financialplanning.model.entity.user.User;
import cz.uhk.mois.financialplanning.repository.UserRepository;
import cz.uhk.mois.financialplanning.response.Failure;
import cz.uhk.mois.financialplanning.response.FailureSupport;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Jan Krunčík
 * @since 22.03.2020 20:06
 */

@Component
@Log4j2
public class UserSupportImpl implements UserSupport {

    private final UserRepository userRepository;

    public UserSupportImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static Failure findUserByIdFailure(Throwable throwable, Long id) {
        String logMessage = String.format("An error occurred while trying to load a user by id '%s' from the database.", id);
        log.error(logMessage, throwable);
        String message = "An error occurred while trying to load a user from the database.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private static Failure saveUserFailure(Throwable throwable, User user) {
        String logMessage = String.format("An error occurred while trying to save the user to the database. %s", user);
        log.error(logMessage, throwable);
        String responseMessage = "An error occurred while trying to save the user to the database.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, responseMessage);
    }

    @Override
    public Either<Failure, User> findUserById(Long id) {
        log.info("Loading user from database by id {}.", id);

        Either<Failure, Optional<User>> userById = loadUserById(id);

        if (userById.isLeft()) {
            return Either.left(userById.getLeft());
        }

        Optional<User> optionalUserById = userById.get();
        if (!optionalUserById.isPresent()) {
            log.error(String.format("User not found by id '%s'.", id));
            return Either.left(FailureSupport.createFailure(HttpStatus.NOT_FOUND, "User not found."));
        }

        return Either.right(optionalUserById.get());
    }

    @Override
    public Either<Failure, User> saveUser(User user) {
        return Try.of(() -> userRepository.save(user))
                  .toEither()
                  .mapLeft(throwable -> saveUserFailure(throwable, user));
    }

    private Either<Failure, Optional<User>> loadUserById(Long id) {
        return Try.of(() -> userRepository.findById(id))
                  .toEither()
                  .mapLeft(throwable -> findUserByIdFailure(throwable, id));
    }
}
