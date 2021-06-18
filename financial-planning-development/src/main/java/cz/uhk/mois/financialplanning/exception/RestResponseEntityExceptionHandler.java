package cz.uhk.mois.financialplanning.exception;

import cz.uhk.mois.financialplanning.response.ServerResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Handling all exceptions. <br/>
 * <i>Return matching object for easy recognition on FE.</i>
 *
 * @author Jan Krunčík
 * @since 28.03.2020 5:03
 */

@ControllerAdvice
@Log4j2
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected static ResponseEntity<ServerResponse> handleConflict(RuntimeException ex, WebRequest request) {
        log.error("An unattended error was processed.", ex);

        String message = "An error occurred while trying to process the request. " + ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(ServerResponse.builder()
                                                 .message(message)
                                                 .build());
    }
}
