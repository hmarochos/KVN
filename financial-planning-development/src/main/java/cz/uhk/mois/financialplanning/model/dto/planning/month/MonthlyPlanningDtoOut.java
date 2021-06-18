package cz.uhk.mois.financialplanning.model.dto.planning.month;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author Jan Krunčík
 * @since 11.04.2020 0:12
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MonthlyPlanningDtoOut {

    // List of items (/ information) that "show" the saved amount in the specified month (with a monthly payout offset)
    private List<MonthlyOverview> monthlyOverviewMinus25List;
    private List<MonthlyOverview> monthlyOverviewMinus5List;
    private List<MonthlyOverview> monthlyOverview0List;
    private List<MonthlyOverview> monthlyOverviewPlus25List;
    private List<MonthlyOverview> monthlyOverviewPlus5List;

    // List of items with information about the months and wishes a user can afford to buy for a particular month (with a monthly payout offset)
    private List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus25List;
    private List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus5List;
    private List<MonthlyAffordedWishOverview> affordedWishesOverview0List;
    private List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus25List;
    private List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus5List;

}
