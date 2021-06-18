package ua.uhk.mois.financialplanning.model.dto.user;

import ua.uhk.mois.financialplanning.model.entity.user.Role;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author KVN
 * @since 15.03.2021 19:42
 */

@Data
@ToString
public class UserDtoOut {

    private String firstName;

    private String lastName;

    private String email;

    private Long accountId;

    private String telephoneNumber;

    private AddressDto address;

    private List<Role> roles;
}
