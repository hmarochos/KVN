package ua.uhk.mois.financialplanning.model.dto.user;

import lombok.Data;
import lombok.ToString;

/**
 * @author KVN
 * @since 20.03.2021 0:06
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
