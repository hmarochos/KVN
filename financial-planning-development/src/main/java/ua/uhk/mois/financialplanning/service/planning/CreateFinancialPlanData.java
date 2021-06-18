package ua.uhk.mois.financialplanning.service.planning;

import ua.uhk.mois.financialplanning.model.entity.wish.Wish;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Support / Auxiliary class for passing data that is needed to create a financial plan.
 *
 * @author KVN
 * @since 11.04.2021 0:42
 */

@Data
@Builder
public class CreateFinancialPlanData {

    /**
     * List of wishes for which a financial plan is to be created.
     */
    private List<Wish> wishList;

    /**
     * Initial amount from which the financial plan is to start.
     */
    private BigDecimal amountSaved;

    /**
     * Monthly income. This is the amount by which financial planning will be increased each month to achieve the
     * wishes.
     */
    private BigDecimal monthlyProfit;
}
