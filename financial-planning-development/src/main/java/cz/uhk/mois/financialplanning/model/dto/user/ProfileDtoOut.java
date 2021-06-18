package cz.uhk.mois.financialplanning.model.dto.user;

import lombok.Data;

/**
 * @author Jan Krunčík
 * @since 16.03.2020 0:29
 */

@Data
public class ProfileDtoOut {

    private String firstName;

    private String lastName;

    private String email;

    private Long accountId;

    private String telephoneNumber;

    private AddressDto address;
}
