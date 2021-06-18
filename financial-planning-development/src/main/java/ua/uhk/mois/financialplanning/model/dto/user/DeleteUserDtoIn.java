package ua.uhk.mois.financialplanning.model.dto.user;

import lombok.Data;
import lombok.ToString;

/**
 * @author KVN
 * @since 16.03.2021 12:17
 */

@Data
@ToString
public class DeleteUserDtoIn {

    private String email;
}
