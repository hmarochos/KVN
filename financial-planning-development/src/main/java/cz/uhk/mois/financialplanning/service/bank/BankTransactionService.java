package cz.uhk.mois.financialplanning.service.bank;

import cz.uhk.mois.financialplanning.model.dto.transaction.bank.AddBankTransactionDtoIn;
import cz.uhk.mois.financialplanning.model.dto.transaction.bank.BankTransactionDtoOut;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * It serves for communication with the banking API provided by Unicorn.
 *
 * @author Jan Krunčík
 * @since 03.04.2020 14:58
 */

public interface BankTransactionService {

    /**
     * Create / Add / Save new transaction to database simulating bank.
     *
     * @param dtoIn
     *         new transaction information
     *
     * @return mono (kind of promise) return of required value (created transaction)
     */
    Mono<BankTransactionDtoOut> add(AddBankTransactionDtoIn dtoIn);

    /**
     * Obtain signed-in user transactions with accountId that were created at the specified interval (dateFrom and
     * dateTo).
     *
     * @param dateFrom
     *         beginning of the interval, transactions created after this date with the same accountId will be in the
     *         selection
     * @param dateTo
     *         end of the interval, transactions created before this date with the same accountId will be in the
     *         selection
     * @param accountId
     *         account number of the signed-in user whose transactions are to be selected (/ retrieved)
     *
     * @return mono (/ promise) to ensure that transactions that meet the above described requirements will be obtained
     */
    Mono<List<UniTransaction>> getByDateInterval(ZonedDateTime dateFrom, ZonedDateTime dateTo, Long accountId);
}
