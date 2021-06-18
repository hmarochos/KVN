package ua.uhk.mois.financialplanning.validation.planning;

import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author KVN
 * @since 11.04.2021 0:11
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FinancialPlanningValidationSupport {

    static Validation<String, BigDecimal> validateAmountSaved(BigDecimal amountSaved) {
        if (amountSaved == null) {
            return Validation.invalid("The amount saved is not specified.");
        }
        if (amountSaved.compareTo(BigDecimal.ZERO) < 0) {
            return Validation.invalid("The amount saved must be a positive number.");
        }
        return Validation.valid(amountSaved);
    }

    static Validation<String, BigDecimal> validateMonthlyProfit(BigDecimal monthlyProfit) {
        if (monthlyProfit == null) {
            return Validation.invalid("The monthly profit is not specified.");
        }
        if (monthlyProfit.compareTo(BigDecimal.ZERO) < 0) {
            return Validation.invalid("The monthly profit must be a positive number.");
        }
        return Validation.valid(monthlyProfit);
    }
}
