package cz.uhk.mois.financialplanning.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 22.03.2020 8:21
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordDtoOut {

    private String message;
}
