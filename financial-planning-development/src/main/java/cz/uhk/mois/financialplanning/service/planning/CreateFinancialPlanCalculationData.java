package cz.uhk.mois.financialplanning.service.planning;

import cz.uhk.mois.financialplanning.model.entity.wish.Wish;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * To pass the data needed to calculate a financial (/ savings) plan.
 *
 * @author Jan Krunčík
 * @since 11.04.2020 6:14
 */

@Data
@Builder
public class CreateFinancialPlanCalculationData {

    /**
     * Initial amount from which to calculate.
     */
    private BigDecimal amountSaved;

    /**
     * Expected monthly net profit.
     */
    private BigDecimal monthlyProfit;

    /**
     * Number of months or years for which the financial (/ savings) plan is to be calculated.
     */
    private Integer savingsPeriod;

    /**
     * List of wishes for which the savings plan is to be calculated.
     */
    private List<Wish> wishList;

    /**
     * Possibilities of variation in monthly income. <br/>
     * <i>This is a potential difference in savings. For example, if a user thought they would earn more or less over
     * the course of a month, etc.</i>
     */
    private Variance variance;

    /**
     * The type of savings for which a financial (/ savings) plan will be created.
     */
    private SavingsType savingsType;
}
