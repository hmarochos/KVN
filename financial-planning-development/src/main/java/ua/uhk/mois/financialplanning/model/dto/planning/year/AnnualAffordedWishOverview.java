package ua.uhk.mois.financialplanning.model.dto.planning.year;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author KVN
 * @since 11.04.2021 13:34
 */

@Data
@Builder
@ToString
public class AnnualAffordedWishOverview {

    /**
     * Index of the year when the user will be able to afford the wish given in wishName.
     */
    private int yearIndex;

    /**
     * The name of the wish that the user will be able to afford in the year on the index yearIndex.
     */
    private String wishName;
}
