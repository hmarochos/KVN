package ua.uhk.mois.financialplanning.model.dto.user;

import lombok.Data;

/**
 * @author KVN
 * @since 16.03.2021 0:29
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
