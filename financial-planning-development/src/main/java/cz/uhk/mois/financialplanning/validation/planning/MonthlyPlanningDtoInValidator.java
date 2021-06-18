package cz.uhk.mois.financialplanning.validation.planning;

import cz.uhk.mois.financialplanning.model.dto.planning.month.MonthlyPlanningDtoIn;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 11.04.2020 0:10
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MonthlyPlanningDtoInValidator {

    public static Validation<Seq<String>, MonthlyPlanningDtoIn> validate(MonthlyPlanningDtoIn dtoIn) {
        return Validation.combine(FinancialPlanningValidationSupport.validateAmountSaved(dtoIn.getAmountSaved()),
                                  FinancialPlanningValidationSupport.validateMonthlyProfit(dtoIn.getMonthlyProfit()))
                         .ap(MonthlyPlanningDtoIn::new);
    }
}
