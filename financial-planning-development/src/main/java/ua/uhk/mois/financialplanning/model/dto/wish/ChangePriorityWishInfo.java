package ua.uhk.mois.financialplanning.model.dto.wish;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Information about the wish whose priority is to be changed.
 *
 * @author KVN
 * @since 10.04.2021 13:40
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChangePriorityWishInfo {

    /**
     * The Id of the wish to change priority.
     */
    private Long id;

    /**
     * Wish name (/ title). Respectively "basic" information about the identification of a particular wish. It is needed
     * only for cases of validation, when the user will not have this wish stored in the database so the id will not be
     * returned, but a text message will be.
     */
    private String name;

    /**
     * New priority to set wish with id above.
     */
    private Integer priority;
}
