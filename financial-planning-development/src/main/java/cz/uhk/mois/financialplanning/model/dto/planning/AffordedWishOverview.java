package cz.uhk.mois.financialplanning.model.dto.planning;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author Jan Krunčík
 * @since 11.04.2020 14:25
 */

@Data
@Builder
@ToString
public class AffordedWishOverview {

    /**
     * Index of the month or year when the user will be able to afford the wish given in wishName.
     */
    private int index;

    /**
     * The name of the wish that the user will be able to afford in the month or year on the index.
     */
    private String wishName;

}
