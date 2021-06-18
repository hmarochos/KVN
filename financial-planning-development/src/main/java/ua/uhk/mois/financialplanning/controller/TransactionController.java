package ua.uhk.mois.financialplanning.controller;

import ua.uhk.mois.financialplanning.controller.path.UrlConstant;
import ua.uhk.mois.financialplanning.model.dto.transaction.AddTransactionDtoIn;
import ua.uhk.mois.financialplanning.model.dto.transaction.GetByDateIntervalDtoIn;
import ua.uhk.mois.financialplanning.model.dto.transaction.GetByDateIntervalDtoOut;
import ua.uhk.mois.financialplanning.model.dto.transaction.LastMonthTransactionsOverviewDtoOut;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.BankTransactionDtoOut;
import ua.uhk.mois.financialplanning.response.FailureResponse;
import ua.uhk.mois.financialplanning.response.ServerResponse;
import ua.uhk.mois.financialplanning.response.Success;
import ua.uhk.mois.financialplanning.service.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;

/**
 * @author KVN
 * @since 03.04.2021 1:45
 */

@RestController
@RequestMapping(UrlConstant.TRANSACTION)
@Log4j2
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<BankTransactionDtoOut>> add(@RequestBody AddTransactionDtoIn dtoIn) {
        log.info("Add / create transaction. {}", dtoIn);
        return transactionService.add(dtoIn)
                                 .mapLeft(failure -> new FailureResponse<BankTransactionDtoOut>().createResponse(failure))
                                 .fold(Function.identity(), Success::createResponse);
    }

    @PostMapping(path = UrlConstant.TRANSACTION_GET_BY_DATE_INTERVAL, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<GetByDateIntervalDtoOut>> getByDateInterval(@RequestBody GetByDateIntervalDtoIn dtoIn) {
        log.info("Obtaining (/ filtering) transactions according to the specified interval. {}", dtoIn);
        return transactionService.getByDateInterval(dtoIn)
                                 .mapLeft(failure -> new FailureResponse<GetByDateIntervalDtoOut>().createResponse(failure))
                                 .fold(Function.identity(), Success::createResponse);
    }

    @GetMapping(path = UrlConstant.TRANSACTION_LAST_MONTH_TRANSACTIONS_OVERVIEW, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<LastMonthTransactionsOverviewDtoOut>> getLastMonthTransactionsOverview() {
        log.info("Get an overview of transactions made last month.");
        return transactionService.getLastMonthTransactionsOverview()
                                 .mapLeft(failure -> new FailureResponse<LastMonthTransactionsOverviewDtoOut>().createResponse(failure))
                                 .fold(Function.identity(), Success::createResponse);
    }
}
