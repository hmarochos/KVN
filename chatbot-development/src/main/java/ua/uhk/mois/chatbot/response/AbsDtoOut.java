package ua.uhk.mois.chatbot.response;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author KVN
 * @since 15.03.2021 20:21
 */

@Data
public abstract class AbsDtoOut<T> {

    public ResponseEntity<ServerResponse<T>> createResponse(HttpStatus httpStatus, String message) {
        ServerResponse<T> serverResponse = createServerResponse(message);
        return ResponseEntity.status(httpStatus)
                             .body(serverResponse);
    }

    public ResponseEntity<ServerResponse<T>> createResponse(HttpStatus httpStatus, T body) {
        ServerResponse<T> serverResponse = createServerResponse(body);
        return ResponseEntity.status(httpStatus)
                             .body(serverResponse);
    }

    private ServerResponse<T> createServerResponse(String message) {
        return ServerResponse.<T>builder()
                .message(message)
                .build();
    }

    public ServerResponse<T> createServerResponse(T body) {
        return ServerResponse.<T>builder()
                .body(body)
                .build();
    }
}
