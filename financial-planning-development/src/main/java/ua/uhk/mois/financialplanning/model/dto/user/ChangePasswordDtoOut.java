package ua.uhk.mois.financialplanning.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author KVN
 * @since 22.03.2021 8:21
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordDtoOut {

    private String message;
}
