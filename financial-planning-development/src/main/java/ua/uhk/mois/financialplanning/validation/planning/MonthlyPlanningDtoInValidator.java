package ua.uhk.mois.financialplanning.validation.planning;

import ua.uhk.mois.financialplanning.model.dto.planning.month.MonthlyPlanningDtoIn;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author KVN
 * @since 11.04.2021 0:10
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MonthlyPlanningDtoInValidator {

    public static Validation<Seq<String>, MonthlyPlanningDtoIn> validate(MonthlyPlanningDtoIn dtoIn) {
        return Validation.combine(FinancialPlanningValidationSupport.validateAmountSaved(dtoIn.getAmountSaved()),
                                  FinancialPlanningValidationSupport.validateMonthlyProfit(dtoIn.getMonthlyProfit()))
                         .ap(MonthlyPlanningDtoIn::new);
    }
}
