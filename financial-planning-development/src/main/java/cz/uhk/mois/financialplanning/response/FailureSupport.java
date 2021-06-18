package cz.uhk.mois.financialplanning.response;

import cz.uhk.mois.financialplanning.validation.ValidationSupport;
import io.vavr.collection.Seq;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;

/**
 * @author Jan Krunčík
 * @since 28.03.2020 10:33
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class FailureSupport {

    public static Failure createFailure(HttpStatus httpStatus, String message) {
        return Failure.builder()
                      .httpStatus(httpStatus)
                      .message(message)
                      .build();
    }

    public static Failure validationFailure(Seq<String> validationViolations, String objName) {
        String validationViolationsMsg = ValidationSupport.removeListText(validationViolations.toString());
        String logMsg = String.format("%s is not valid. %s", objName, validationViolationsMsg);
        log.error(logMsg);
        return createFailure(HttpStatus.UNPROCESSABLE_ENTITY, validationViolationsMsg);
    }

    public static Failure validationFailure(String validationViolation, String objName) {
        String logMsg = String.format("%s is not valid. %s", objName, validationViolation);
        log.error(logMsg);
        return createFailure(HttpStatus.UNPROCESSABLE_ENTITY, validationViolation);
    }
}
