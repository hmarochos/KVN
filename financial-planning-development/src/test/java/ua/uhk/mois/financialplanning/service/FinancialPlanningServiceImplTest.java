package ua.uhk.mois.financialplanning.service;

import ua.uhk.mois.financialplanning.configuration.AbsTestConfiguration;
import ua.uhk.mois.financialplanning.configuration.user.UserTestSupport;
import ua.uhk.mois.financialplanning.model.dto.planning.month.MonthlyAffordedWishOverview;
import ua.uhk.mois.financialplanning.model.dto.planning.month.MonthlyOverview;
import ua.uhk.mois.financialplanning.model.dto.planning.month.MonthlyPlanningDtoIn;
import ua.uhk.mois.financialplanning.model.dto.planning.month.MonthlyPlanningDtoOut;
import ua.uhk.mois.financialplanning.model.dto.planning.year.AnnualAffordedWishOverview;
import ua.uhk.mois.financialplanning.model.dto.planning.year.AnnualOverview;
import ua.uhk.mois.financialplanning.model.dto.planning.year.AnnualPlanningDtoIn;
import ua.uhk.mois.financialplanning.model.dto.planning.year.AnnualPlanningDtoOut;
import ua.uhk.mois.financialplanning.model.entity.user.User;
import ua.uhk.mois.financialplanning.response.Failure;
import ua.uhk.mois.financialplanning.response.Success;
import io.vavr.control.Either;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.createWish;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.signInUser;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.signOutUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class FinancialPlanningServiceImplTest extends AbsTestConfiguration {

    @Autowired
    private FinancialPlanningService financialPlanningService;

    private static MonthlyPlanningDtoIn createMonthlyPlanningDtoIn() {
        MonthlyPlanningDtoIn dtoIn = new MonthlyPlanningDtoIn();
        dtoIn.setAmountSaved(BigDecimal.valueOf(0));
        dtoIn.setMonthlyProfit(BigDecimal.valueOf(5000));
        return dtoIn;
    }

    private static void assertMonthlyOverview(MonthlyOverview monthlyOverview, int monthIndex, BigDecimal amountSaved) {
        assertEquals(monthIndex, monthlyOverview.getMonthIndex());
        assertEquals(0, amountSaved.compareTo(monthlyOverview.getAmountSaved()));
    }

    private static void assertMonthlyAffordedWishOverview(MonthlyAffordedWishOverview monthlyAffordedWishOverview, int monthIndex, String wishName) {
        assertEquals(monthIndex, monthlyAffordedWishOverview.getMonthIndex());
        assertEquals(wishName, monthlyAffordedWishOverview.getWishName());
    }

    private static void assertAnnualAffordedWishOverview(AnnualAffordedWishOverview annualAffordedWishOverview, int yearIndex, String wishName) {
        assertEquals(yearIndex, annualAffordedWishOverview.getYearIndex());
        assertEquals(wishName, annualAffordedWishOverview.getWishName());
    }

    private static void assertAnnualOverview(AnnualOverview annualOverview, int yearIndex, BigDecimal amountSaved) {
        assertEquals(yearIndex, annualOverview.getYearIndex());
        assertEquals(0, amountSaved.compareTo(annualOverview.getAmountSaved()));
    }

    private static AnnualPlanningDtoIn createAnnualPlanningDtoIn() {
        AnnualPlanningDtoIn dtoIn = new AnnualPlanningDtoIn();
        dtoIn.setAmountSaved(BigDecimal.valueOf(0));
        dtoIn.setMonthlyProfit(BigDecimal.valueOf(5000));
        return dtoIn;
    }

    @BeforeEach
    void setUp() {
        log.info("Clear database before test.");
        clearDatabase();

        log.info("Logout user before test (if any).");
        signOutUser();
    }

    @AfterEach
    void tearDown() {
        log.info("Clear database after test.");
        clearDatabase();

        log.info("Logout user after test.");
        signOutUser();
    }

    @Test
    @Transactional
    void getMonthlyPlanning_Hds_FirstTwoMonths() {
        log.info("Test of creating a monthly savings plan to achieve wishes. The user saves on all wishes during the first two months.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        user.getWishList().get(0).setPrice(BigDecimal.valueOf(500));
        user.getWishList().get(2).setPrice(BigDecimal.valueOf(5000));
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        MonthlyPlanningDtoIn dtoIn = createMonthlyPlanningDtoIn();

        // Execution
        Either<Failure, Success<MonthlyPlanningDtoOut>> monthlyPlanning = financialPlanningService.getMonthlyPlanning(dtoIn);

        // Verification
        assertTrue(monthlyPlanning.isRight());
        assertEquals(HttpStatus.OK, monthlyPlanning.get().getHttpStatus());

        assertDatabaseSize(1, 3);

        MonthlyPlanningDtoOut dtoOut = monthlyPlanning.get().getBody();

        List<MonthlyOverview> monthlyOverviewMinus25List = dtoOut.getMonthlyOverviewMinus25List();
        assertThat(monthlyOverviewMinus25List, hasSize(40));
        int expectedAmountSavedMinus25 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 1) {
                expectedAmountSavedMinus25 = 3375;
            } else if (i == 2) {
                expectedAmountSavedMinus25 = 3250;
            }
            expectedAmountSavedMinus25 += 4875;
            assertMonthlyOverview(monthlyOverviewMinus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus25));
        }

        List<MonthlyOverview> monthlyOverviewMinus5List = dtoOut.getMonthlyOverviewMinus5List();
        assertThat(monthlyOverviewMinus5List, hasSize(40));
        int expectedAmountSavedMinus5 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 1) {
                expectedAmountSavedMinus5 = 3250;
            } else if (i == 2) {
                expectedAmountSavedMinus5 = 3000;
            }
            expectedAmountSavedMinus5 += 4750;
            assertMonthlyOverview(monthlyOverviewMinus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus5));
        }

        List<MonthlyOverview> monthlyOverview0List = dtoOut.getMonthlyOverview0List();
        assertThat(monthlyOverview0List, hasSize(40));
        int expectedAmountSaved0 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 1 || i == 2) {
                expectedAmountSaved0 = 3500;
            }
            expectedAmountSaved0 += 5000;
            assertMonthlyOverview(monthlyOverview0List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSaved0));
        }

        List<MonthlyOverview> monthlyOverviewPlus25List = dtoOut.getMonthlyOverviewPlus25List();
        assertThat(monthlyOverviewPlus25List, hasSize(40));
        int expectedAmountSavedPlus25 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 1) {
                expectedAmountSavedPlus25 = 3625;
            } else if (i == 2) {
                expectedAmountSavedPlus25 = 3750;
            }
            expectedAmountSavedPlus25 += 5125;
            assertMonthlyOverview(monthlyOverviewPlus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus25));
        }

        List<MonthlyOverview> monthlyOverviewPlus5List = dtoOut.getMonthlyOverviewPlus5List();
        assertThat(monthlyOverviewPlus5List, hasSize(40));
        int expectedAmountSavedPlus5 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 1) {
                expectedAmountSavedPlus5 = 3750;
            } else if (i == 2) {
                expectedAmountSavedPlus5 = 4000;
            }
            expectedAmountSavedPlus5 += 5250;
            assertMonthlyOverview(monthlyOverviewPlus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus5));
        }

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus25List = dtoOut.getAffordedWishesOverviewMinus25List();
        assertThat(affordedWishesOverviewMinus25List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(0), 1, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(1), 1, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(2), 2, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus5List = dtoOut.getAffordedWishesOverviewMinus5List();
        assertThat(affordedWishesOverviewMinus5List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(0), 1, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(1), 1, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(2), 2, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverview0List = dtoOut.getAffordedWishesOverview0List();
        assertThat(affordedWishesOverview0List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(0), 1, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(1), 1, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(2), 2, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus25List = dtoOut.getAffordedWishesOverviewPlus25List();
        assertThat(affordedWishesOverviewPlus25List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(0), 1, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(1), 1, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(2), 2, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus5List = dtoOut.getAffordedWishesOverviewPlus5List();
        assertThat(affordedWishesOverviewPlus5List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(0), 1, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(1), 1, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(2), 2, "Wish 2");
    }

    @Test
    @Transactional
    void getMonthlyPlanning_Hds_MoreMonths() {
        log.info("Test of creating a monthly savings plan to achieve wishes. The user can save on all wishes during the 'appropriate' time for which the application calculates the monthly savings.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        user.getWishList().get(0).setPrice(BigDecimal.valueOf(1000));
        user.getWishList().get(1).setPrice(BigDecimal.valueOf(2000));
        user.getWishList().get(2).setPrice(BigDecimal.valueOf(5000));
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        MonthlyPlanningDtoIn dtoIn = createMonthlyPlanningDtoIn();
        dtoIn.setMonthlyProfit(BigDecimal.valueOf(500));

        // Execution
        Either<Failure, Success<MonthlyPlanningDtoOut>> monthlyPlanning = financialPlanningService.getMonthlyPlanning(dtoIn);

        // Verification
        assertTrue(monthlyPlanning.isRight());
        assertEquals(HttpStatus.OK, monthlyPlanning.get().getHttpStatus());

        assertDatabaseSize(1, 3);

        MonthlyPlanningDtoOut dtoOut = monthlyPlanning.get().getBody();

        List<MonthlyOverview> monthlyOverviewMinus25List = dtoOut.getMonthlyOverviewMinus25List();
        assertThat(monthlyOverviewMinus25List, hasSize(40));
        double expectedAmountSavedMinus25 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 3) {
                expectedAmountSavedMinus25 = 462.5;
            } else if (i == 7) {
                expectedAmountSavedMinus25 = 412.5;
            } else if (i == 17) {
                expectedAmountSavedMinus25 = 287.5;
            }
            expectedAmountSavedMinus25 += 487.500;
            assertMonthlyOverview(monthlyOverviewMinus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus25));
        }

        List<MonthlyOverview> monthlyOverviewMinus5List = dtoOut.getMonthlyOverviewMinus5List();
        assertThat(monthlyOverviewMinus5List, hasSize(40));
        int expectedAmountSavedMinus5 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 3) {
                expectedAmountSavedMinus5 = 425;
            } else if (i == 7) {
                expectedAmountSavedMinus5 = 325;
            } else if (i == 17) {
                expectedAmountSavedMinus5 = 75;
            }
            expectedAmountSavedMinus5 += 475;
            assertMonthlyOverview(monthlyOverviewMinus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus5));
        }

        List<MonthlyOverview> monthlyOverview0List = dtoOut.getMonthlyOverview0List();
        assertThat(monthlyOverview0List, hasSize(40));
        int expectedAmountSaved0 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 2 || i == 6 || i == 16) {
                expectedAmountSaved0 = 0;
            }
            expectedAmountSaved0 += 500;
            assertMonthlyOverview(monthlyOverview0List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSaved0));
        }

        List<MonthlyOverview> monthlyOverviewPlus25List = dtoOut.getMonthlyOverviewPlus25List();
        assertThat(monthlyOverviewPlus25List, hasSize(40));
        double expectedAmountSavedPlus25 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 2) {
                expectedAmountSavedPlus25 = 25;
            } else if (i == 6) {
                expectedAmountSavedPlus25 = 75;
            } else if (i == 16) {
                expectedAmountSavedPlus25 = 200;
            }
            expectedAmountSavedPlus25 += 512.500;
            assertMonthlyOverview(monthlyOverviewPlus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus25));
        }

        List<MonthlyOverview> monthlyOverviewPlus5List = dtoOut.getMonthlyOverviewPlus5List();
        assertThat(monthlyOverviewPlus5List, hasSize(40));
        int expectedAmountSavedPlus5 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 2) {
                expectedAmountSavedPlus5 = 50;
            } else if (i == 6) {
                expectedAmountSavedPlus5 = 150;
            } else if (i == 16) {
                expectedAmountSavedPlus5 = 400;
            }
            expectedAmountSavedPlus5 += 525;
            assertMonthlyOverview(monthlyOverviewPlus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus5));
        }

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus25List = dtoOut.getAffordedWishesOverviewMinus25List();
        assertThat(affordedWishesOverviewMinus25List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(0), 3, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(1), 7, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(2), 17, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus5List = dtoOut.getAffordedWishesOverviewMinus5List();
        assertThat(affordedWishesOverviewMinus5List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(0), 3, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(1), 7, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(2), 17, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverview0List = dtoOut.getAffordedWishesOverview0List();
        assertThat(affordedWishesOverview0List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(0), 2, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(1), 6, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(2), 16, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus25List = dtoOut.getAffordedWishesOverviewPlus25List();
        assertThat(affordedWishesOverviewPlus25List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(0), 2, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(1), 6, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(2), 16, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus5List = dtoOut.getAffordedWishesOverviewPlus5List();
        assertThat(affordedWishesOverviewPlus5List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(0), 2, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(1), 6, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(2), 16, "Wish 2");
    }

    @Test
    @Transactional
    void getMonthlyPlanning_Hds_FinancialPlanWillNotBeCreatedForAllWishes() {
        log.info("Test of creating a monthly savings plan to achieve wishes. The user is not able to save on all wishes during the 'normal' saving time that the application calculates.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        user.getWishList().get(0).setPrice(BigDecimal.valueOf(2000));
        user.getWishList().get(1).setPrice(BigDecimal.valueOf(20000));
        user.getWishList().get(2).setPrice(BigDecimal.valueOf(1000000000));
        user.getWishList().add(createWish(BigDecimal.valueOf(2100), "Wish 3", 4, user));
        userRepository.save(user);

        assertDatabaseSize(1, 4);

        // Sign-in user
        signInUser(user);

        MonthlyPlanningDtoIn dtoIn = createMonthlyPlanningDtoIn();
        dtoIn.setMonthlyProfit(BigDecimal.valueOf(1000));

        // Execution
        Either<Failure, Success<MonthlyPlanningDtoOut>> monthlyPlanning = financialPlanningService.getMonthlyPlanning(dtoIn);

        // Verification
        assertTrue(monthlyPlanning.isRight());
        assertEquals(HttpStatus.OK, monthlyPlanning.get().getHttpStatus());

        assertDatabaseSize(1, 4);

        MonthlyPlanningDtoOut dtoOut = monthlyPlanning.get().getBody();
        List<MonthlyOverview> monthlyOverviewMinus25List = dtoOut.getMonthlyOverviewMinus25List();

        int expectedAmountSavedMinus25 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 3) {
                expectedAmountSavedMinus25 = 925;
            } else if (i == 23) {
                expectedAmountSavedMinus25 = 425;
            }
            expectedAmountSavedMinus25 += 975;
            assertMonthlyOverview(monthlyOverviewMinus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus25));
        }

        List<MonthlyOverview> monthlyOverviewMinus5List = dtoOut.getMonthlyOverviewMinus5List();
        int expectedAmountSavedMinus5 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 3) {
                expectedAmountSavedMinus5 = 850;
            } else if (i == 24) {
                expectedAmountSavedMinus5 = 800;
            }
            expectedAmountSavedMinus5 += 950;
            assertMonthlyOverview(monthlyOverviewMinus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus5));
        }

        List<MonthlyOverview> monthlyOverview0List = dtoOut.getMonthlyOverview0List();
        int expectedAmountSaved0 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 2 || i == 22) {
                expectedAmountSaved0 = 0;
            }
            expectedAmountSaved0 += 1000;
            assertMonthlyOverview(monthlyOverview0List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSaved0));
        }

        List<MonthlyOverview> monthlyOverviewPlus25List = dtoOut.getMonthlyOverviewPlus25List();
        int expectedAmountSavedPlus25 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 2) {
                expectedAmountSavedPlus25 = 50;
            } else if (i == 22) {
                expectedAmountSavedPlus25 = 550;
            }
            expectedAmountSavedPlus25 += 1025;
            assertMonthlyOverview(monthlyOverviewPlus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus25));
        }

        List<MonthlyOverview> monthlyOverviewPlus5List = dtoOut.getMonthlyOverviewPlus5List();
        int expectedAmountSavedPlus5 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 2) {
                expectedAmountSavedPlus5 = 100;
            } else if (i == 21) {
                expectedAmountSavedPlus5 = 50;
            }
            expectedAmountSavedPlus5 += 1050;
            assertMonthlyOverview(monthlyOverviewPlus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus5));
        }

        List<MonthlyAffordedWishOverview> affordedWishesOverview0List = dtoOut.getAffordedWishesOverview0List();
        assertThat(affordedWishesOverview0List, hasSize(2));
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(0), 2, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(1), 22, "Wish 1");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus25List = dtoOut.getAffordedWishesOverviewMinus25List();
        assertThat(affordedWishesOverviewMinus25List, hasSize(2));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(0), 3, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(1), 23, "Wish 1");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus5List = dtoOut.getAffordedWishesOverviewMinus5List();
        assertThat(affordedWishesOverviewMinus5List, hasSize(2));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(0), 3, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(1), 24, "Wish 1");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus25List = dtoOut.getAffordedWishesOverviewPlus25List();
        assertThat(affordedWishesOverviewPlus25List, hasSize(2));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(0), 2, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(1), 22, "Wish 1");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus5List = dtoOut.getAffordedWishesOverviewPlus5List();
        assertThat(affordedWishesOverviewPlus5List, hasSize(2));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(0), 2, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(1), 21, "Wish 1");
    }

    @Test
    @Transactional
    void getMonthlyPlanning_Hds_MoreExpensiveWishFirst() {
        log.info("Test of creating a monthly savings plan to achieve wishes. The more expensive wishes will be set to buy first, although it would be possible to buy the others first.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        user.getWishList().get(0).setPrice(BigDecimal.valueOf(15000));
        user.getWishList().get(1).setPrice(BigDecimal.valueOf(5000));
        user.getWishList().get(2).setPrice(BigDecimal.valueOf(5000));
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        MonthlyPlanningDtoIn dtoIn = createMonthlyPlanningDtoIn();

        // Execution
        Either<Failure, Success<MonthlyPlanningDtoOut>> monthlyPlanning = financialPlanningService.getMonthlyPlanning(dtoIn);

        // Verification
        assertTrue(monthlyPlanning.isRight());
        assertEquals(HttpStatus.OK, monthlyPlanning.get().getHttpStatus());

        assertDatabaseSize(1, 3);

        MonthlyPlanningDtoOut dtoOut = monthlyPlanning.get().getBody();

        List<MonthlyOverview> monthlyOverviewMinus25List = dtoOut.getMonthlyOverviewMinus25List();
        assertThat(monthlyOverviewMinus25List, hasSize(40));
        int expectedAmountSavedMinus25 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 4) {
                expectedAmountSavedMinus25 = 4500;
            } else if (i == 5) {
                expectedAmountSavedMinus25 = 4375;
            } else if (i == 6) {
                expectedAmountSavedMinus25 = 4250;
            }
            expectedAmountSavedMinus25 += 4875;
            assertMonthlyOverview(monthlyOverviewMinus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus25));
        }

        List<MonthlyOverview> monthlyOverviewMinus5List = dtoOut.getMonthlyOverviewMinus5List();
        assertThat(monthlyOverviewMinus5List, hasSize(40));
        int expectedAmountSavedMinus5 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 4) {
                expectedAmountSavedMinus5 = 4000;
            } else if (i == 5) {
                expectedAmountSavedMinus5 = 3750;
            } else if (i == 6) {
                expectedAmountSavedMinus5 = 3500;
            }
            expectedAmountSavedMinus5 += 4750;
            assertMonthlyOverview(monthlyOverviewMinus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus5));
        }

        List<MonthlyOverview> monthlyOverview0List = dtoOut.getMonthlyOverview0List();
        assertThat(monthlyOverview0List, hasSize(40));
        int expectedAmountSaved0 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 3 || i == 4 || i == 5) {
                expectedAmountSaved0 = 0;
            }
            expectedAmountSaved0 += 5000;
            assertMonthlyOverview(monthlyOverview0List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSaved0));
        }

        List<MonthlyOverview> monthlyOverviewPlus25List = dtoOut.getMonthlyOverviewPlus25List();
        assertThat(monthlyOverviewPlus25List, hasSize(40));
        int expectedAmountSavedPlus25 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 3) {
                expectedAmountSavedPlus25 = 375;
            } else if (i == 4) {
                expectedAmountSavedPlus25 = 500;
            } else if (i == 5) {
                expectedAmountSavedPlus25 = 625;
            }
            expectedAmountSavedPlus25 += 5125;
            assertMonthlyOverview(monthlyOverviewPlus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus25));
        }

        List<MonthlyOverview> monthlyOverviewPlus5List = dtoOut.getMonthlyOverviewPlus5List();
        assertThat(monthlyOverviewPlus5List, hasSize(40));
        int expectedAmountSavedPlus5 = 0;
        for (int i = 0; i < 40; i++) {
            if (i == 3) {
                expectedAmountSavedPlus5 = 750;
            } else if (i == 4) {
                expectedAmountSavedPlus5 = 1000;
            } else if (i == 5) {
                expectedAmountSavedPlus5 = 1250;
            }
            expectedAmountSavedPlus5 += 5250;
            assertMonthlyOverview(monthlyOverviewPlus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus5));
        }

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus25List = dtoOut.getAffordedWishesOverviewMinus25List();
        assertThat(affordedWishesOverviewMinus25List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(0), 4, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(1), 5, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(2), 6, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus5List = dtoOut.getAffordedWishesOverviewMinus5List();
        assertThat(affordedWishesOverviewMinus5List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(0), 4, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(1), 5, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(2), 6, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverview0List = dtoOut.getAffordedWishesOverview0List();
        assertThat(affordedWishesOverview0List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(0), 3, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(1), 4, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(2), 5, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus25List = dtoOut.getAffordedWishesOverviewPlus25List();
        assertThat(affordedWishesOverviewPlus25List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(0), 3, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(1), 4, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(2), 5, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus5List = dtoOut.getAffordedWishesOverviewPlus5List();
        assertThat(affordedWishesOverviewPlus5List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(0), 3, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(1), 4, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(2), 5, "Wish 2");
    }

    @Test
    @Transactional
    void getMonthlyPlanning_Hds_AllInOneMonth() {
        log.info("Test of creating a monthly savings plan to achieve wishes. The user will be able to purchase all wishes in one month.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        user.getWishList().get(0).setPrice(BigDecimal.valueOf(15000));
        user.getWishList().get(1).setPrice(BigDecimal.valueOf(3000));
        user.getWishList().get(2).setPrice(BigDecimal.valueOf(2500));
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        MonthlyPlanningDtoIn dtoIn = createMonthlyPlanningDtoIn();
        dtoIn.setAmountSaved(BigDecimal.valueOf(4000));
        dtoIn.setMonthlyProfit(BigDecimal.valueOf(10000));

        // Execution
        Either<Failure, Success<MonthlyPlanningDtoOut>> monthlyPlanning = financialPlanningService.getMonthlyPlanning(dtoIn);

        // Verification
        assertTrue(monthlyPlanning.isRight());
        assertEquals(HttpStatus.OK, monthlyPlanning.get().getHttpStatus());

        assertDatabaseSize(1, 3);

        MonthlyPlanningDtoOut dtoOut = monthlyPlanning.get().getBody();

        List<MonthlyOverview> monthlyOverviewMinus25List = dtoOut.getMonthlyOverviewMinus25List();
        assertThat(monthlyOverviewMinus25List, hasSize(40));
        int expectedAmountSavedMinus25 = 4000;
        for (int i = 0; i < 40; i++) {
            if (i == 2) {
                expectedAmountSavedMinus25 = 3000;
            }
            expectedAmountSavedMinus25 += 9750;
            assertMonthlyOverview(monthlyOverviewMinus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus25));
        }

        List<MonthlyOverview> monthlyOverviewMinus5List = dtoOut.getMonthlyOverviewMinus5List();
        assertThat(monthlyOverviewMinus5List, hasSize(40));
        int expectedAmountSavedMinus5 = 4000;
        for (int i = 0; i < 40; i++) {
            if (i == 2) {
                expectedAmountSavedMinus5 = 2500;
            }
            expectedAmountSavedMinus5 += 9500;
            assertMonthlyOverview(monthlyOverviewMinus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus5));
        }

        List<MonthlyOverview> monthlyOverview0List = dtoOut.getMonthlyOverview0List();
        assertThat(monthlyOverview0List, hasSize(40));
        int expectedAmountSaved0 = 4000;
        for (int i = 0; i < 40; i++) {
            if (i == 2) {
                expectedAmountSaved0 = 3500;
            }
            expectedAmountSaved0 += 10000;
            assertMonthlyOverview(monthlyOverview0List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSaved0));
        }

        List<MonthlyOverview> monthlyOverviewPlus25List = dtoOut.getMonthlyOverviewPlus25List();
        assertThat(monthlyOverviewPlus25List, hasSize(40));
        int expectedAmountSavedPlus25 = 4000;
        for (int i = 0; i < 40; i++) {
            if (i == 2) {
                expectedAmountSavedPlus25 = 4000;
            }
            expectedAmountSavedPlus25 += 10250;
            assertMonthlyOverview(monthlyOverviewPlus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus25));
        }

        List<MonthlyOverview> monthlyOverviewPlus5List = dtoOut.getMonthlyOverviewPlus5List();
        assertThat(monthlyOverviewPlus5List, hasSize(40));
        int expectedAmountSavedPlus5 = 4000;
        for (int i = 0; i < 40; i++) {
            if (i == 2) {
                expectedAmountSavedPlus5 = 4500;
            }
            expectedAmountSavedPlus5 += 10500;
            assertMonthlyOverview(monthlyOverviewPlus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus5));
        }

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus25List = dtoOut.getAffordedWishesOverviewMinus25List();
        assertThat(affordedWishesOverviewMinus25List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(0), 2, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(1), 2, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus25List.get(2), 2, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus5List = dtoOut.getAffordedWishesOverviewMinus5List();
        assertThat(affordedWishesOverviewMinus5List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(0), 2, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(1), 2, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewMinus5List.get(2), 2, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverview0List = dtoOut.getAffordedWishesOverview0List();
        assertThat(affordedWishesOverview0List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(0), 2, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(1), 2, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverview0List.get(2), 2, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus25List = dtoOut.getAffordedWishesOverviewPlus25List();
        assertThat(affordedWishesOverviewPlus25List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(0), 2, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(1), 2, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus25List.get(2), 2, "Wish 2");

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus5List = dtoOut.getAffordedWishesOverviewPlus5List();
        assertThat(affordedWishesOverviewPlus5List, hasSize(3));
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(0), 2, "Wish 0");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(1), 2, "Wish 1");
        assertMonthlyAffordedWishOverview(affordedWishesOverviewPlus5List.get(2), 2, "Wish 2");
    }

    @Test
    void getMonthlyPlanning_InvalidDtoIn() {
        log.info("Test of creating a monthly savings plan to achieve wishes. The input dtoIn will contain invalid values.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        MonthlyPlanningDtoIn dtoIn = new MonthlyPlanningDtoIn();
        dtoIn.setAmountSaved(null);
        dtoIn.setMonthlyProfit(BigDecimal.valueOf(-1000));

        // Execution
        Either<Failure, Success<MonthlyPlanningDtoOut>> monthlyPlanning = financialPlanningService.getMonthlyPlanning(dtoIn);

        // Verification
        assertTrue(monthlyPlanning.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, monthlyPlanning.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "The amount saved is not specified., The monthly profit must be a positive number.";
        assertEquals(expectedMessage, monthlyPlanning.getLeft().getMessage());
    }

    @Test
    void getMonthlyPlanning_UserNotSignedIn() {
        log.info("Test of creating a monthly savings plan to achieve wishes. User will not be signed-in. This should not occur, an unsigned user should not have permission to call the appropriate method.");

        // Data preparation
        assertEmptyDatabase();

        MonthlyPlanningDtoIn dtoIn = createMonthlyPlanningDtoIn();

        // Execution
        Either<Failure, Success<MonthlyPlanningDtoOut>> monthlyPlanning = financialPlanningService.getMonthlyPlanning(dtoIn);

        // Verification
        assertTrue(monthlyPlanning.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, monthlyPlanning.getLeft().getHttpStatus());

        assertEmptyDatabase();

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, monthlyPlanning.getLeft().getMessage());
    }

    @Test
    void getMonthlyPlanning_UserNotFoundById() {
        log.info("Test of creating a monthly savings plan to achieve wishes. The user will not be found in the database to verify identity according to the id obtained from the Spring context (token).");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setPasswordHash(user.getPasswordHash());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        MonthlyPlanningDtoIn dtoIn = createMonthlyPlanningDtoIn();

        // Execution
        Either<Failure, Success<MonthlyPlanningDtoOut>> monthlyPlanning = financialPlanningService.getMonthlyPlanning(dtoIn);

        // Verification
        assertTrue(monthlyPlanning.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, monthlyPlanning.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, monthlyPlanning.getLeft().getMessage());
    }

    @Test
    @Transactional
    void getMonthlyPlanning_UserHasNoWish() {
        log.info("Test of creating a monthly savings plan to achieve wishes. The user will not have any wishes for which a financial plan should be created. Therefore, only a normal financial (/ savings) plan with a profit will be created, ie without expense on wishes.");

        // Data preparation
        User user = UserTestSupport.createUser(false);
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        MonthlyPlanningDtoIn dtoIn = createMonthlyPlanningDtoIn();

        // Execution
        Either<Failure, Success<MonthlyPlanningDtoOut>> monthlyPlanning = financialPlanningService.getMonthlyPlanning(dtoIn);

        // Verification
        assertTrue(monthlyPlanning.isRight());
        assertEquals(HttpStatus.OK, monthlyPlanning.get().getHttpStatus());

        assertDatabaseSize(1, 0);

        MonthlyPlanningDtoOut dtoOut = monthlyPlanning.get().getBody();

        List<MonthlyOverview> monthlyOverviewMinus25List = dtoOut.getMonthlyOverviewMinus25List();
        assertThat(monthlyOverviewMinus25List, hasSize(40));
        int expectedAmountSavedMinus25 = 0;
        for (int i = 0; i < 40; i++) {
            expectedAmountSavedMinus25 += 4875;
            assertMonthlyOverview(monthlyOverviewMinus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus25));
        }

        List<MonthlyOverview> monthlyOverviewMinus5List = dtoOut.getMonthlyOverviewMinus5List();
        assertThat(monthlyOverviewMinus5List, hasSize(40));
        int expectedAmountSavedMinus5 = 0;
        for (int i = 0; i < 40; i++) {
            expectedAmountSavedMinus5 += 4750;
            assertMonthlyOverview(monthlyOverviewMinus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus5));
        }

        List<MonthlyOverview> monthlyOverview0List = dtoOut.getMonthlyOverview0List();
        assertThat(monthlyOverview0List, hasSize(40));
        int expectedAmountSaved0 = 0;
        for (int i = 0; i < 40; i++) {
            expectedAmountSaved0 += 5000;
            assertMonthlyOverview(monthlyOverview0List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSaved0));
        }

        List<MonthlyOverview> monthlyOverviewPlus25List = dtoOut.getMonthlyOverviewPlus25List();
        assertThat(monthlyOverviewPlus25List, hasSize(40));
        int expectedAmountSavedPlus25 = 0;
        for (int i = 0; i < 40; i++) {
            expectedAmountSavedPlus25 += 5125;
            assertMonthlyOverview(monthlyOverviewPlus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus25));
        }

        List<MonthlyOverview> monthlyOverviewPlus5List = dtoOut.getMonthlyOverviewPlus5List();
        assertThat(monthlyOverviewPlus5List, hasSize(40));
        int expectedAmountSavedPlus5 = 0;
        for (int i = 0; i < 40; i++) {
            expectedAmountSavedPlus5 += 5250;
            assertMonthlyOverview(monthlyOverviewPlus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus5));
        }

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus25List = dtoOut.getAffordedWishesOverviewMinus25List();
        assertThat(affordedWishesOverviewMinus25List, empty());

        List<MonthlyAffordedWishOverview> affordedWishesOverviewMinus5List = dtoOut.getAffordedWishesOverviewMinus5List();
        assertThat(affordedWishesOverviewMinus5List, empty());

        List<MonthlyAffordedWishOverview> affordedWishesOverview0List = dtoOut.getAffordedWishesOverview0List();
        assertThat(affordedWishesOverview0List, empty());

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus25List = dtoOut.getAffordedWishesOverviewPlus25List();
        assertThat(affordedWishesOverviewPlus25List, empty());

        List<MonthlyAffordedWishOverview> affordedWishesOverviewPlus5List = dtoOut.getAffordedWishesOverviewPlus5List();
        assertThat(affordedWishesOverviewPlus5List, empty());
    }

    @Test
    @Transactional
    void getAnnualPlanning_Hds() {
        log.info("Test of creation of annual financial (/ savings) plan to buy wishes.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        user.getWishList().get(0).setPrice(BigDecimal.valueOf(55000));
        user.getWishList().get(1).setPrice(BigDecimal.valueOf(60000));
        user.getWishList().get(2).setPrice(BigDecimal.valueOf(70000));
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        AnnualPlanningDtoIn dtoIn = createAnnualPlanningDtoIn();

        // Execution
        Either<Failure, Success<AnnualPlanningDtoOut>> annualPlanning = financialPlanningService.getAnnualPlanning(dtoIn);

        // Verification
        assertTrue(annualPlanning.isRight());
        assertEquals(HttpStatus.OK, annualPlanning.get().getHttpStatus());

        assertDatabaseSize(1, 3);

        AnnualPlanningDtoOut dtoOut = annualPlanning.get().getBody();

        List<AnnualOverview> annualOverviewMinus25List = dtoOut.getAnnualOverviewMinus25List();
        assertThat(annualOverviewMinus25List, hasSize(35));
        int expectedAmountSavedMinus25 = 0;
        for (int i = 0; i < 35; i++) {
            if (i == 1) {
                expectedAmountSavedMinus25 = 3500;
            } else if (i == 2) {
                expectedAmountSavedMinus25 = 2000;
            } else if (i == 4) {
                expectedAmountSavedMinus25 = 49000;
            }
            expectedAmountSavedMinus25 += 58500;
            assertAnnualOverview(annualOverviewMinus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus25));
        }

        List<AnnualOverview> annualOverviewMinus5List = dtoOut.getAnnualOverviewMinus5List();
        assertThat(annualOverviewMinus5List, hasSize(35));
        int expectedAmountSavedMinus5 = 0;
        for (int i = 0; i < 35; i++) {
            if (i == 1) {
                expectedAmountSavedMinus5 = 2000;
            } else if (i == 3) {
                expectedAmountSavedMinus5 = 56000;
            } else if (i == 4) {
                expectedAmountSavedMinus5 = 43000;
            }
            expectedAmountSavedMinus5 += 57000;
            assertAnnualOverview(annualOverviewMinus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus5));
        }

        List<AnnualOverview> annualOverview0List = dtoOut.getAnnualOverview0List();
        assertThat(annualOverview0List, hasSize(35));
        int expectedAmountSaved0 = 0;
        for (int i = 0; i < 35; i++) {
            if (i == 1) {
                expectedAmountSaved0 = 5000;
            } else if (i == 2) {
                expectedAmountSaved0 = 5000;
            } else if (i == 4) {
                expectedAmountSaved0 = 55000;
            }
            expectedAmountSaved0 += 60000;
            assertAnnualOverview(annualOverview0List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSaved0));
        }

        List<AnnualOverview> annualOverviewPlus25List = dtoOut.getAnnualOverviewPlus25List();
        assertThat(annualOverviewPlus25List, hasSize(35));
        int expectedAmountSavedPlus25 = 0;
        for (int i = 0; i < 35; i++) {
            if (i == 1) {
                expectedAmountSavedPlus25 = 6500;
            } else if (i == 2) {
                expectedAmountSavedPlus25 = 8000;
            } else if (i == 4) {
                expectedAmountSavedPlus25 = 61000;
            }
            expectedAmountSavedPlus25 += 61500;
            assertAnnualOverview(annualOverviewPlus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus25));
        }

        List<AnnualOverview> annualOverviewPlus5List = dtoOut.getAnnualOverviewPlus5List();
        assertThat(annualOverviewPlus5List, hasSize(35));
        int expectedAmountSavedPlus5 = 0;
        for (int i = 0; i < 35; i++) {
            if (i == 1) {
                expectedAmountSavedPlus5 = 8000;
            } else if (i == 2) {
                expectedAmountSavedPlus5 = 11000;
            } else if (i == 3) {
                expectedAmountSavedPlus5 = 4000;
            }
            expectedAmountSavedPlus5 += 63000;
            assertAnnualOverview(annualOverviewPlus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus5));
        }

        List<AnnualAffordedWishOverview> affordedWishesOverviewMinus25List = dtoOut.getAffordedWishesOverviewMinus25List();
        assertThat(affordedWishesOverviewMinus25List, hasSize(3));
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus25List.get(0), 1, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus25List.get(1), 2, "Wish 1");
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus25List.get(2), 4, "Wish 2");

        List<AnnualAffordedWishOverview> affordedWishesOverviewMinus5List = dtoOut.getAffordedWishesOverviewMinus5List();
        assertThat(affordedWishesOverviewMinus5List, hasSize(3));
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus5List.get(0), 1, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus5List.get(1), 3, "Wish 1");
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus5List.get(2), 4, "Wish 2");

        List<AnnualAffordedWishOverview> affordedWishesOverview0List = dtoOut.getAffordedWishesOverview0List();
        assertThat(affordedWishesOverview0List, hasSize(3));
        assertAnnualAffordedWishOverview(affordedWishesOverview0List.get(0), 1, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverview0List.get(1), 2, "Wish 1");
        assertAnnualAffordedWishOverview(affordedWishesOverview0List.get(2), 4, "Wish 2");

        List<AnnualAffordedWishOverview> affordedWishesOverviewPlus25List = dtoOut.getAffordedWishesOverviewPlus25List();
        assertThat(affordedWishesOverviewPlus25List, hasSize(3));
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus25List.get(0), 1, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus25List.get(1), 2, "Wish 1");
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus25List.get(2), 4, "Wish 2");

        List<AnnualAffordedWishOverview> affordedWishesOverviewPlus5List = dtoOut.getAffordedWishesOverviewPlus5List();
        assertThat(affordedWishesOverviewPlus5List, hasSize(3));
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus5List.get(0), 1, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus5List.get(1), 2, "Wish 1");
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus5List.get(2), 3, "Wish 2");
    }

    @Test
    @Transactional
    void getAnnualPlanning_Hds_FinancialPlanWillNotBeCreatedForAllWishes() {
        log.info("Test of creation of annual financial (/ savings) plan to buy wishes. Not all user-defined wishes will be found in this test. Large amounts of user wishes and low user earnings will be entered, so even the set number of years will not be enough for the user to achieve their wishes.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        user.getWishList().get(0).setPrice(BigDecimal.valueOf(55000));
        user.getWishList().get(1).setPrice(BigDecimal.valueOf(60000));
        user.getWishList().get(2).setPrice(BigDecimal.valueOf(999999999.999999999999999));
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        AnnualPlanningDtoIn dtoIn = createAnnualPlanningDtoIn();
        dtoIn.setMonthlyProfit(BigDecimal.valueOf(1000));

        // Execution
        Either<Failure, Success<AnnualPlanningDtoOut>> annualPlanning = financialPlanningService.getAnnualPlanning(dtoIn);

        // Verification
        assertTrue(annualPlanning.isRight());
        assertEquals(HttpStatus.OK, annualPlanning.get().getHttpStatus());

        assertDatabaseSize(1, 3);

        AnnualPlanningDtoOut dtoOut = annualPlanning.get().getBody();

        List<AnnualOverview> annualOverviewMinus25List = dtoOut.getAnnualOverviewMinus25List();
        int expectedAmountSavedMinus25 = 0;
        for (int i = 0; i < 30; i++) {
            if (i == 5) {
                expectedAmountSavedMinus25 = 3500;
            } else if (i == 10) {
                expectedAmountSavedMinus25 = 2000;
            }
            expectedAmountSavedMinus25 += 11700;
            assertAnnualOverview(annualOverviewMinus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus25));
        }

        List<AnnualOverview> annualOverviewMinus5List = dtoOut.getAnnualOverviewMinus5List();
        int expectedAmountSavedMinus5 = 0;
        for (int i = 0; i < 30; i++) {
            if (i == 5) {
                expectedAmountSavedMinus5 = 2000;
            } else if (i == 11) {
                expectedAmountSavedMinus5 = 10400;
            }
            expectedAmountSavedMinus5 += 11400;
            assertAnnualOverview(annualOverviewMinus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus5));
        }

        List<AnnualOverview> annualOverview0List = dtoOut.getAnnualOverview0List();
        int expectedAmountSaved0 = 0;
        for (int i = 0; i < 30; i++) {
            if (i == 5 || i == 10) {
                expectedAmountSaved0 = 5000;
            }
            expectedAmountSaved0 += 12000;
            assertAnnualOverview(annualOverview0List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSaved0));
        }

        List<AnnualOverview> annualOverviewPlus25List = dtoOut.getAnnualOverviewPlus25List();
        int expectedAmountSavedPlus25 = 0;
        for (int i = 0; i < 30; i++) {
            if (i == 5) {
                expectedAmountSavedPlus25 = 6500;
            } else if (i == 10) {
                expectedAmountSavedPlus25 = 8000;
            }
            expectedAmountSavedPlus25 += 12300;
            assertAnnualOverview(annualOverviewPlus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus25));
        }

        List<AnnualOverview> annualOverviewPlus5List = dtoOut.getAnnualOverviewPlus5List();
        int expectedAmountSavedPlus5 = 0;
        for (int i = 0; i < 30; i++) {
            if (i == 5) {
                expectedAmountSavedPlus5 = 8000;
            } else if (i == 10) {
                expectedAmountSavedPlus5 = 11000;
            }
            expectedAmountSavedPlus5 += 12600;
            assertAnnualOverview(annualOverviewPlus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus5));
        }

        List<AnnualAffordedWishOverview> affordedWishesOverviewMinus25List = dtoOut.getAffordedWishesOverviewMinus25List();
        assertThat(affordedWishesOverviewMinus25List, hasSize(2));
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus25List.get(0), 5, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus25List.get(1), 10, "Wish 1");

        List<AnnualAffordedWishOverview> affordedWishesOverviewMinus5List = dtoOut.getAffordedWishesOverviewMinus5List();
        assertThat(affordedWishesOverviewMinus5List, hasSize(2));
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus5List.get(0), 5, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus5List.get(1), 11, "Wish 1");

        List<AnnualAffordedWishOverview> affordedWishesOverview0List = dtoOut.getAffordedWishesOverview0List();
        assertThat(affordedWishesOverview0List, hasSize(2));
        assertAnnualAffordedWishOverview(affordedWishesOverview0List.get(0), 5, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverview0List.get(1), 10, "Wish 1");

        List<AnnualAffordedWishOverview> affordedWishesOverviewPlus25List = dtoOut.getAffordedWishesOverviewPlus25List();
        assertThat(affordedWishesOverviewPlus25List, hasSize(2));
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus25List.get(0), 5, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus25List.get(1), 10, "Wish 1");

        List<AnnualAffordedWishOverview> affordedWishesOverviewPlus5List = dtoOut.getAffordedWishesOverviewPlus5List();
        assertThat(affordedWishesOverviewPlus5List, hasSize(2));
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus5List.get(0), 5, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus5List.get(1), 10, "Wish 1");
    }

    @Test
    @Transactional
    void getAnnualPlanning_MoreExpensiveWishFirst() {
        log.info("Test of creation of annual financial (/ savings) plan to buy wishes. The more expensive wishes will be set to buy first, although it would be possible to buy the others first.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        user.getWishList().get(0).setPrice(BigDecimal.valueOf(55000));
        user.getWishList().get(1).setPrice(BigDecimal.valueOf(30000));
        user.getWishList().get(2).setPrice(BigDecimal.valueOf(30000));
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        AnnualPlanningDtoIn dtoIn = createAnnualPlanningDtoIn();

        // Execution
        Either<Failure, Success<AnnualPlanningDtoOut>> annualPlanning = financialPlanningService.getAnnualPlanning(dtoIn);

        // Verification
        assertTrue(annualPlanning.isRight());
        assertEquals(HttpStatus.OK, annualPlanning.get().getHttpStatus());

        assertDatabaseSize(1, 3);

        AnnualPlanningDtoOut dtoOut = annualPlanning.get().getBody();

        List<AnnualOverview> annualOverviewMinus25List = dtoOut.getAnnualOverviewMinus25List();
        assertThat(annualOverviewMinus25List, hasSize(35));
        int expectedAmountSavedMinus25 = 0;
        for (int i = 0; i < 35; i++) {
            if (i == 1) {
                expectedAmountSavedMinus25 = 3500;
            } else if (i == 2) {
                expectedAmountSavedMinus25 = 2000;
            }
            expectedAmountSavedMinus25 += 58500;
            assertAnnualOverview(annualOverviewMinus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus25));
        }

        List<AnnualOverview> annualOverviewMinus5List = dtoOut.getAnnualOverviewMinus5List();
        assertThat(annualOverviewMinus5List, hasSize(35));
        int expectedAmountSavedMinus5 = 0;
        for (int i = 0; i < 35; i++) {
            if (i == 1) {
                expectedAmountSavedMinus5 = 2000;
            } else if (i == 2) {
                expectedAmountSavedMinus5 = 29000;
            } else if (i == 3) {
                expectedAmountSavedMinus5 = 56000;
            }
            expectedAmountSavedMinus5 += 57000;
            assertAnnualOverview(annualOverviewMinus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus5));
        }

        List<AnnualOverview> annualOverview0List = dtoOut.getAnnualOverview0List();
        assertThat(annualOverview0List, hasSize(35));
        int expectedAmountSaved0 = 0;
        for (int i = 0; i < 35; i++) {
            if (i == 1 || i == 2) {
                expectedAmountSaved0 = 5000;
            }
            expectedAmountSaved0 += 60000;
            assertAnnualOverview(annualOverview0List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSaved0));
        }

        List<AnnualOverview> annualOverviewPlus25List = dtoOut.getAnnualOverviewPlus25List();
        assertThat(annualOverviewPlus25List, hasSize(35));
        int expectedAmountSavedPlus25 = 0;
        for (int i = 0; i < 35; i++) {
            if (i == 1) {
                expectedAmountSavedPlus25 = 6500;
            } else if (i == 2) {
                expectedAmountSavedPlus25 = 8000;
            }
            expectedAmountSavedPlus25 += 61500;
            assertAnnualOverview(annualOverviewPlus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus25));
        }

        List<AnnualOverview> annualOverviewPlus5List = dtoOut.getAnnualOverviewPlus5List();
        assertThat(annualOverviewPlus5List, hasSize(35));
        int expectedAmountSavedPlus5 = 0;
        for (int i = 0; i < 35; i++) {
            if (i == 1) {
                expectedAmountSavedPlus5 = 8000;
            } else if (i == 2) {
                expectedAmountSavedPlus5 = 11000;
            }
            expectedAmountSavedPlus5 += 63000;
            assertAnnualOverview(annualOverviewPlus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus5));
        }

        List<AnnualAffordedWishOverview> affordedWishesOverviewMinus25List = dtoOut.getAffordedWishesOverviewMinus25List();
        assertThat(affordedWishesOverviewMinus25List, hasSize(3));
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus25List.get(0), 1, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus25List.get(1), 2, "Wish 1");
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus25List.get(2), 2, "Wish 2");

        List<AnnualAffordedWishOverview> affordedWishesOverviewMinus5List = dtoOut.getAffordedWishesOverviewMinus5List();
        assertThat(affordedWishesOverviewMinus5List, hasSize(3));
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus5List.get(0), 1, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus5List.get(1), 2, "Wish 1");
        assertAnnualAffordedWishOverview(affordedWishesOverviewMinus5List.get(2), 3, "Wish 2");

        List<AnnualAffordedWishOverview> affordedWishesOverview0List = dtoOut.getAffordedWishesOverview0List();
        assertThat(affordedWishesOverview0List, hasSize(3));
        assertAnnualAffordedWishOverview(affordedWishesOverview0List.get(0), 1, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverview0List.get(1), 2, "Wish 1");
        assertAnnualAffordedWishOverview(affordedWishesOverview0List.get(2), 2, "Wish 2");

        List<AnnualAffordedWishOverview> affordedWishesOverviewPlus25List = dtoOut.getAffordedWishesOverviewPlus25List();
        assertThat(affordedWishesOverviewPlus25List, hasSize(3));
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus25List.get(0), 1, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus25List.get(1), 2, "Wish 1");
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus25List.get(2), 2, "Wish 2");

        List<AnnualAffordedWishOverview> affordedWishesOverviewPlus5List = dtoOut.getAffordedWishesOverviewPlus5List();
        assertThat(affordedWishesOverviewPlus5List, hasSize(3));
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus5List.get(0), 1, "Wish 0");
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus5List.get(1), 2, "Wish 1");
        assertAnnualAffordedWishOverview(affordedWishesOverviewPlus5List.get(2), 2, "Wish 2");
    }

    @Test
    void getAnnualPlanning_InvalidDtoIn() {
        log.info("Test of creation of annual financial (/ savings) plan to buy wishes. The input dtoIn will contain invalid data.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        AnnualPlanningDtoIn dtoIn = createAnnualPlanningDtoIn();
        dtoIn.setAmountSaved(BigDecimal.valueOf(-500L));
        dtoIn.setMonthlyProfit(null);

        // Execution
        Either<Failure, Success<AnnualPlanningDtoOut>> annualPlanning = financialPlanningService.getAnnualPlanning(dtoIn);

        // Verification
        assertTrue(annualPlanning.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, annualPlanning.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "The amount saved must be a positive number., The monthly profit is not specified.";
        assertEquals(expectedMessage, annualPlanning.getLeft().getMessage());
    }

    @Test
    void getAnnualPlanning_UserNotSignedIn() {
        log.info("Test of creation of annual financial (/ savings) plan to buy wishes. The user will not be signed-in, it would not be possible to retrieve the wish of the particular user (for example). This should not be the case. The method cannot be called by an unsigned user.");

        // Data preparation
        assertEmptyDatabase();

        AnnualPlanningDtoIn dtoIn = createAnnualPlanningDtoIn();

        // Execution
        Either<Failure, Success<AnnualPlanningDtoOut>> annualPlanning = financialPlanningService.getAnnualPlanning(dtoIn);

        // Verification
        assertTrue(annualPlanning.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, annualPlanning.getLeft().getHttpStatus());

        assertEmptyDatabase();

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, annualPlanning.getLeft().getMessage());
    }

    @Test
    void getAnnualPlanning_UserNotFoundById() {
        log.info("Test of creation of annual financial (/ savings) plan to buy wishes. User will not be found in database by id obtained from Spring context (token). It will not be possible to verify his identity.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setPasswordHash(user.getPasswordHash());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        AnnualPlanningDtoIn dtoIn = createAnnualPlanningDtoIn();

        // Execution
        Either<Failure, Success<AnnualPlanningDtoOut>> annualPlanning = financialPlanningService.getAnnualPlanning(dtoIn);

        // Verification
        assertTrue(annualPlanning.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, annualPlanning.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, annualPlanning.getLeft().getMessage());
    }

    @Test
    @Transactional
    void getAnnualPlanning_UserHasNoWish() {
        log.info("Test of creation of annual financial (/ savings) plan to buy wishes. Therefore, only a normal financial (/ savings) plan with a profit will be created, ie without expense on wishes.");

        // Data preparation
        User user = UserTestSupport.createUser(false);
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        AnnualPlanningDtoIn dtoIn = createAnnualPlanningDtoIn();

        // Execution
        Either<Failure, Success<AnnualPlanningDtoOut>> annualPlanning = financialPlanningService.getAnnualPlanning(dtoIn);

        // Verification
        assertTrue(annualPlanning.isRight());
        assertEquals(HttpStatus.OK, annualPlanning.get().getHttpStatus());

        assertDatabaseSize(1, 0);

        AnnualPlanningDtoOut dtoOut = annualPlanning.get().getBody();

        List<AnnualOverview> annualOverviewMinus25List = dtoOut.getAnnualOverviewMinus25List();
        assertThat(annualOverviewMinus25List, hasSize(35));
        int expectedAmountSavedMinus25 = 0;
        for (int i = 0; i < 35; i++) {
            expectedAmountSavedMinus25 += 58500;
            assertAnnualOverview(annualOverviewMinus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus25));
        }

        List<AnnualOverview> annualOverviewMinus5List = dtoOut.getAnnualOverviewMinus5List();
        assertThat(annualOverviewMinus5List, hasSize(35));
        int expectedAmountSavedMinus5 = 0;
        for (int i = 0; i < 35; i++) {
            expectedAmountSavedMinus5 += 57000;
            assertAnnualOverview(annualOverviewMinus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedMinus5));
        }

        List<AnnualOverview> annualOverview0List = dtoOut.getAnnualOverview0List();
        assertThat(annualOverview0List, hasSize(35));
        int expectedAmountSaved0 = 0;
        for (int i = 0; i < 35; i++) {
            expectedAmountSaved0 += 60000;
            assertAnnualOverview(annualOverview0List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSaved0));
        }

        List<AnnualOverview> annualOverviewPlus25List = dtoOut.getAnnualOverviewPlus25List();
        assertThat(annualOverviewPlus25List, hasSize(35));
        int expectedAmountSavedPlus25 = 0;
        for (int i = 0; i < 35; i++) {
            expectedAmountSavedPlus25 += 61500;
            assertAnnualOverview(annualOverviewPlus25List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus25));
        }

        List<AnnualOverview> annualOverviewPlus5List = dtoOut.getAnnualOverviewPlus5List();
        assertThat(annualOverviewPlus5List, hasSize(35));
        int expectedAmountSavedPlus5 = 0;
        for (int i = 0; i < 35; i++) {
            expectedAmountSavedPlus5 += 63000;
            assertAnnualOverview(annualOverviewPlus5List.get(i), i + 1, BigDecimal.valueOf(expectedAmountSavedPlus5));
        }

        List<AnnualAffordedWishOverview> affordedWishesOverviewMinus25List = dtoOut.getAffordedWishesOverviewMinus25List();
        assertThat(affordedWishesOverviewMinus25List, empty());

        List<AnnualAffordedWishOverview> affordedWishesOverviewMinus5List = dtoOut.getAffordedWishesOverviewMinus5List();
        assertThat(affordedWishesOverviewMinus5List, empty());

        List<AnnualAffordedWishOverview> affordedWishesOverview0List = dtoOut.getAffordedWishesOverview0List();
        assertThat(affordedWishesOverview0List, empty());

        List<AnnualAffordedWishOverview> affordedWishesOverviewPlus25List = dtoOut.getAffordedWishesOverviewPlus25List();
        assertThat(affordedWishesOverviewPlus25List, empty());

        List<AnnualAffordedWishOverview> affordedWishesOverviewPlus5List = dtoOut.getAffordedWishesOverviewPlus5List();
        assertThat(affordedWishesOverviewPlus5List, empty());
    }
}
