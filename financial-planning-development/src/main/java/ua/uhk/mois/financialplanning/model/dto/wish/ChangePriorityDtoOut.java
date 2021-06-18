package ua.uhk.mois.financialplanning.model.dto.wish;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author KVN
 * @since 10.04.2021 13:44
 */

@Data
@NoArgsConstructor
public class ChangePriorityDtoOut {

    /**
     * Information that the wishes listed in dtoIn have been successfully changed priority.
     */
    private String message;

}
