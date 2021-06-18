package ua.uhk.mois.financialplanning.service;

import ua.uhk.mois.financialplanning.bank.DaySupport;
import ua.uhk.mois.financialplanning.configuration.AbsTestConfiguration;
import ua.uhk.mois.financialplanning.model.dto.transaction.AddTransactionDtoIn;
import ua.uhk.mois.financialplanning.model.dto.transaction.DailyTransactionsResult;
import ua.uhk.mois.financialplanning.model.dto.transaction.Direction;
import ua.uhk.mois.financialplanning.model.dto.transaction.GetByDateIntervalDtoIn;
import ua.uhk.mois.financialplanning.model.dto.transaction.GetByDateIntervalDtoOut;
import ua.uhk.mois.financialplanning.model.dto.transaction.LastMonthTransactionsOverviewDtoOut;
import ua.uhk.mois.financialplanning.model.dto.transaction.Value;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.BankTransactionDtoOut;
import ua.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import ua.uhk.mois.financialplanning.model.entity.user.Role;
import ua.uhk.mois.financialplanning.model.entity.user.User;
import ua.uhk.mois.financialplanning.response.Failure;
import ua.uhk.mois.financialplanning.response.Success;
import ua.uhk.mois.financialplanning.service.bank.BankTransactionService;
import ua.uhk.mois.financialplanning.service.support.UserSupport;
import io.vavr.control.Either;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static ua.uhk.mois.financialplanning.configuration.TransactionSupport.createAddTransactionDtoIn;
import static ua.uhk.mois.financialplanning.configuration.TransactionSupport.createTransactionList;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.ACCOUNT_ID;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.createUser;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.signInUser;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.signOutUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class TransactionServiceImplTest extends AbsTestConfiguration {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private Clock clock;

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private BankTransactionService bankTransactionService;

    @Autowired
    private ModelMapper modelMapper;

    @Mock
    private DaySupport daySupport;

    private static void assertDailyTransactionResult(Integer dayIndex, Long result, DailyTransactionsResult dailyTransactionsResult) {
        assertEquals(dayIndex, dailyTransactionsResult.getDayIndex());
        assertEquals(0, BigDecimal.valueOf(result).compareTo(dailyTransactionsResult.getResult()));
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
    void add_InvalidDtoIn() {
        log.info("Add / create transaction test. The input data will contain invalid values.");

        // Data preparation
        assertEmptyDatabase();

        AddTransactionDtoIn dtoIn = createAddTransactionDtoIn();
        dtoIn.setAccountId(null);
        dtoIn.setValue(new Value(BigDecimal.valueOf(-1000L), null));
        dtoIn.setBankCode(-123L);
        dtoIn.setPartyDescription("    ![]{}[]<> §");
        dtoIn.setDirection(null);
        dtoIn.setTransactionType(null);
        dtoIn.setPaymentDate(null);
        dtoIn.getAdditionalInfoDomestic().setConstantSymbol(null);
        dtoIn.getAdditionalInfoDomestic().setVariableSymbol("    ");
        dtoIn.getAdditionalInfoDomestic().setSpecificSymbol("abc_!-");

        // Execution
        Either<Failure, Success<BankTransactionDtoOut>> add = transactionService.add(dtoIn);

        // Verification
        assertTrue(add.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, add.getLeft().getHttpStatus());

        assertEmptyDatabase();

        String expectedMessage = "Account number is not specified., Amount must be a positive number., Bank code must be a positive number., The party description contains illegal characters: '<>[]{}§'., Transaction direction not specified., Transaction type not specified., Payment date not specified., The specific symbol can only be 1 - 10 numbers.";
        assertEquals(expectedMessage, add.getLeft().getMessage());
    }

    @Test
    void add_UserNotSignedIn() {
        log.info("Add / create transaction test. User will not be signed-in. Therefore, user will not be able to obtain account id information or perform the appropriate operation.");

        // Data preparation
        assertEmptyDatabase();

        AddTransactionDtoIn dtoIn = createAddTransactionDtoIn();

        // Execution
        Either<Failure, Success<BankTransactionDtoOut>> add = transactionService.add(dtoIn);

        // Verification
        assertTrue(add.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, add.getLeft().getHttpStatus());

        assertEmptyDatabase();

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, add.getLeft().getMessage());
    }

    @Test
    void add_UserNotFoundById() {
        log.info("Add / create transaction test. The user will not be found in the database according to the id obtained from the Spring context (token) to verify its identity.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setEmail(user.getEmail());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        AddTransactionDtoIn dtoIn = createAddTransactionDtoIn();

        // Execution
        Either<Failure, Success<BankTransactionDtoOut>> add = transactionService.add(dtoIn);

        // Verification
        assertTrue(add.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, add.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, add.getLeft().getMessage());
    }

    @Test
    @Transactional
    void add_WrongUrlAddress() {
        log.info("Add / create transaction test. An unknown address will be specified. This is a simulation of data processing error - saving (/ creating) transactions.");

        // Data preparation
        User user = createUser();
        user.setRoles(Collections.singletonList(Role.ADMIN));
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        AddTransactionDtoIn dtoIn = createAddTransactionDtoIn();

        // Execution
        Either<Failure, Success<BankTransactionDtoOut>> add = transactionService.add(dtoIn);

        // Verification
        assertTrue(add.isLeft());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, add.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "An error occurred while trying to create a transaction.";
        assertEquals(expectedMessage, add.getLeft().getMessage());
    }

    @Test
    @Transactional
    void add_SignedInUserIsNotAdmin() {
        log.info("Add / create transaction test. An unknown address will be specified to send the request to create / add transaction, but the process will not reach it. The signed-in user does not have sufficient permissions. However, this should not happen in real life because no user with sufficient privileges could call this method.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        AddTransactionDtoIn dtoIn = createAddTransactionDtoIn();

        // Execution
        Either<Failure, Success<BankTransactionDtoOut>> add = transactionService.add(dtoIn);

        // Verification
        assertTrue(add.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, add.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "You do not have sufficient permissions.";
        assertEquals(expectedMessage, add.getLeft().getMessage());
    }

    @Test
    void getByDateInterval_InvalidDtoIn() {
        log.info("Test of getting transactions according to the account number of the signed-in user and at the specified interval. Input data will not be entered in a valid format.");

        // Data preparation
        assertEmptyDatabase();

        GetByDateIntervalDtoIn dtoIn = new GetByDateIntervalDtoIn();

        // Execution
        Either<Failure, Success<GetByDateIntervalDtoOut>> byDateInterval = transactionService.getByDateInterval(dtoIn);

        // Verification
        assertTrue(byDateInterval.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, byDateInterval.getLeft().getHttpStatus());

        assertEmptyDatabase();

        String expectedMessage = "DateFrom (beginning of interval) is not specified., DateTo (end of interval) is not specified.";
        assertEquals(expectedMessage, byDateInterval.getLeft().getMessage());
    }

    @Test
    void getByDateInterval_UserNotSignedIn() {
        log.info("Test of getting transactions according to the account number of the signed-in user and at the specified interval. The user will not be signed-in, so application would not get the necessary data for further processing.");

        // Data preparation
        assertEmptyDatabase();

        GetByDateIntervalDtoIn dtoIn = createGetByDateIntervalDtoIn();

        // Execution
        Either<Failure, Success<GetByDateIntervalDtoOut>> byDateInterval = transactionService.getByDateInterval(dtoIn);

        // Verification
        assertTrue(byDateInterval.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, byDateInterval.getLeft().getHttpStatus());

        assertEmptyDatabase();

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, byDateInterval.getLeft().getMessage());
    }

    @Test
    void getByDateInterval_UserNotFoundById() {
        log.info("Test of getting transactions according to the account number of the signed-in user and at the specified interval. The user will not be found according to the id obtained from the token (Spring context). In practice it should not occur.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setPasswordHash(user.getPasswordHash());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        GetByDateIntervalDtoIn dtoIn = createGetByDateIntervalDtoIn();

        // Execution
        Either<Failure, Success<GetByDateIntervalDtoOut>> byDateInterval = transactionService.getByDateInterval(dtoIn);

        // Verification
        assertTrue(byDateInterval.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, byDateInterval.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, byDateInterval.getLeft().getMessage());
    }

    @Test
    @Transactional
    void getByDateInterval_AccountIdNotSpecified() {
        log.info("Test of getting transactions according to the account number of the signed-in user and at the specified interval. The user will not have a completed account number, so its transactions cannot be obtained.");

        // Data preparation
        User user = createUser();
        user.setAccountId(null);
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        GetByDateIntervalDtoIn dtoIn = createGetByDateIntervalDtoIn();

        // Execution
        Either<Failure, Success<GetByDateIntervalDtoOut>> byDateInterval = transactionService.getByDateInterval(dtoIn);

        // Verification
        assertTrue(byDateInterval.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, byDateInterval.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "You do not have an account id filled out, you cannot create a transaction.";
        assertEquals(expectedMessage, byDateInterval.getLeft().getMessage());
    }

    @Test
    @Transactional
    void getByDateInterval_WrongUrlAddress() {
        log.info("Test of getting transactions according to the account number of the signed-in user and at the specified interval. Banking API call simulation test - missing address.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Sign-in user
        signInUser(user);

        GetByDateIntervalDtoIn dtoIn = createGetByDateIntervalDtoIn();

        // Execution
        Either<Failure, Success<GetByDateIntervalDtoOut>> byDateInterval = transactionService.getByDateInterval(dtoIn);

        // Verification
        assertTrue(byDateInterval.isLeft());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, byDateInterval.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "An error occurred while trying to retrieve transactions.";
        assertEquals(expectedMessage, byDateInterval.getLeft().getMessage());
    }

    @Test
    void getLastMonthTransactionsOverview_Hds() {
        log.info("Test of getting a monthly transaction report for the FE chart. The values for March will be entered. The first 5 days will always be 2x 500 uaK income and 1x 500 expenses. The next 24 days will be 1x 1000 incomes. The penultimate day will be only 1x 500 uaK. The last day will be without income.");

        // Mock preparation
        Mockito.when(daySupport.getCountOfDaysInLastMonth(ArgumentMatchers.any(ZonedDateTime.class)))
               .thenReturn(31);

        // Data preparation

        // 30 x 500 income
        List<Transaction> transactionList = createTransactionList(1, 31, ACCOUNT_ID, Direction.INCOMING);
        // 29 x 500 income (Make multiple transactions for one day)
        transactionList.addAll(createTransactionList(1, 30, ACCOUNT_ID, Direction.INCOMING));
        // 5 x 500 expenditure
        transactionList.addAll(createTransactionList(1, 6, ACCOUNT_ID, Direction.OUTGOING));
        transactionRepository.saveAll(transactionList);

        User user = createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3, 64);

        // Sign-in user
        signInUser(user);

        TransactionServiceImpl transactionServiceWithMockDaySupport = new TransactionServiceImpl(userSupport, bankTransactionService, modelMapper, transactionRepository, daySupport, clock);

        // Execution
        Either<Failure, Success<LastMonthTransactionsOverviewDtoOut>> lastMonthTransactionsOverview = transactionServiceWithMockDaySupport.getLastMonthTransactionsOverview();

        // Verification
        assertTrue(lastMonthTransactionsOverview.isRight());
        assertEquals(HttpStatus.OK, lastMonthTransactionsOverview.get().getHttpStatus());

        assertDatabaseSize(1, 3, 64);

        LastMonthTransactionsOverviewDtoOut dtoOut = lastMonthTransactionsOverview.get().getBody();
        assertNotNull(dtoOut);

        List<DailyTransactionsResult> dailyTransactionsResultList = dtoOut.getDailyTransactionsResultList();
        assertNotNull(dailyTransactionsResultList);
        assertThat(dailyTransactionsResultList, hasSize(31));
        // 500 + 500 - 500 = 500
        assertDailyTransactionResult(1, 500L, dailyTransactionsResultList.get(0));
        // 500 (above) + 500 + 500 - 500 = 1000
        assertDailyTransactionResult(2, 1000L, dailyTransactionsResultList.get(1));
        // 1000 (above) + 500 + 500 - 500 = 1500
        assertDailyTransactionResult(3, 1500L, dailyTransactionsResultList.get(2));
        // 15000 (above) + 500 + 500 - 500 = 2000
        assertDailyTransactionResult(4, 2000L, dailyTransactionsResultList.get(3));
        // 2000 (above) + 500 + 500 - 500 = 2500
        assertDailyTransactionResult(5, 2500L, dailyTransactionsResultList.get(4));
        // 2500 (above) + 500 + 500 = 3500
        assertDailyTransactionResult(6, 3500L, dailyTransactionsResultList.get(5));
        // 3500 (above) + 500 + 500 = 4500
        assertDailyTransactionResult(7, 4500L, dailyTransactionsResultList.get(6));
        // 4500 (above) + 500 + 500 = 5500
        assertDailyTransactionResult(8, 5500L, dailyTransactionsResultList.get(7));
        // 5500 (above) + 500 + 500 = 6500
        assertDailyTransactionResult(9, 6500L, dailyTransactionsResultList.get(8));
        // 6500 (above) + 500 + 500 = 7500
        assertDailyTransactionResult(10, 7500L, dailyTransactionsResultList.get(9));
        // 7500 (above) + 500 + 500 = 8500
        assertDailyTransactionResult(11, 8500L, dailyTransactionsResultList.get(10));
        // 8500 (above) + 500 + 500 = 9500
        assertDailyTransactionResult(12, 9500L, dailyTransactionsResultList.get(11));
        // 9500 (above) + 500 + 500 = 10500
        assertDailyTransactionResult(13, 10500L, dailyTransactionsResultList.get(12));
        // 10500 (above) + 500 + 500 = 11500
        assertDailyTransactionResult(14, 11500L, dailyTransactionsResultList.get(13));
        // 11500 (above) + 500 + 500 = 12500
        assertDailyTransactionResult(15, 12500L, dailyTransactionsResultList.get(14));
        // 12500 (above) + 500 + 500 = 13500
        assertDailyTransactionResult(16, 13500L, dailyTransactionsResultList.get(15));
        // 13500 (above) + 500 + 500 = 14500
        assertDailyTransactionResult(17, 14500L, dailyTransactionsResultList.get(16));
        // 14500 (above) + 500 + 500 = 15500
        assertDailyTransactionResult(18, 15500L, dailyTransactionsResultList.get(17));
        // 15500 (above) + 500 + 500 = 16500
        assertDailyTransactionResult(19, 16500L, dailyTransactionsResultList.get(18));
        // 16500 (above) + 500 + 500 = 17500
        assertDailyTransactionResult(20, 17500L, dailyTransactionsResultList.get(19));
        // 17500 (above) + 500 + 500 = 18500
        assertDailyTransactionResult(21, 18500L, dailyTransactionsResultList.get(20));
        // 18500 (above) + 500 + 500 = 19500
        assertDailyTransactionResult(22, 19500L, dailyTransactionsResultList.get(21));
        // 19500 (above) + 500 + 500 = 20500
        assertDailyTransactionResult(23, 20500L, dailyTransactionsResultList.get(22));
        // 20500 (above) + 500 + 500 = 21500
        assertDailyTransactionResult(24, 21500L, dailyTransactionsResultList.get(23));
        // 21500 (above) + 500 + 500 = 22500
        assertDailyTransactionResult(25, 22500L, dailyTransactionsResultList.get(24));
        // 22500 (above) + 500 + 500 = 23500
        assertDailyTransactionResult(26, 23500L, dailyTransactionsResultList.get(25));
        // 23500 (above) + 500 + 500 = 24500
        assertDailyTransactionResult(27, 24500L, dailyTransactionsResultList.get(26));
        // 24500 (above) + 500 + 500 = 25500
        assertDailyTransactionResult(28, 25500L, dailyTransactionsResultList.get(27));
        // 25500 (above) + 500 + 500 = 26500
        assertDailyTransactionResult(29, 26500L, dailyTransactionsResultList.get(28));
        // 26500 (above) + 500 = 27000
        assertDailyTransactionResult(30, 27000L, dailyTransactionsResultList.get(29));
        // 27000 (above) + 0 = 27000
        assertDailyTransactionResult(31, 27000L, dailyTransactionsResultList.get(30));
    }

    @Test
    void getLastMonthTransactionsOverview_FewTransactions_Hds() {
        log.info("Test of getting a monthly transaction report for the FE chart. The values for March will be entered. Only some mid-month transactions will be entered. Furthermore, there will be two users in the database to test whether only one (correct) user will be retrieved.");

        // Mock preparation
        Mockito.when(daySupport.getCountOfDaysInLastMonth(ArgumentMatchers.any(ZonedDateTime.class)))
               .thenReturn(31);

        // Data preparation

        // 7 x 500 income (10. - 16. March)
        List<Transaction> transactionList = createTransactionList(10, 17, ACCOUNT_ID, Direction.INCOMING);
        // 6 x 500 expenditure (5. - 10. March)
        transactionList.addAll(createTransactionList(5, 11, ACCOUNT_ID, Direction.OUTGOING));
        // 1 x 500 expenditure (14. March)
        transactionList.addAll(createTransactionList(14, 15, ACCOUNT_ID, Direction.OUTGOING));
        transactionRepository.saveAll(transactionList);

        // The user whose transactions will be tested
        User user1 = createUser(true);
        userRepository.save(user1);

        // A user who is "extra", his data will not be used in this test
        Long accountId = 1L;
        User user2 = createUser(true);
        user2.setEmail(user2.getEmail() + "m");
        user2.setAccountId(accountId);
        userRepository.save(user2);

        List<Transaction> transactionList2 = createTransactionList(1, 16, accountId, Direction.INCOMING);
        transactionRepository.saveAll(transactionList2);

        assertDatabaseSize(2, 6, 29);

        // Sign-in user
        signInUser(user1);

        TransactionServiceImpl transactionServiceWithMockDaySupport = new TransactionServiceImpl(userSupport, bankTransactionService, modelMapper, transactionRepository, daySupport, clock);

        // Execution
        Either<Failure, Success<LastMonthTransactionsOverviewDtoOut>> lastMonthTransactionsOverview = transactionServiceWithMockDaySupport.getLastMonthTransactionsOverview();

        // Verification
        assertTrue(lastMonthTransactionsOverview.isRight());
        assertEquals(HttpStatus.OK, lastMonthTransactionsOverview.get().getHttpStatus());

        assertDatabaseSize(2, 6, 29);

        LastMonthTransactionsOverviewDtoOut dtoOut = lastMonthTransactionsOverview.get().getBody();
        assertNotNull(dtoOut);

        List<DailyTransactionsResult> dailyTransactionsResultList = dtoOut.getDailyTransactionsResultList();
        assertNotNull(dailyTransactionsResultList);
        assertThat(dailyTransactionsResultList, hasSize(31));
        // 0
        assertDailyTransactionResult(1, 0L, dailyTransactionsResultList.get(0));
        // 0
        assertDailyTransactionResult(2, 0L, dailyTransactionsResultList.get(1));
        // 0
        assertDailyTransactionResult(3, 0L, dailyTransactionsResultList.get(2));
        // 0
        assertDailyTransactionResult(4, 0L, dailyTransactionsResultList.get(3));
        // 0 (above) - 500 = -500
        assertDailyTransactionResult(5, -500L, dailyTransactionsResultList.get(4));
        // -500 (above) - 500 = -1000
        assertDailyTransactionResult(6, -1000L, dailyTransactionsResultList.get(5));
        // -1000 (above) - 500 = -1500
        assertDailyTransactionResult(7, -1500L, dailyTransactionsResultList.get(6));
        // -1500 (above) - 500 = -2000
        assertDailyTransactionResult(8, -2000L, dailyTransactionsResultList.get(7));
        // -2000 (above) - 500 = -2500
        assertDailyTransactionResult(9, -2500L, dailyTransactionsResultList.get(8));
        // -2500 (above) + 500 - 500 = -2500
        assertDailyTransactionResult(10, -2500L, dailyTransactionsResultList.get(9));
        // -2500 (above) + 500 = -2000
        assertDailyTransactionResult(11, -2000L, dailyTransactionsResultList.get(10));
        // -2000 (above) + 500 = -1500
        assertDailyTransactionResult(12, -1500L, dailyTransactionsResultList.get(11));
        // -1500 (above) + 500 = -1000
        assertDailyTransactionResult(13, -1000L, dailyTransactionsResultList.get(12));
        // -1000 (above) + 500 - 500 = -1000
        assertDailyTransactionResult(14, -1000L, dailyTransactionsResultList.get(13));
        // -1000 (above) + 500 = -500
        assertDailyTransactionResult(15, -500L, dailyTransactionsResultList.get(14));
        // -500 (above) + 500 = 0
        assertDailyTransactionResult(16, 0L, dailyTransactionsResultList.get(15));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(17, 0L, dailyTransactionsResultList.get(16));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(18, 0L, dailyTransactionsResultList.get(17));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(19, 0L, dailyTransactionsResultList.get(18));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(20, 0L, dailyTransactionsResultList.get(19));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(21, 0L, dailyTransactionsResultList.get(20));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(22, 0L, dailyTransactionsResultList.get(21));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(23, 0L, dailyTransactionsResultList.get(22));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(24, 0L, dailyTransactionsResultList.get(23));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(25, 0L, dailyTransactionsResultList.get(24));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(26, 0L, dailyTransactionsResultList.get(25));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(27, 0L, dailyTransactionsResultList.get(26));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(28, 0L, dailyTransactionsResultList.get(27));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(29, 0L, dailyTransactionsResultList.get(28));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(30, 0L, dailyTransactionsResultList.get(29));
        // 0 (above) + 0 = 0
        assertDailyTransactionResult(31, 0L, dailyTransactionsResultList.get(30));
    }

    @Test
    void getLastMonthTransactionsOverview_NoTransactions() {
        log.info("Test of getting a monthly transaction report for the FE chart. The user will not have any transactions specified.");

        // Mock preparation
        Mockito.when(daySupport.getCountOfDaysInLastMonth(ArgumentMatchers.any(ZonedDateTime.class)))
               .thenReturn(31);

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        TransactionServiceImpl transactionServiceWithMockDaySupport = new TransactionServiceImpl(userSupport, bankTransactionService, modelMapper, transactionRepository, daySupport, clock);

        // Execution
        Either<Failure, Success<LastMonthTransactionsOverviewDtoOut>> lastMonthTransactionsOverview = transactionServiceWithMockDaySupport.getLastMonthTransactionsOverview();

        // Verification
        assertTrue(lastMonthTransactionsOverview.isRight());
        assertEquals(HttpStatus.OK, lastMonthTransactionsOverview.get().getHttpStatus());

        assertDatabaseSize(1, 3);

        LastMonthTransactionsOverviewDtoOut dtoOut = lastMonthTransactionsOverview.get().getBody();
        assertNotNull(dtoOut);

        List<DailyTransactionsResult> dailyTransactionsResultList = dtoOut.getDailyTransactionsResultList();
        assertNotNull(dailyTransactionsResultList);
        assertThat(dailyTransactionsResultList, hasSize(31));

        IntStream.range(1, 32)
                 .forEach(i -> {
                     DailyTransactionsResult dailyTransactionsResult = dailyTransactionsResultList.get(i - 1);
                     assertEquals(i, dailyTransactionsResult.getDayIndex());
                     assertEquals(0, BigDecimal.ZERO.compareTo(dailyTransactionsResult.getResult()));
                 });
    }

    @Test
    @Transactional
    void getLastMonthTransactionsOverview_AccountIdNotSpecified() {
        log.info("Test of getting a monthly transaction report for the FE chart. The user will not have filled in the account number (accountId).");

        // Data preparation
        User user = createUser(true);
        user.setAccountId(null);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        // Execution
        Either<Failure, Success<LastMonthTransactionsOverviewDtoOut>> lastMonthTransactionsOverview = transactionService.getLastMonthTransactionsOverview();

        // Verification
        assertTrue(lastMonthTransactionsOverview.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, lastMonthTransactionsOverview.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "You do not have an account id filled out, you cannot create a transaction.";
        assertEquals(expectedMessage, lastMonthTransactionsOverview.getLeft().getMessage());
    }

    @Test
    void getLastMonthTransactionsOverview_UserNotSignedIn() {
        log.info("Test of getting a monthly transaction report for the FE chart. No user will be signed-in.");

        // Data preparation
        List<Transaction> transactionList = createTransactionList(1, 31, ACCOUNT_ID, Direction.INCOMING);
        transactionRepository.saveAll(transactionList);

        assertDatabaseSize(0, 0, 30);

        // Execution
        Either<Failure, Success<LastMonthTransactionsOverviewDtoOut>> lastMonthTransactionsOverview = transactionService.getLastMonthTransactionsOverview();

        // Verification
        assertTrue(lastMonthTransactionsOverview.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, lastMonthTransactionsOverview.getLeft().getHttpStatus());

        assertDatabaseSize(0, 0, 30);

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, lastMonthTransactionsOverview.getLeft().getMessage());
    }

    @Test
    void getLastMonthTransactionsOverview_UserNotFoundById() {
        log.info("Test of getting a monthly transaction report for the FE chart. The user will not be found in the database according to the id obtained from the Spring context (token) to verify its identity.");

        // Data preparation
        List<Transaction> transactionList = createTransactionList(1, 31, ACCOUNT_ID, Direction.INCOMING);
        transactionRepository.saveAll(transactionList);

        User user = createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3, 30);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setEmail(user.getEmail());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        // Execution
        Either<Failure, Success<LastMonthTransactionsOverviewDtoOut>> lastMonthTransactionsOverview = transactionService.getLastMonthTransactionsOverview();

        // Verification
        assertTrue(lastMonthTransactionsOverview.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, lastMonthTransactionsOverview.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3, 30);

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, lastMonthTransactionsOverview.getLeft().getMessage());
    }

    @Test
    void findTransactionsByAccountId_Hds() {
        log.info("Retrieve transactions related to the account number from the database for the last month.");

        // Data preparation
        List<Transaction> transactionList = createTransactionList(1, 6, ACCOUNT_ID, Direction.INCOMING);
        transactionList.addAll(createTransactionList(6, 11, ACCOUNT_ID, Direction.INCOMING));
        transactionRepository.saveAll(transactionList);

        // The user whose transactions will be tested
        User user1 = createUser(true);
        userRepository.save(user1);

        // A user who is "extra", his data will not be used in this test
        Long accountId = 1L;
        User user2 = createUser(true);
        user2.setEmail(user2.getEmail() + "m");
        user2.setAccountId(accountId);
        userRepository.save(user2);

        List<Transaction> transactionList2 = createTransactionList(10, 15, accountId, Direction.OUTGOING);
        transactionList2.stream().parallel()
                        .forEach(transaction -> transaction.getValue().setAmount(BigDecimal.valueOf(1000)));
        transactionRepository.saveAll(transactionList2);

        assertDatabaseSize(2, 6, 15);

        // Sign-in user
        signInUser(user1);

        // Execution
        Either<Failure, List<Transaction>> transactionsByAccountId = transactionService.findTransactionsByAccountId(ACCOUNT_ID);

        // Verification
        assertTrue(transactionsByAccountId.isRight());

        List<Transaction> transactions = transactionsByAccountId.get();
        assertNotNull(transactionList);
        assertThat(transactions, hasSize(10));
        transactionList.parallelStream()
                       .forEach(transaction -> {
                           assertEquals(ACCOUNT_ID, transaction.getAccountId());
                           assertEquals(Direction.INCOMING, transaction.getDirection());
                           assertEquals(BigDecimal.valueOf(500), transaction.getValue().getAmount());
                       });
    }

    private GetByDateIntervalDtoIn createGetByDateIntervalDtoIn() {
        ZonedDateTime now = ZonedDateTime.now(clock);

        GetByDateIntervalDtoIn dtoIn = new GetByDateIntervalDtoIn();
        dtoIn.setDateFrom(now);
        dtoIn.setDateTo(now);
        return dtoIn;
    }
}
