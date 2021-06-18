package ua.uhk.mois.financialplanning.model.dto.user;

import ua.uhk.mois.financialplanning.model.entity.user.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author KVN
 * @since 15.03.2021 19:42
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password", "passwordConfirmation"})
public class CreateUserDtoIn {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String passwordConfirmation;

    private Long accountId;

    private String telephoneNumber;

    private AddressDto address;

    private List<Role> roles;
}
