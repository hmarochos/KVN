package cz.uhk.mois.financialplanning.model.dto.wish;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 10.04.2020 13:44
 */

@Data
@NoArgsConstructor
public class ChangePriorityDtoOut {

    /**
     * Information that the wishes listed in dtoIn have been successfully changed priority.
     */
    private String message;

}
