package ua.uhk.mois.financialplanning.service;

import ua.uhk.mois.financialplanning.bank.DaySupport;
import ua.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import ua.uhk.mois.financialplanning.repository.TransactionRepository;
import ua.uhk.mois.financialplanning.service.bank.BankTransactionService;
import ua.uhk.mois.financialplanning.service.bank.UniTransaction;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Log4j2
public class CopyTransactionServiceImpl implements CopyTransactionService {

    private final TransactionRepository transactionRepository;
    private final BankTransactionService bankTransactionService;
    private final ModelMapper modelMapper;
    private final DaySupport daySupport;
    private final Clock clock;

    public CopyTransactionServiceImpl(TransactionRepository transactionRepository, BankTransactionService bankTransactionService, ModelMapper modelMapper, DaySupport daySupport, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.bankTransactionService = bankTransactionService;
        this.modelMapper = modelMapper;
        this.daySupport = daySupport;
        this.clock = clock;
    }

    /**
     * Check for retrieved transactions that were created at the specified interval for an error when attempting to
     * retrieve them.
     *
     * @param uniTransactionListMono
     *         "function (/ something like a promise)" to retrieve and return transactions that were created at the
     *         specified interval
     *
     * @return right list of loaded transactions, otherwise left (null)
     */
    private static Either<Void, List<UniTransaction>> handleFoundTransactions(Mono<List<UniTransaction>> uniTransactionListMono) {
        Either<Void, Either<Void, List<UniTransaction>>> foundTransactions = getFoundTransactions(uniTransactionListMono);

        if (foundTransactions.isLeft()) {
            return Either.left(foundTransactions.getLeft());
        }

        // Check whether there was an error in retrieving the transactions
        Either<Void, List<UniTransaction>> getByDateIntervalDtoOuts = foundTransactions.get();
        if (getByDateIntervalDtoOuts.isLeft()) {
            return Either.left(getByDateIntervalDtoOuts.getLeft());
        }

        return Either.right(getByDateIntervalDtoOuts.get());
    }

    private static Either<Void, Either<Void, List<UniTransaction>>> getFoundTransactions(Mono<List<UniTransaction>> uniTransactionListMono) {
        return Try.of(() -> uniTransactionListMono.toFuture()
                                                  .handle(CopyTransactionServiceImpl::handleFoundTransactions)
                                                  .get())
                  .toEither()
                  .mapLeft(CopyTransactionServiceImpl::getFoundTransactionsFailure);
    }

    /**
     * Get retrieved transactions that match the specified interval and check for an error.
     *
     * @param uniTransactionList
     *         value (/ transactions) will be present if the transactions are successfully loaded
     * @param throwable
     *         an exception will be present if an error occurred while getting transactions
     *
     * @return right with a list of loaded transactions that were created at the specified interval, otherwise left
     * (null)
     */
    private static Either<Void, List<UniTransaction>> handleFoundTransactions(List<UniTransaction> uniTransactionList, Throwable throwable) {
        if (uniTransactionList != null) {
            log.info("Transactions successfully loaded. {}", uniTransactionList);
            return Either.right(uniTransactionList);
        }
        log.error("An error occurred while trying to retrieve transactions that were created at the specified interval.", throwable);
        return Either.left(null);
    }

    private static Void getFoundTransactionsFailure(Throwable throwable) {
        String logMessage = "An error occurred while processing a promise to retrieve transactions corresponding to the specified interval.";
        log.error(logMessage, throwable);
        return null;
    }

    private static void setTransactionDependencyToRelatedObjects(Transaction transaction) {
        transaction.getValue().setTransaction(transaction);
        transaction.getPartyAccount().setTransaction(transaction);
        if (transaction.getAdditionalInfoDomestic() != null) {
            transaction.getAdditionalInfoDomestic().setTransaction(transaction);
        }
    }

    @Override
    public void copyTransactionsFromLastMonth(Long accountId) {
        log.info("Copying last month's transactions of user with accountId {}.", accountId);

        ZonedDateTime now = ZonedDateTime.now(clock);

        ZonedDateTime dateFrom = now
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .minusMonths(1)
                .withDayOfMonth(1);

        int daysInMonth = daySupport.getCountOfDaysInLastMonth(now);

        ZonedDateTime dateTo = now
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .minusMonths(1)
                .withDayOfMonth(daysInMonth);

        Stream.of(bankTransactionService.getByDateInterval(dateFrom, dateTo, accountId))
              .forEach(listMono -> handleFoundTransactions(listMono)
                      .map(this::convertToTransaction)
                      .peek(transactions -> transactions.forEach(CopyTransactionServiceImpl::setTransactionDependencyToRelatedObjects))
                      .forEach(transactionRepository::saveAll));
    }

    private List<Transaction> convertToTransaction(List<UniTransaction> transactionDtoOutList) {
        return transactionDtoOutList
                .parallelStream()
                .map(this::convertToTransaction)
                .collect(Collectors.toList());
    }

    private Transaction convertToTransaction(UniTransaction transactionByTimeInterval) {
        return modelMapper.map(transactionByTimeInterval, Transaction.class);
    }
}
