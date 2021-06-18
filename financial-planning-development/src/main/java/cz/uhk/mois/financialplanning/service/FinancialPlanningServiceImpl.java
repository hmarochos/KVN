package cz.uhk.mois.financialplanning.service;

import cz.uhk.mois.financialplanning.model.dto.planning.AffordedWishOverview;
import cz.uhk.mois.financialplanning.model.dto.planning.FinancialPlanOverview;
import cz.uhk.mois.financialplanning.model.dto.planning.month.MonthlyAffordedWishOverview;
import cz.uhk.mois.financialplanning.model.dto.planning.month.MonthlyOverview;
import cz.uhk.mois.financialplanning.model.dto.planning.month.MonthlyPlanningDtoIn;
import cz.uhk.mois.financialplanning.model.dto.planning.month.MonthlyPlanningDtoOut;
import cz.uhk.mois.financialplanning.model.dto.planning.year.AnnualAffordedWishOverview;
import cz.uhk.mois.financialplanning.model.dto.planning.year.AnnualOverview;
import cz.uhk.mois.financialplanning.model.dto.planning.year.AnnualPlanningDtoIn;
import cz.uhk.mois.financialplanning.model.dto.planning.year.AnnualPlanningDtoOut;
import cz.uhk.mois.financialplanning.model.entity.wish.Wish;
import cz.uhk.mois.financialplanning.response.Failure;
import cz.uhk.mois.financialplanning.response.FailureSupport;
import cz.uhk.mois.financialplanning.response.Success;
import cz.uhk.mois.financialplanning.service.planning.CreateFinancialPlanCalculationData;
import cz.uhk.mois.financialplanning.service.planning.CreateFinancialPlanData;
import cz.uhk.mois.financialplanning.service.planning.SavingsType;
import cz.uhk.mois.financialplanning.service.planning.Variance;
import cz.uhk.mois.financialplanning.service.support.AuthSupport;
import cz.uhk.mois.financialplanning.service.support.UserSupport;
import cz.uhk.mois.financialplanning.validation.planning.AnnualPlanningDtoInValidator;
import cz.uhk.mois.financialplanning.validation.planning.MonthlyPlanningDtoInValidator;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static cz.uhk.mois.financialplanning.service.support.AuthSupport.getSignedInUser;

/**
 * @author Jan Krunčík
 * @since 11.04.2020 0:17
 */

@Component
@Log4j2
public class FinancialPlanningServiceImpl implements FinancialPlanningService {

    private final UserSupport userSupport;

    /**
     * The number of months for which savings will be calculated.
     */
    @Value("${saving.months.count:30}")
    private Integer savingMonthsCount;

    @Value("${saving.years.count:30}")
    private Integer savingYearsCount;

    public FinancialPlanningServiceImpl(UserSupport userSupport) {
        this.userSupport = userSupport;
    }

    /**
     * Sort wishes in ascending order according to their priorities.
     *
     * @param wishList
     *         wish list to be ordered according to the above requirements
     *
     * @return sorted wish list according to the above requirements
     */
    private static List<Wish> sortWishesAscByPriority(List<Wish> wishList) {
        if (wishList == null) {
            return Collections.emptyList();
        }

        Comparator<Wish> compareByPriority = Comparator.comparing(Wish::getPriority);
        wishList.sort(compareByPriority);
        return wishList;
    }

    private static Success<MonthlyPlanningDtoOut> getMonthlyPlanningSuccess(MonthlyPlanningDtoOut dtoOut) {
        log.info("Monthly planning successfully created. {}", dtoOut);
        return Success.<MonthlyPlanningDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    private static Success<AnnualPlanningDtoOut> getAnnualPlanningSuccess(AnnualPlanningDtoOut dtoOut) {
        log.info("Annual planning successfully created. {}", dtoOut);
        return Success.<AnnualPlanningDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    /**
     * Calculation of savings (/ planning) for defined period n months or years (depends on
     * calculationData.savingsType). <br/>
     * <i>For each wish by priority, method try to find out whether the user will be able to buy that wish in a defined
     * period of n months or years.</i>
     *
     * @param calculationData
     *         criteria for which the savings plan is to be calculated
     *
     * @return a list of savings for n months or years in advance, plus a list of indexes of months or years when the
     * user will be able to buy any wish
     */
    private static Tuple2<List<FinancialPlanOverview>, List<AffordedWishOverview>> computeFinancialPlan(CreateFinancialPlanCalculationData calculationData) {
        BigDecimal amount = calculationData.getAmountSaved();

        // Overview of (monthly or annual) saved amounts
        List<FinancialPlanOverview> financialPlanOverviews = new ArrayList<>();

        // An overview of wishes when the user will be able to afford them
        List<AffordedWishOverview> affordedWishOverviewList = new ArrayList<>();

        // Current wish index, for which the following algorithm predicts when the user will be able to buy it
        int wishIndexUserCanAfford = 0;

        // The cycle calculates the prediction for a fixed period of n months or years
        for (int i = 1; i <= calculationData.getSavingsPeriod(); i++) {
            // Increase savings by the amount a user counts (expect) each month or year
            // Expected monthly salary for the whole year (annual salary) or just for one month (depends on type of savings)
            BigDecimal profit = calculationData.getSavingsType() == SavingsType.ANNUAL ? calculationData.getMonthlyProfit().multiply(BigDecimal.valueOf(12)) : calculationData.getMonthlyProfit();

            amount = amount.add(handlePercentVariance(profit, calculationData.getVariance()));

            // Add a saved amount
            financialPlanOverviews.add(FinancialPlanOverview.builder()
                                                            .index(i)
                                                            .amountSaved(amount)
                                                            .build());

            for (int wishIndex = wishIndexUserCanAfford; wishIndex < calculationData.getWishList().size(); wishIndex++) {
                Wish wish = calculationData.getWishList().get(wishIndex);
                // Finding out whether a user can afford (/ buy) wish (with the next priority) this month or year
                if (wish.getPrice().compareTo(amount) <= 0) {
                    affordedWishOverviewList.add(AffordedWishOverview.builder()
                                                                     .index(i)
                                                                     .wishName(wish.getName())
                                                                     .build());
                    amount = amount.subtract(wish.getPrice());
                    wishIndexUserCanAfford++;
                } else {
                    // The user does not have money to buy a wish that he / she has next in order, so it is necessary to quit, otherwise there would be no need for priority, because all other wishes would go through
                    break;
                }
            }
        }

        return Tuple.of(financialPlanOverviews, affordedWishOverviewList);
    }

    private static BigDecimal handlePercentVariance(BigDecimal amount, Variance variance) {
        if (variance == Variance.TWO_FIVE_PERCENT_LESS) {
            return amount.multiply(BigDecimal.valueOf(1 - 0.025));
        } else if (variance == Variance.FIVE_PERCENT_LESS) {
            return amount.multiply(BigDecimal.valueOf(1 - 0.05));
        } else if (variance == Variance.TWO_FIVE_PERCENT_MORE) {
            return amount.multiply(BigDecimal.valueOf(1.025));
        } else if (variance == Variance.FIVE_PERCENT_MORE) {
            return amount.multiply(BigDecimal.valueOf(1.05));
        } else if (variance == Variance.ZERO) {
            return amount;
        }
        return amount;
    }

    /**
     * Preparation of a financial (/ savings) plan for each prepared type of imbalance.
     *
     * @param calculationData
     *         criteria of financial plan
     *
     * @return list with calculated financial plan according to entered criteria
     */
    private static List<Tuple2<List<FinancialPlanOverview>, List<AffordedWishOverview>>> handleCreatedFinancialPlan(CreateFinancialPlanCalculationData calculationData) {
        List<Tuple2<List<FinancialPlanOverview>, List<AffordedWishOverview>>> tuple2List = new ArrayList<>();

        calculationData.setVariance(Variance.TWO_FIVE_PERCENT_LESS);
        tuple2List.add(computeFinancialPlan(calculationData));

        calculationData.setVariance(Variance.FIVE_PERCENT_LESS);
        tuple2List.add(computeFinancialPlan(calculationData));

        calculationData.setVariance(Variance.ZERO);
        tuple2List.add(computeFinancialPlan(calculationData));

        calculationData.setVariance(Variance.TWO_FIVE_PERCENT_MORE);
        tuple2List.add(computeFinancialPlan(calculationData));

        calculationData.setVariance(Variance.FIVE_PERCENT_MORE);
        tuple2List.add(computeFinancialPlan(calculationData));

        return tuple2List;
    }

    private static List<MonthlyOverview> convertToMonthlyOverview(List<FinancialPlanOverview> financialPlanOverviewList) {
        return financialPlanOverviewList.stream()
                                        .map(financialPlanOverview -> MonthlyOverview.builder()
                                                                                     .monthIndex(financialPlanOverview.getIndex())
                                                                                     .amountSaved(financialPlanOverview.getAmountSaved())
                                                                                     .build())
                                        .collect(Collectors.toList());
    }

    private static List<AnnualOverview> convertToAnnualOverview(List<FinancialPlanOverview> financialPlanOverviewList) {
        return financialPlanOverviewList.stream()
                                        .map(financialPlanOverview -> AnnualOverview.builder()
                                                                                    .yearIndex(financialPlanOverview.getIndex())
                                                                                    .amountSaved(financialPlanOverview.getAmountSaved())
                                                                                    .build())
                                        .collect(Collectors.toList());
    }

    private static List<MonthlyAffordedWishOverview> convertToMonthlyAffordedWishOverview(List<AffordedWishOverview> affordedWishOverviewList) {
        return affordedWishOverviewList.stream()
                                       .map(affordedWishOverview -> MonthlyAffordedWishOverview.builder()
                                                                                               .monthIndex(affordedWishOverview.getIndex())
                                                                                               .wishName(affordedWishOverview.getWishName())
                                                                                               .build())
                                       .collect(Collectors.toList());
    }

    private static List<AnnualAffordedWishOverview> convertToAnnualAffordedWishOverview(List<AffordedWishOverview> affordedWishOverviewList) {
        return affordedWishOverviewList.stream()
                                       .map(affordedWishOverview -> AnnualAffordedWishOverview.builder()
                                                                                              .yearIndex(affordedWishOverview.getIndex())
                                                                                              .wishName(affordedWishOverview.getWishName())
                                                                                              .build())
                                       .collect(Collectors.toList());
    }

    @Override
    public Either<Failure, Success<MonthlyPlanningDtoOut>> getMonthlyPlanning(MonthlyPlanningDtoIn dtoIn) {
        log.info("Obtaining a monthly financial plan to achieve the wish of the signed-in user. {}", dtoIn);

        return MonthlyPlanningDtoInValidator.validate(dtoIn)
                                            .mapError(validationViolations -> FailureSupport.validationFailure(validationViolations, "MonthlyPlanningDtoIn"))
                                            .toEither()
                                            .flatMap(validatedDtoIn -> getSignedInUser()
                                                    .flatMap(AuthSupport::checkSignedInUser)
                                                    .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                                    .flatMap(userSupport::findUserById)
                                                    .map(user -> CreateFinancialPlanData.builder()
                                                                                        .wishList(sortWishesAscByPriority(user.getWishList()))
                                                                                        .amountSaved(validatedDtoIn.getAmountSaved())
                                                                                        .monthlyProfit(validatedDtoIn.getMonthlyProfit())
                                                                                        .build()))
                                            .map(this::createMonthlyFinancialPlan)
                                            .map(FinancialPlanningServiceImpl::getMonthlyPlanningSuccess);
    }

    @Override
    public Either<Failure, Success<AnnualPlanningDtoOut>> getAnnualPlanning(AnnualPlanningDtoIn dtoIn) {
        log.info("Obtaining an annual financial plan to achieve the wishes of the signed-in user. {}", dtoIn);

        return AnnualPlanningDtoInValidator.validate(dtoIn)
                                           .mapError(validationViolations -> FailureSupport.validationFailure(validationViolations, "AnnualPlanningDtoIn"))
                                           .toEither()
                                           .flatMap(validatedDtoIn -> getSignedInUser()
                                                   .flatMap(AuthSupport::checkSignedInUser)
                                                   .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                                   .flatMap(userSupport::findUserById)
                                                   .map(user -> CreateFinancialPlanData.builder()
                                                                                       .wishList(sortWishesAscByPriority(user.getWishList()))
                                                                                       .amountSaved(validatedDtoIn.getAmountSaved())
                                                                                       .monthlyProfit(validatedDtoIn.getMonthlyProfit())
                                                                                       .build()))
                                           .map(this::createAnnualFinancialPlan)
                                           .map(FinancialPlanningServiceImpl::getAnnualPlanningSuccess);
    }

    /**
     * Creating a savings plan that determines when a user can afford defined wishes (in sequence).
     *
     * @param financialPlanData
     *         data needed to create a matching plan (defined wishes and starting amounts)
     *
     * @return created a savings plan that shows the user when he / she can buy the items defined in each wish
     */
    private MonthlyPlanningDtoOut createMonthlyFinancialPlan(CreateFinancialPlanData financialPlanData) {
        CreateFinancialPlanCalculationData calculationData = CreateFinancialPlanCalculationData.builder()
                                                                                               .amountSaved(financialPlanData.getAmountSaved())
                                                                                               .monthlyProfit(financialPlanData.getMonthlyProfit())
                                                                                               .savingsPeriod(savingMonthsCount)
                                                                                               .wishList(financialPlanData.getWishList())
                                                                                               .savingsType(SavingsType.MONTHLY)
                                                                                               .build();

        List<Tuple2<List<FinancialPlanOverview>, List<AffordedWishOverview>>> tuple2List = handleCreatedFinancialPlan(calculationData);

        return MonthlyPlanningDtoOut.builder()
                                    .monthlyOverviewMinus25List(convertToMonthlyOverview(tuple2List.get(0)._1))
                                    .monthlyOverviewMinus5List(convertToMonthlyOverview(tuple2List.get(1)._1))
                                    .monthlyOverview0List(convertToMonthlyOverview(tuple2List.get(2)._1))
                                    .monthlyOverviewPlus25List(convertToMonthlyOverview(tuple2List.get(3)._1))
                                    .monthlyOverviewPlus5List(convertToMonthlyOverview(tuple2List.get(4)._1))
                                    .affordedWishesOverviewMinus25List(convertToMonthlyAffordedWishOverview(tuple2List.get(0)._2))
                                    .affordedWishesOverviewMinus5List(convertToMonthlyAffordedWishOverview(tuple2List.get(1)._2))
                                    .affordedWishesOverview0List(convertToMonthlyAffordedWishOverview(tuple2List.get(2)._2))
                                    .affordedWishesOverviewPlus25List(convertToMonthlyAffordedWishOverview(tuple2List.get(3)._2))
                                    .affordedWishesOverviewPlus5List(convertToMonthlyAffordedWishOverview(tuple2List.get(4)._2))
                                    .build();
    }

    /**
     * Creating a savings plan that determines when a user can afford defined wishes (in sequence).
     *
     * @param financialPlanData
     *         data needed to create a matching plan (defined wishes and starting amounts)
     *
     * @return created a savings plan that shows the user when he / she can buy the items defined in each wish
     */
    private AnnualPlanningDtoOut createAnnualFinancialPlan(CreateFinancialPlanData financialPlanData) {
        CreateFinancialPlanCalculationData calculationData = CreateFinancialPlanCalculationData.builder()
                                                                                               .amountSaved(financialPlanData.getAmountSaved())
                                                                                               .monthlyProfit(financialPlanData.getMonthlyProfit())
                                                                                               .savingsPeriod(savingYearsCount)
                                                                                               .wishList(financialPlanData.getWishList())
                                                                                               .savingsType(SavingsType.ANNUAL)
                                                                                               .build();

        List<Tuple2<List<FinancialPlanOverview>, List<AffordedWishOverview>>> tuple2List = handleCreatedFinancialPlan(calculationData);

        return AnnualPlanningDtoOut.builder()
                                   .annualOverviewMinus25List(convertToAnnualOverview(tuple2List.get(0)._1))
                                   .annualOverviewMinus5List(convertToAnnualOverview(tuple2List.get(1)._1))
                                   .annualOverview0List(convertToAnnualOverview(tuple2List.get(2)._1))
                                   .annualOverviewPlus25List(convertToAnnualOverview(tuple2List.get(3)._1))
                                   .annualOverviewPlus5List(convertToAnnualOverview(tuple2List.get(4)._1))
                                   .affordedWishesOverviewMinus25List(convertToAnnualAffordedWishOverview(tuple2List.get(0)._2))
                                   .affordedWishesOverviewMinus5List(convertToAnnualAffordedWishOverview(tuple2List.get(1)._2))
                                   .affordedWishesOverview0List(convertToAnnualAffordedWishOverview(tuple2List.get(2)._2))
                                   .affordedWishesOverviewPlus25List(convertToAnnualAffordedWishOverview(tuple2List.get(3)._2))
                                   .affordedWishesOverviewPlus5List(convertToAnnualAffordedWishOverview(tuple2List.get(4)._2))
                                   .build();
    }
}
