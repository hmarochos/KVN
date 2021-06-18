package ua.uhk.mois.financialplanning.service;

import ua.uhk.mois.financialplanning.model.dto.transaction.AddTransactionDtoIn;
import ua.uhk.mois.financialplanning.model.dto.transaction.GetByDateIntervalDtoIn;
import ua.uhk.mois.financialplanning.model.dto.transaction.GetByDateIntervalDtoOut;
import ua.uhk.mois.financialplanning.model.dto.transaction.LastMonthTransactionsOverviewDtoOut;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.BankTransactionDtoOut;
import ua.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import ua.uhk.mois.financialplanning.response.Failure;
import ua.uhk.mois.financialplanning.response.Success;
import io.vavr.control.Either;

import java.util.List;

/**
 * Data processing before sending a request to the banking API (bank simulation).
 *
 * @author KVN
 * @since 03.04.2021 1:47
 */

public interface TransactionService {

    /**
     * Transaction processing and completion of necessary data and sending it to the database (bank simulation).
     *
     * @param dtoIn
     *         basic information about the transaction to be created
     *
     * @return right with the data of the created transaction, otherwise left with information about the error
     */
    Either<Failure, Success<BankTransactionDtoOut>> add(AddTransactionDtoIn dtoIn);

    /**
     * Get / Retrieve those transactions from the provided database (banking API simulation) that meet the specified
     * date range and belong to a specific user (according to accountId). <br/>
     * <i>It intentionally reads data from the "bank", because the user can enter any interval, not the data for the
     * last month only.</i>
     *
     * @param dtoIn
     *         the interval at which transactions were to be executed to be included in the selection
     *
     * @return right with a list of transactions that were executed (/ paid) at the specified interval, otherwise left
     * with information about the error
     */
    Either<Failure, Success<GetByDateIntervalDtoOut>> getByDateInterval(GetByDateIntervalDtoIn dtoIn);

    /**
     * Get a monthly overview of signed-in user transactions for the last month. <br/>
     * <i>Data will be retrieved from a backed up database (used by us, not "borrowed").</i>
     * <br/>
     * <i>The data obtained will contain one item for each day of the month, which will be the sum of all previous
     * transactions (from all previous days + those transactions for that day).</i>
     *
     * @return right with the above transaction list for each day of the previous month, otherwise left with the error
     * information
     */
    Either<Failure, Success<LastMonthTransactionsOverviewDtoOut>> getLastMonthTransactionsOverview();

    /**
     * Find all transactions that relate to the account (users) with accountId. <br/>
     * <i>Transactions will be retrieved from "our" database (backed up), which stores the user's transactions for the
     * last month.</i>
     *
     * @param accountId
     *         the account number of the user to which transactions should be found
     *
     * @return right with the list of transactions related to the user with the corresponding accountId, otherwise left
     * with information about the error
     */
    Either<Failure, List<Transaction>> findTransactionsByAccountId(Long accountId);

}
