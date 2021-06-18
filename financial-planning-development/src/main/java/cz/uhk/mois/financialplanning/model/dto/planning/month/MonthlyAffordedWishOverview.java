package cz.uhk.mois.financialplanning.model.dto.planning.month;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author Jan Krunčík
 * @since 11.04.2020 1:46
 */

@Data
@Builder
@ToString
public class MonthlyAffordedWishOverview {

    /**
     * Index of the month when the user will be able to afford the wish given in wishName.
     */
    private int monthIndex;

    /**
     * The name of the wish that the user will be able to afford in the month on the index monthIndex.
     */
    private String wishName;

}
