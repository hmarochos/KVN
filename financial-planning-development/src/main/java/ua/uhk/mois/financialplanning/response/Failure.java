package ua.uhk.mois.financialplanning.response;

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

}
