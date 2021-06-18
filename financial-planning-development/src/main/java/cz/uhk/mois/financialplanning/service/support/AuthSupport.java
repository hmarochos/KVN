package cz.uhk.mois.financialplanning.service.support;

import cz.uhk.mois.financialplanning.response.Failure;
import cz.uhk.mois.financialplanning.response.FailureSupport;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * @author Jan Krunčík
 * @since 22.03.2020 20:11
 */

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthSupport {

    public static Either<Failure, OAuth2Authentication> getSignedInUser() {
        return Try.of(() -> (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication())
                  .toEither()
                  .mapLeft(AuthSupport::getSignedInUserFailure);
    }

    /**
     * Check if any user is really signed-in (context-based user is not null).
     *
     * @param auth2Authentication
     *         signed-in user information (read from Spring context)
     *
     * @return right with method parameter (object with information about signed-in user in Spring context), otherwise
     * left with information that user is not singed-in (if auth2Authentication parameter is null)
     */
    public static Either<Failure, OAuth2Authentication> checkSignedInUser(OAuth2Authentication auth2Authentication) {
        if (auth2Authentication == null) {
            String message = "No user signed-in.";
            log.error(message);
            return Either.left(FailureSupport.createFailure(HttpStatus.UNAUTHORIZED, message));
        }
        return Either.right(auth2Authentication);
    }

    /**
     * Get the user id from the singed-in user's (token) in the Spring context.
     *
     * @param auth2Authentication
     *         signed-in user information (in the Spring context)
     *
     * @return right with the user record id in the database, otherwise left with the error information
     */
    public static Either<Failure, Long> getIdFromSignedInUserToken(OAuth2Authentication auth2Authentication) {
        return Try.of(() -> {
            Object id = auth2Authentication.getPrincipal();
            return Long.parseLong(id.toString());
        })
                  .toEither()
                  .mapLeft(throwable -> getIdFromSignedInUserTokenFailure(throwable, auth2Authentication));
    }

    private static Failure getSignedInUserFailure(Throwable throwable) {
        log.error("There was an error trying to get the signed-in user.", throwable);
        String message = "Failed to get signed-in user.";
        return FailureSupport.createFailure(HttpStatus.UNAUTHORIZED, message);
    }

    private static Failure getIdFromSignedInUserTokenFailure(Throwable throwable, OAuth2Authentication auth2Authentication) {
        String logMessage = String.format("An error occurred while trying to get an id from the signed-in user token. %s", auth2Authentication);
        log.error(logMessage, throwable);
        String message = "There was an error retrieving signed-in user data.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
