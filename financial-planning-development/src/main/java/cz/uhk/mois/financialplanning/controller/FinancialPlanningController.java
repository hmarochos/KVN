package cz.uhk.mois.financialplanning.controller;

import cz.uhk.mois.financialplanning.controller.path.UrlConstant;
import cz.uhk.mois.financialplanning.model.dto.planning.month.MonthlyPlanningDtoIn;
import cz.uhk.mois.financialplanning.model.dto.planning.month.MonthlyPlanningDtoOut;
import cz.uhk.mois.financialplanning.model.dto.planning.year.AnnualPlanningDtoIn;
import cz.uhk.mois.financialplanning.model.dto.planning.year.AnnualPlanningDtoOut;
import cz.uhk.mois.financialplanning.response.FailureResponse;
import cz.uhk.mois.financialplanning.response.ServerResponse;
import cz.uhk.mois.financialplanning.response.Success;
import cz.uhk.mois.financialplanning.service.FinancialPlanningService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;

/**
 * @author Jan Krunčík
 * @since 10.04.2020 23:01
 */

@RestController
@RequestMapping(UrlConstant.FINANCIAL_PLANNING)
@Log4j2
public class FinancialPlanningController {

    private final FinancialPlanningService financialPlanningService;

    public FinancialPlanningController(FinancialPlanningService financialPlanningService) {
        this.financialPlanningService = financialPlanningService;
    }

    @PostMapping(path = UrlConstant.FINANCIAL_PLANNING_MONTHLY_PLAN, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<MonthlyPlanningDtoOut>> getMonthlyPlanning(@RequestBody MonthlyPlanningDtoIn dtoIn) {
        log.info("Obtaining a monthly financial plan to achieve the wish of the signed-in user. {}", dtoIn);
        return financialPlanningService.getMonthlyPlanning(dtoIn)
                                       .mapLeft(failure -> new FailureResponse<MonthlyPlanningDtoOut>().createResponse(failure))
                                       .fold(Function.identity(), Success::createResponse);
    }

    @PostMapping(path = UrlConstant.FINANCIAL_PLANNING_ANNUAL_PLAN, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<AnnualPlanningDtoOut>> getAnnualPlanning(@RequestBody AnnualPlanningDtoIn dtoIn) {
        log.info("Obtaining an annual financial plan to achieve the wishes of the signed-in user. {}", dtoIn);
        return financialPlanningService.getAnnualPlanning(dtoIn)
                                       .mapLeft(failure -> new FailureResponse<AnnualPlanningDtoOut>().createResponse(failure))
                                       .fold(Function.identity(), Success::createResponse);
    }
}
