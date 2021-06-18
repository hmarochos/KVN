package cz.uhk.mois.financialplanning.model.dto.planning;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author Jan Krunčík
 * @since 11.04.2020 14:22
 */

@Data
@Builder
@ToString
public class FinancialPlanOverview {

    /**
     * Index of the month or year when the user will be able to afford a particular wish.
     */
    private int index;

    /**
     * Amount saved in the relevant month or year (index).
     */
    private BigDecimal amountSaved;

}
