package ua.uhk.mois.financialplanning.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author KVN
 * @since 22.03.2021 8:21
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"originalPassword", "newPassword"})
public class ChangePasswordDtoIn {

    /**
     * Email of the user whose password is to be changed.
     */
    private String email;

    /**
     * Original / Current password of the signed in user (to confirm identity).
     */
    private String originalPassword;

    /**
     * New password for user with the above username and email.
     */
    private String newPassword;

    /**
     * Confirm password (must be the same as newPassword).
     */
    private String confirmationPassword;
}
