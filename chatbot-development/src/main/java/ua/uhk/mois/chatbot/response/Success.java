package ua.uhk.mois.chatbot.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * @author KVN
 * @since 15.03.2021 22:59
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Success<T> {

    private HttpStatus httpStatus;

    private T body;
}
