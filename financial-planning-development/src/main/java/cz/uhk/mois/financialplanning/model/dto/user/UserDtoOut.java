package cz.uhk.mois.financialplanning.model.dto.user;

import cz.uhk.mois.financialplanning.model.entity.user.Role;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Jan Krunčík
 * @since 15.03.2020 19:42
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
