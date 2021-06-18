package cz.uhk.mois.financialplanning.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author Jan Krunčík
 * @since 15.03.2020 22:59
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Success<T> {

    private HttpStatus httpStatus;

    private T body;

    public ResponseEntity<ServerResponse<T>> createResponse() {
        ServerResponse<T> serverResponse = createServerResponse();
        return ResponseEntity.status(httpStatus)
                             .body(serverResponse);
    }

    private ServerResponse<T> createServerResponse() {
        return ServerResponse.<T>builder()
                .body(body)
                .build();
    }
}
