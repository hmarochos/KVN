package ua.uhk.mois.financialplanning.model.dto.planning.year;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author KVN
 * @since 11.04.2021 11:31
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AnnualPlanningDtoIn {

    /**
     * This is a user-spared baseline from which to start planning to achieve a wishes. <br/>
     * <i>Amounts are considered in uaech crowns.</i>
     */
    private BigDecimal amountSaved;

    /**
     * Net monthly profit, which will be added to the savings amount amountSaved each month. <br/>
     * <i>Amounts are considered in uaech crowns.</i>
     */
    private BigDecimal monthlyProfit;
}
