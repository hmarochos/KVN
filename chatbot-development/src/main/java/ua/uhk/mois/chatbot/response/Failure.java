package ua.uhk.mois.chatbot.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * @author KVN
 * @since 15.03.2021 21:05
 */

@Data
@Builder
public class Failure {

    private HttpStatus httpStatus;

    private String message;

    public static Failure createFailure(HttpStatus httpStatus, String message) {
        return builder()
                .httpStatus(httpStatus)
                .message(message)
                .build();
    }
}
