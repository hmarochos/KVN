package ua.uhk.mois.financialplanning.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author KVN
 * @since 16.03.2021 12:41
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateUserDtoIn {

    private String firstName;

    private String lastName;

    private String originalEmail;

    private String updatedEmail;

    private Long accountId;

    private String telephoneNumber;

    private AddressDto address;
}
