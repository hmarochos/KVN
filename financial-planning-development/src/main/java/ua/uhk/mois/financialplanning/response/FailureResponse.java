package ua.uhk.mois.financialplanning.response;

import lombok.Data;
import org.springframework.http.ResponseEntity;

/**
 * @author KVN
 * @since 04.05.2021 3:13
 */

@Data
public class FailureResponse<T> {

    public ResponseEntity<ServerResponse<T>> createResponse(Failure failure) {
        ServerResponse<T> serverResponse = createServerResponse(failure.getMessage());
        return ResponseEntity.status(failure.getHttpStatus())
                             .body(serverResponse);
    }

    private ServerResponse<T> createServerResponse(String message) {
        return ServerResponse.<T>builder()
                .message(message)
                .build();
    }

}
