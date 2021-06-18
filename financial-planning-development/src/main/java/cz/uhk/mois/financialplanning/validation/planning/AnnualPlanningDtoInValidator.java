package cz.uhk.mois.financialplanning.validation.planning;

import cz.uhk.mois.financialplanning.model.dto.planning.year.AnnualPlanningDtoIn;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 11.04.2020 11:33
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnualPlanningDtoInValidator {

    public static Validation<Seq<String>, AnnualPlanningDtoIn> validate(AnnualPlanningDtoIn dtoIn) {
        return Validation.combine(FinancialPlanningValidationSupport.validateAmountSaved(dtoIn.getAmountSaved()),
                                  FinancialPlanningValidationSupport.validateMonthlyProfit(dtoIn.getMonthlyProfit()))
                         .ap(AnnualPlanningDtoIn::new);
    }
}
