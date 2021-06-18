package ua.uhk.mois.financialplanning.model.dto.planning.year;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author KVN
 * @since 11.04.2021 11:31
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AnnualPlanningDtoOut {

    // List of items (/ information) that "show" the saved amount in the specified year (with a year payout offset)
    private List<AnnualOverview> annualOverviewMinus25List;
    private List<AnnualOverview> annualOverviewMinus5List;
    private List<AnnualOverview> annualOverview0List;
    private List<AnnualOverview> annualOverviewPlus25List;
    private List<AnnualOverview> annualOverviewPlus5List;

    // List of items with information about the years and wishes a user can afford to buy for a particular year (with a year payout offset)
    private List<AnnualAffordedWishOverview> affordedWishesOverviewMinus25List;
    private List<AnnualAffordedWishOverview> affordedWishesOverviewMinus5List;
    private List<AnnualAffordedWishOverview> affordedWishesOverview0List;
    private List<AnnualAffordedWishOverview> affordedWishesOverviewPlus25List;
    private List<AnnualAffordedWishOverview> affordedWishesOverviewPlus5List;
}
