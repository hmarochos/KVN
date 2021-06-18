package cz.uhk.mois.financialplanning.model.dto.user;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jan Krunčík
 * @since 16.03.2020 12:17
 */

@Data
@ToString
public class DeleteUserDtoIn {

    private String email;
}
