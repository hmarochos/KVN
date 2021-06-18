package cz.uhk.mois.financialplanning.model.dto.user;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jan Krunčík
 * @since 20.03.2020 0:06
 */

@Data
@ToString
public class UpdateUserDtoOut {

    private String firstName;

    private String lastName;

    private String email;

    private Long accountId;

    private String telephoneNumber;

    private AddressDto address;
}
