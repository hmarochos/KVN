package cz.uhk.mois.financialplanning.model.dto.planning.month;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author Jan Krunčík
 * @since 11.04.2020 0:09
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MonthlyPlanningDtoIn {

    /**
     * This is a user-spared baseline from which to start planning to achieve a wishes. <br/>
     * <i>Amounts are considered in Czech crowns.</i>
     */
    private BigDecimal amountSaved;

    /**
     * Net monthly profit, which will be added to the savings amount (amountSaved) each month. <br/>
     * <i>Amounts are considered in Czech crowns.</i>
     */
    private BigDecimal monthlyProfit;

}
