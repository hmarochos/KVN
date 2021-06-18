package cz.uhk.mois.financialplanning.model.dto.planning.year;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author Jan Krunčík
 * @since 11.04.2020 13:29
 */

@Data
@Builder
@ToString
public class AnnualOverview {

    /**
     * Index of the year when the user will be able to afford a particular wish.
     */
    private int yearIndex;

    /**
     * Amount saved in the relevant year (yearIndex).
     */
    private BigDecimal amountSaved;
}
