package cz.uhk.mois.financialplanning.service;

import cz.uhk.mois.financialplanning.model.dto.planning.month.MonthlyPlanningDtoIn;
import cz.uhk.mois.financialplanning.model.dto.planning.month.MonthlyPlanningDtoOut;
import cz.uhk.mois.financialplanning.model.dto.planning.year.AnnualPlanningDtoIn;
import cz.uhk.mois.financialplanning.model.dto.planning.year.AnnualPlanningDtoOut;
import cz.uhk.mois.financialplanning.response.Failure;
import cz.uhk.mois.financialplanning.response.Success;
import io.vavr.control.Either;

/**
 * @author Jan Krunčík
 * @since 11.04.2020 0:14
 */

public interface FinancialPlanningService {

    /**
     * Get a monthly financial plan to achieve the wish of the signed-in user.
     *
     * @param dtoIn
     *         information about the start of planning, ie the amount (saved) amount should be started and how much
     *         should be added
     *
     * @return right with a monthly plan in the form suitable for displaying in the graph (on FE), otherwise left with
     * information about the error
     */
    Either<Failure, Success<MonthlyPlanningDtoOut>> getMonthlyPlanning(MonthlyPlanningDtoIn dtoIn);

    /**
     * Get an annual financial plan to achieve the wishes of the signed-in user.
     *
     * @param dtoIn
     *         information about the start of planning, ie the amount (saved) amount should be started and how much
     *         should be added
     *
     * @return right with the annual financial savings plan created to achieve a sufficient amount to buy set wishes,
     * otherwise left with information about the error
     */
    Either<Failure, Success<AnnualPlanningDtoOut>> getAnnualPlanning(AnnualPlanningDtoIn dtoIn);
}
