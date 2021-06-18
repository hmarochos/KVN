package cz.uhk.mois.financialplanning.service;

import cz.uhk.mois.financialplanning.bank.DaySupport;
import cz.uhk.mois.financialplanning.model.dto.transaction.AddTransactionDtoIn;
import cz.uhk.mois.financialplanning.model.dto.transaction.DailyTransactionsResult;
import cz.uhk.mois.financialplanning.model.dto.transaction.Direction;
import cz.uhk.mois.financialplanning.model.dto.transaction.GetByDateIntervalDtoIn;
import cz.uhk.mois.financialplanning.model.dto.transaction.GetByDateIntervalDtoOut;
import cz.uhk.mois.financialplanning.model.dto.transaction.LastMonthTransactionsOverviewDtoOut;
import cz.uhk.mois.financialplanning.model.dto.transaction.bank.AddBankTransactionDtoIn;
import cz.uhk.mois.financialplanning.model.dto.transaction.bank.BankTransactionDtoOut;
import cz.uhk.mois.financialplanning.model.dto.transaction.bank.PartyAccount;
import cz.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import cz.uhk.mois.financialplanning.model.entity.user.Role;
import cz.uhk.mois.financialplanning.model.entity.user.User;
import cz.uhk.mois.financialplanning.repository.TransactionRepository;
import cz.uhk.mois.financialplanning.response.Failure;
import cz.uhk.mois.financialplanning.response.FailureSupport;
import cz.uhk.mois.financialplanning.response.Success;
import cz.uhk.mois.financialplanning.service.bank.BankTransactionService;
import cz.uhk.mois.financialplanning.service.bank.UniTransaction;
import cz.uhk.mois.financialplanning.service.support.AuthSupport;
import cz.uhk.mois.financialplanning.service.support.UserSupport;
import cz.uhk.mois.financialplanning.validation.transaction.AddTransactionDtoInValidator;
import cz.uhk.mois.financialplanning.validation.transaction.GetByDateIntervalDtoInValidator;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cz.uhk.mois.financialplanning.service.support.AuthSupport.getSignedInUser;

/**
 * @author Jan Krunčík
 * @since 03.04.2020 1:48
 */

@Component
@Log4j2
public class TransactionServiceImpl implements TransactionService {

    private final UserSupport userSupport;
    private final BankTransactionService bankTransactionService;
    private final ModelMapper modelMapper;
    private final TransactionRepository transactionRepository;
    private final DaySupport daySupport;
    private final Clock clock;

    public TransactionServiceImpl(UserSupport userSupport, BankTransactionService bankTransactionService, ModelMapper modelMapper, TransactionRepository transactionRepository, DaySupport daySupport, Clock clock) {
        this.userSupport = userSupport;
        this.bankTransactionService = bankTransactionService;
        this.modelMapper = modelMapper;
        this.transactionRepository = transactionRepository;
        this.daySupport = daySupport;
        this.clock = clock;
    }

    /**
     * Check the created transaction for any errors when creating / saving etc.
     *
     * @param bankTransactionDtoOutMono
     *         "function (/ something like a promise)" to create and return the created transaction
     *
     * @return right with the data of the created transaction, otherwise left with information about the error
     */
    private static Either<Failure, BankTransactionDtoOut> handleCreatedTransaction(Mono<BankTransactionDtoOut> bankTransactionDtoOutMono) {
        Either<Failure, Either<Failure, BankTransactionDtoOut>> transaction = getCreatedTransaction(bankTransactionDtoOutMono);

        if (transaction.isLeft()) {
            return Either.left(transaction.getLeft());
        }

        // Check whether there was an error in creating the transaction or obtaining information about the created transaction
        Either<Failure, BankTransactionDtoOut> bankTransactionDtoOuts = transaction.get();
        if (bankTransactionDtoOuts.isLeft()) {
            return Either.left(bankTransactionDtoOuts.getLeft());
        }
        return Either.right(bankTransactionDtoOuts.get());
    }

    /**
     * Create a transaction (/ save it to the provided database for banking API simulation). <br/>
     * <i>The following source provides information for working with mono - data and blocking asynchronous
     * processing.</i> <br/>
     * <i>https://stackoverflow.com/questions/50000461/transforming-a-spring-webflux-mono-to-an-either-preferably-without-blocking</i>
     *
     * @param bankTransactionDtoOutMono
     *         "promise" to obtain the created transaction (their necessary data)
     *
     * @return right with the data of the created transaction (successfully stored in the database), otherwise left with
     * information about the error
     */
    private static Either<Failure, Either<Failure, BankTransactionDtoOut>> getCreatedTransaction(Mono<BankTransactionDtoOut> bankTransactionDtoOutMono) {
        return Try.of(() -> bankTransactionDtoOutMono.toFuture()
                                                     .handle(TransactionServiceImpl::handleCreatedTransaction)
                                                     .get())
                  .toEither()
                  .mapLeft(TransactionServiceImpl::getCreatedTransactionFailure);
    }

    /**
     * Obtaining the created transaction and checking for an error.
     *
     * @param dtoOut
     *         value will be present if the transaction is successfully created
     * @param throwable
     *         value will be present if an error occurs while processing transaction creation
     *
     * @return right with the created transaction (its necessary data), otherwise left with information about the error
     */
    private static Either<Failure, BankTransactionDtoOut> handleCreatedTransaction(BankTransactionDtoOut dtoOut, Throwable throwable) {
        if (dtoOut != null) {
            log.info("Transaction successfully created. {}", dtoOut);
            return Either.right(dtoOut);
        }
        log.error("An error occurred while trying to create a transaction (/ process it).", throwable);
        String message = "An error occurred while trying to create a transaction.";
        return Either.left(FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message));
    }

    private static Failure getCreatedTransactionFailure(Throwable throwable) {
        String logMessage = "There was an error processing the promise to retrieve the saved (/ created) transaction.";
        log.error(logMessage, throwable);
        String responseMessage = "An error occurred while processing the transaction.";
        return FailureSupport.createFailure(HttpStatus.BAD_REQUEST, responseMessage);
    }

    private static Either<Failure, User> checkAccountIdPresence(User user) {
        if (user.getAccountId() == null) {
            String logMessage = String.format("The signed-in user has not filled in accountId, it is not possible to create a transaction. %s", user);
            log.error(logMessage);
            String responseMessage = "You do not have an account id filled out, you cannot create a transaction.";
            return Either.left(FailureSupport.createFailure(HttpStatus.BAD_REQUEST, responseMessage));
        }
        return Either.right(user);
    }

    private static Success<BankTransactionDtoOut> addSuccessDtoOut(BankTransactionDtoOut dtoOut) {
        log.info("Transaction successfully created. {}", dtoOut);
        return Success.<BankTransactionDtoOut>builder()
                .httpStatus(HttpStatus.CREATED)
                .body(dtoOut)
                .build();
    }

    private static Success<GetByDateIntervalDtoOut> getByDateIntervalSuccessDtoOut(GetByDateIntervalDtoOut dtoOut) {
        log.info("Transactions successfully loaded. {}", dtoOut);
        return Success.<GetByDateIntervalDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    private static Failure getFoundTransactionsFailure(Throwable throwable) {
        String logMessage = "An error occurred while processing a promise to retrieve transactions corresponding to the specified interval.";
        log.error(logMessage, throwable);
        String responseMessage = "An error occurred while processing transactions corresponding to the specified interval.";
        return FailureSupport.createFailure(HttpStatus.BAD_REQUEST, responseMessage);
    }

    private static AddBankTransactionDtoIn createAddBankTransactionDtoIn(AddTransactionDtoIn dtoIn) {
        return AddBankTransactionDtoIn.builder()
                                      .accountId(dtoIn.getAccountId())
                                      .value(dtoIn.getValue())
                                      .partyAccount(new PartyAccount(dtoIn.getAccountId().toString(), dtoIn.getBankCode().toString()))
                                      .partyDescription(dtoIn.getPartyDescription())
                                      .direction(dtoIn.getDirection())
                                      .transactionType(dtoIn.getTransactionType())
                                      .valueDate(dtoIn.getPaymentDate().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT))
                                      .bookingDate(dtoIn.getPaymentDate().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT))
                                      .additionalInfoDomestic(dtoIn.getAdditionalInfoDomestic())
                                      .build();
    }

    /**
     * Divide transactions into days on which they were made. <br/>
     * <i>Transactions will be broken down into day indices (1- [28 - 31]). On each day there will be a list of
     * transactions that were executed on that day.</i> <br/>
     * <i>The key on the map will be the index of the day, the value will be a list of transactions made on that
     * day.</i>
     *
     * @param transactionList
     *         list of transactions to be divided into the above map
     *
     * @return a list of transactions broken down by the days on which they were executed
     */
    private static Map<Integer, List<Transaction>> groupTransactionsByDayIndex(List<Transaction> transactionList) {
        return transactionList.stream()
                              .collect(Collectors.groupingBy(transaction -> transaction.getValueDate().getDayOfMonth()));
    }

    /**
     * Calculate transactions for graphs to display on the GUI. <br/>
     * <i>It is a sequential sum of all transactions, ie each subsequent value (the following day) will contain the sum
     * of all previous transactions (for all previous days of the month) + the current one.</i> <br/
     * <i>The values ​​need to be "recalculated" in order to display them appropriately in the FE graph.</i>
     *
     * @param transactionValuesForEachDay
     *         Map of transaction values ​​distributed for each day of the month
     *
     * @return map where the key will be the index of the day of the month and the value will be the sum of transactions
     * for previous days + the current one
     */
    private static Map<Integer, BigDecimal> computeTransactionForGraph(Map<Integer, BigDecimal> transactionValuesForEachDay) {
        // Alg
        // day 1 = 0 + sum of first day transactions
        // day 2 = sum of first day transactions + sum of second day transactions
        // day 3 = sum of second day transactions + sum os third day transactions
        // day 4 = day 3 + day 4
        // ...

        BigDecimal zero = BigDecimal.ZERO;

        for (Map.Entry<Integer, BigDecimal> integerBigDecimalEntry : transactionValuesForEachDay.entrySet()) {
            zero = zero.add(integerBigDecimalEntry.getValue());
            transactionValuesForEachDay.put(integerBigDecimalEntry.getKey(), zero);
        }

        return transactionValuesForEachDay;
    }

    /**
     * Convert calculated transactions for each day to the dtoOut object list. <br/>
     * <i>This is only needed to allow the values ​​to be easily accessed by the FE (in optimal form).</i>
     *
     * @param computedTransactionsForEachDay
     *         map of the sum of transactions for each day
     *
     * @return a list of transactions for each day in a form suitable for working with it on the FE.
     */
    private static List<DailyTransactionsResult> convertToDailyTransactionsResultList(Map<Integer, BigDecimal> computedTransactionsForEachDay) {
        return computedTransactionsForEachDay.entrySet().stream()
                                             .map(integerBigDecimalEntry -> DailyTransactionsResult.builder()
                                                                                                   .dayIndex(integerBigDecimalEntry.getKey())
                                                                                                   .result(integerBigDecimalEntry.getValue())
                                                                                                   .build())
                                             .collect(Collectors.toList());
    }

    private static LastMonthTransactionsOverviewDtoOut createLastMonthTransactionsOverviewDtoOut(List<DailyTransactionsResult> dailyTransactionsResultList) {
        LastMonthTransactionsOverviewDtoOut dtoOut = new LastMonthTransactionsOverviewDtoOut();
        dtoOut.setDailyTransactionsResultList(dailyTransactionsResultList);
        return dtoOut;
    }

    private static Success<LastMonthTransactionsOverviewDtoOut> getLastMonthTransactionsOverviewSuccess(LastMonthTransactionsOverviewDtoOut dtoOut) {
        log.info("Transaction report for last month successfully created. {}", dtoOut);
        return Success.<LastMonthTransactionsOverviewDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    private static Failure findTransactionsByAccountIdFailure(Throwable throwable, Long accountId) {
        String logMessage = String.format("There was an error trying to retrieve a list of user-related transactions with account number %s.", accountId);
        log.error(logMessage, throwable);
        String responseMessage = "There was an error trying to retrieve a list of user-related transactions.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, responseMessage);
    }

    /**
     * Check if signed-in user is admin (has ADMIN role).
     *
     * @param user
     *         signed-in user, who can find out if it is admin (has the role of admin)
     *
     * @return right with the user in the method parameter if it has the admin role, otherwise left with the information
     * that the user is not admin (does not have sufficient permissions to perform the necessary operation).
     */
    private static Either<Failure, User> checkUserIsAdmin(User user) {
        if (user.getRoles() == null || !user.getRoles().contains(Role.ADMIN)) {
            String logMessage = String.format("The signed-in user does not have admin privileges. %s", user);
            log.error(logMessage);
            String responseMessage = "You do not have sufficient permissions.";
            return Either.left(FailureSupport.createFailure(HttpStatus.UNAUTHORIZED, responseMessage));
        }
        return Either.right(user);
    }

    /**
     * Check for retrieved transactions that were created at the specified interval for an error when attempting to
     * retrieve them.
     *
     * @param uniTransactionListMono
     *         "function (/ something like a promise)" to retrieve and return transactions that were created at the
     *         specified interval
     *
     * @return right list of loaded transactions, otherwise left with information about the error
     */
    private Either<Failure, GetByDateIntervalDtoOut> handleFoundTransactions(Mono<List<UniTransaction>> uniTransactionListMono) {
        Either<Failure, Either<Failure, GetByDateIntervalDtoOut>> foundTransactions = getFoundTransactions(uniTransactionListMono);

        if (foundTransactions.isLeft()) {
            return Either.left(foundTransactions.getLeft());
        }

        // Check whether there was an error in retrieving the transactions
        Either<Failure, GetByDateIntervalDtoOut> getByDateIntervalDtoOuts = foundTransactions.get();
        if (getByDateIntervalDtoOuts.isLeft()) {
            return Either.left(getByDateIntervalDtoOuts.getLeft());
        }
        return Either.right(getByDateIntervalDtoOuts.get());
    }

    private Either<Failure, Either<Failure, GetByDateIntervalDtoOut>> getFoundTransactions(Mono<List<UniTransaction>> uniTransactionListMono) {
        return Try.of(() -> uniTransactionListMono.toFuture()
                                                  .handle(this::handleFoundTransactions)
                                                  .get())
                  .toEither()
                  .mapLeft(TransactionServiceImpl::getFoundTransactionsFailure);
    }

    /**
     * Get retrieved transactions that match the specified interval and check for an error.
     *
     * @param uniTransactionList
     *         value (/ transactions) will be present if the transactions are successfully loaded
     * @param throwable
     *         an exception will be present if an error occurred while getting transactions
     *
     * @return right with a list of loaded transactions that were created at the specified interval, otherwise left with
     * information about the error
     */
    private Either<Failure, GetByDateIntervalDtoOut> handleFoundTransactions(List<UniTransaction> uniTransactionList, Throwable throwable) {
        if (uniTransactionList != null) {
            log.info("Transactions successfully loaded. {}", uniTransactionList);
            GetByDateIntervalDtoOut dtoOut = new GetByDateIntervalDtoOut();
            dtoOut.setBankTransactionDtoOutList(convertToDto(uniTransactionList));
            return Either.right(dtoOut);
        }
        log.error("An error occurred while trying to retrieve transactions that were created at the specified interval.", throwable);
        String message = "An error occurred while trying to retrieve transactions.";
        return Either.left(FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message));
    }

    private List<BankTransactionDtoOut> convertToDto(List<UniTransaction> uniTransactionList) {
        Type listType = new TypeToken<List<BankTransactionDtoOut>>() {
        }.getType();
        return modelMapper.map(uniTransactionList, listType);
    }

    @Override
    public Either<Failure, Success<BankTransactionDtoOut>> add(AddTransactionDtoIn dtoIn) {
        log.info("Add / create transaction. {}", dtoIn);

        return AddTransactionDtoInValidator.validate(dtoIn)
                                           .mapError(validationViolations -> FailureSupport.validationFailure(validationViolations, "AddTransactionDtoIn"))
                                           .toEither()
                                           .flatMap(validatedDtoIn -> getSignedInUser()
                                                   .flatMap(AuthSupport::checkSignedInUser)
                                                   .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                                   .flatMap(userSupport::findUserById)
                                                   .flatMap(TransactionServiceImpl::checkUserIsAdmin)
                                                   .map(user -> createAddBankTransactionDtoIn(validatedDtoIn)))
                                           .map(bankTransactionService::add)
                                           .flatMap(TransactionServiceImpl::handleCreatedTransaction)
                                           .map(TransactionServiceImpl::addSuccessDtoOut);
    }

    @Override
    public Either<Failure, Success<GetByDateIntervalDtoOut>> getByDateInterval(GetByDateIntervalDtoIn dtoIn) {
        log.info("Obtaining (/ filtering) transactions according to the specified interval. {}", dtoIn);

        return GetByDateIntervalDtoInValidator.validate(dtoIn)
                                              .mapError(validationViolations -> FailureSupport.validationFailure(validationViolations, "GetByDateIntervalDtoIn"))
                                              .toEither()
                                              .flatMap(validatedDtoIn -> getSignedInUser()
                                                      .flatMap(AuthSupport::checkSignedInUser)
                                                      .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                                      .flatMap(userSupport::findUserById)
                                                      .flatMap(TransactionServiceImpl::checkAccountIdPresence)
                                                      .map(user -> bankTransactionService.getByDateInterval(validatedDtoIn.getDateFrom(), validatedDtoIn.getDateTo(), user.getAccountId())))
                                              .flatMap(this::handleFoundTransactions)
                                              .map(TransactionServiceImpl::getByDateIntervalSuccessDtoOut);
    }

    @Override
    public Either<Failure, Success<LastMonthTransactionsOverviewDtoOut>> getLastMonthTransactionsOverview() {
        log.info("Get an overview of transactions made last month.");

        return getSignedInUser()
                .flatMap(AuthSupport::checkSignedInUser)
                .flatMap(AuthSupport::getIdFromSignedInUserToken)
                .flatMap(userSupport::findUserById)
                .flatMap(TransactionServiceImpl::checkAccountIdPresence)
                .flatMap(user -> findTransactionsByAccountId(user.getAccountId()))
                .map(TransactionServiceImpl::groupTransactionsByDayIndex)
                .map(this::computeTransactionForEachDay)
                .map(TransactionServiceImpl::computeTransactionForGraph)
                .map(TransactionServiceImpl::convertToDailyTransactionsResultList)
                .map(TransactionServiceImpl::createLastMonthTransactionsOverviewDtoOut)
                .map(TransactionServiceImpl::getLastMonthTransactionsOverviewSuccess);
    }

    @Override
    public Either<Failure, List<Transaction>> findTransactionsByAccountId(Long accountId) {
        return Try.of(() -> transactionRepository.findAllByAccountId(accountId))
                  .toEither()
                  .mapLeft(throwable -> findTransactionsByAccountIdFailure(throwable, accountId));
    }

    /**
     * Calculation (sum) of transactions for each day of the last month. <br/>
     * <i>This is to determine the final account balance for a particular day for the last month.</i>
     * <br/>
     * <i>There will be zero at the beginning, if it is an incoming payment, the amount will be added to zero,
     * otherwise it will deduct.</i>
     *
     * @param transactionsPerDay
     *         list of transactions broken down for each day
     *
     * @return a map where the key will be an index of the day of the month and the value will be the specific amount
     * (the sum of transactions for that day) that the user "worked" on that day
     */
    private Map<Integer, BigDecimal> computeTransactionForEachDay(Map<Integer, List<Transaction>> transactionsPerDay) {
        int daysInLastMonth = daySupport.getCountOfDaysInLastMonth(ZonedDateTime.now(clock));

        Map<Integer, BigDecimal> transactionsSumForEachDayInMonth = new HashMap<>();

        IntStream.range(0, daysInLastMonth)
                 .forEach(i -> {
                     int dayIndex = i + 1;
                     List<Transaction> transactions = transactionsPerDay.get(dayIndex);
                     if (transactions == null || transactions.isEmpty()) {
                         // No transaction was made on this day
                         transactionsSumForEachDayInMonth.put(dayIndex, BigDecimal.ZERO);
                         return;
                     }

                     // At least one transaction was executed, so they will be calculated with previous
                     BigDecimal sum = BigDecimal.ZERO;
                     for (Transaction transaction : transactions) {
                         if (transaction.getDirection() == Direction.INCOMING) {
                             sum = sum.add(transaction.getValue().getAmount());
                         } else if (transaction.getDirection() == Direction.OUTGOING) {
                             sum = sum.subtract(transaction.getValue().getAmount());
                         }
                     }

                     transactionsSumForEachDayInMonth.put(dayIndex, sum);
                 });

        return transactionsSumForEachDayInMonth;
    }
}
