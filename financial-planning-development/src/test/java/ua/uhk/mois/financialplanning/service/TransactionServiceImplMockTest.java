package ua.uhk.mois.financialplanning.service;

import ua.uhk.mois.financialplanning.bank.DaySupport;
import ua.uhk.mois.financialplanning.configuration.AbsTestConfiguration;
import ua.uhk.mois.financialplanning.configuration.TransactionSupport;
import ua.uhk.mois.financialplanning.model.dto.transaction.AddTransactionDtoIn;
import ua.uhk.mois.financialplanning.model.dto.transaction.AdditionalInfoDomestic;
import ua.uhk.mois.financialplanning.model.dto.transaction.Direction;
import ua.uhk.mois.financialplanning.model.dto.transaction.GetByDateIntervalDtoIn;
import ua.uhk.mois.financialplanning.model.dto.transaction.GetByDateIntervalDtoOut;
import ua.uhk.mois.financialplanning.model.dto.transaction.Value;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.AddBankTransactionDtoIn;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.BankTransactionDtoOut;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.PartyAccount;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.TransactionType;
import ua.uhk.mois.financialplanning.model.entity.user.Role;
import ua.uhk.mois.financialplanning.model.entity.user.User;
import ua.uhk.mois.financialplanning.model.entity.wish.Currency;
import ua.uhk.mois.financialplanning.response.Failure;
import ua.uhk.mois.financialplanning.response.Success;
import ua.uhk.mois.financialplanning.service.bank.BankTransactionService;
import ua.uhk.mois.financialplanning.service.bank.UniTransaction;
import ua.uhk.mois.financialplanning.service.support.UserSupport;
import io.vavr.control.Either;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.createUser;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.signInUser;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.signOutUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class TransactionServiceImplMockTest extends AbsTestConfiguration {

    private static final ZonedDateTime TIME_INTERVAL = ZonedDateTime.of(2019, 3, 8, 15, 30, 35, 0, ZoneId.systemDefault());
    private static final ZonedDateTime TIME_INTERVAL_FROM = ZonedDateTime.of(2019, 1, 5, 15, 30, 35, 0, ZoneId.systemDefault());
    private static final ZonedDateTime TIME_INTERVAL_TO = ZonedDateTime.of(2021, 1, 5, 15, 30, 35, 0, ZoneId.systemDefault());

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private Clock clock;

    @Autowired
    private DaySupport daySupport;

    @Mock
    private BankTransactionService bankTransactionService;

    @Captor
    private ArgumentCaptor<AddBankTransactionDtoIn> addCaptor;

    private static BankTransactionDtoOut createAddBankTransactionDtoOut(Long accountId) {
        BankTransactionDtoOut dtoOut = new BankTransactionDtoOut();
        dtoOut.setValue(new Value(BigDecimal.valueOf(1000L), Currency.uaK));
        PartyAccount partyAccount = new PartyAccount();
        partyAccount.setAccountNumber(accountId.toString());
        partyAccount.setBankCode("123");
        dtoOut.setPartyAccount(partyAccount);
        dtoOut.setPartyDescription("Test description.");
        dtoOut.setDirection(Direction.OUTGOING);
        dtoOut.setTransactionType(TransactionType.CARD);
        dtoOut.setValueDate(TIME_INTERVAL);
        dtoOut.setBookingDate(TIME_INTERVAL);
        dtoOut.setAdditionalInfoDomestic(createAdditionalInfoDomestic());
        return dtoOut;
    }

    private static List<UniTransaction> createDummyBankTransactionDtoOutList(Long accountId) {
        return IntStream.range(0, 2)
                        .mapToObj(i -> createBankTransactionDtoOut(accountId))
                        .collect(Collectors.toList());
    }

    private static UniTransaction createBankTransactionDtoOut(Long accountId) {
        UniTransaction uniTransaction = new UniTransaction();
        uniTransaction.setValue(new Value(BigDecimal.valueOf(1000L), Currency.uaK));
        PartyAccount partyAccount = new PartyAccount();
        partyAccount.setAccountNumber(accountId.toString());
        partyAccount.setBankCode("123");
        uniTransaction.setPartyAccount(partyAccount);
        uniTransaction.setPartyDescription("Test description.");
        uniTransaction.setDirection(Direction.OUTGOING);
        uniTransaction.setTransactionType(TransactionType.CARD);
        uniTransaction.setValueDate(TIME_INTERVAL_TO);
        uniTransaction.setBookingDate(TIME_INTERVAL_TO);
        uniTransaction.setAdditionalInfoDomestic(createAdditionalInfoDomestic());
        return uniTransaction;
    }

    private static AdditionalInfoDomestic createAdditionalInfoDomestic() {
        AdditionalInfoDomestic additionalInfoDomestic = new AdditionalInfoDomestic();
        additionalInfoDomestic.setConstantSymbol("1234");
        additionalInfoDomestic.setVariableSymbol("123456789");
        additionalInfoDomestic.setSpecificSymbol("987654321");
        return additionalInfoDomestic;
    }

    private static void compareAddTransactionDtoInWithDtoOut(AddTransactionDtoIn dtoIn, BankTransactionDtoOut dtoOut, Long accountId) {
        assertEquals(accountId.toString(), dtoOut.getPartyAccount().getAccountNumber());
        assertEquals(dtoIn.getValue(), dtoOut.getValue());
        assertEquals(String.valueOf(dtoIn.getBankCode()), dtoOut.getPartyAccount().getBankCode());
        assertEquals(dtoIn.getPartyDescription(), dtoOut.getPartyDescription());
        assertEquals(dtoIn.getDirection(), dtoOut.getDirection());
        assertEquals(dtoIn.getTransactionType(), dtoOut.getTransactionType());
        assertTrue(dtoIn.getPaymentDate().isEqual(dtoOut.getValueDate()));
        assertTrue(dtoIn.getPaymentDate().isEqual(dtoOut.getBookingDate()));
        assertEquals(dtoIn.getAdditionalInfoDomestic(), dtoOut.getAdditionalInfoDomestic());
    }

    private static void compareAddBankTransactionDtoInWithDtoOut(AddBankTransactionDtoIn dtoIn, BankTransactionDtoOut dtoOut, Long accountId) {
        assertEquals(accountId.toString(), dtoOut.getPartyAccount().getAccountNumber());
        assertEquals(dtoIn.getValue(), dtoOut.getValue());
        assertEquals(String.valueOf(dtoIn.getPartyAccount().getBankCode()), dtoOut.getPartyAccount().getBankCode());
        assertEquals(dtoIn.getPartyDescription(), dtoOut.getPartyDescription());
        assertEquals(dtoIn.getDirection(), dtoOut.getDirection());
        assertEquals(dtoIn.getTransactionType(), dtoOut.getTransactionType());
        assertEquals(dtoIn.getValueDate(), dtoOut.getValueDate().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT));
        assertEquals(dtoIn.getValueDate(), dtoOut.getBookingDate().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT));
        assertEquals(dtoIn.getAdditionalInfoDomestic(), dtoOut.getAdditionalInfoDomestic());
    }

    private static void compareByDateIntervalDtoOut(List<UniTransaction> dummyBankTransactionDtoOut, List<BankTransactionDtoOut> dtoOut, Long accountId) {
        IntStream.range(0, 2)
                 .forEach(i -> compareUniTransactionWithGetByDateIntervalDtoOut(dummyBankTransactionDtoOut.get(i), dtoOut.get(i), accountId));
    }

    private static void compareUniTransactionWithGetByDateIntervalDtoOut(UniTransaction uniTransaction, BankTransactionDtoOut dtoOut, Long accountId) {
        assertEquals(accountId.toString(), dtoOut.getPartyAccount().getAccountNumber());
        assertEquals(uniTransaction.getValue(), dtoOut.getValue());
        assertEquals(String.valueOf(uniTransaction.getPartyAccount().getBankCode()), dtoOut.getPartyAccount().getBankCode());
        assertEquals(uniTransaction.getPartyDescription(), dtoOut.getPartyDescription());
        assertEquals(uniTransaction.getDirection(), dtoOut.getDirection());
        assertEquals(uniTransaction.getTransactionType(), dtoOut.getTransactionType());
        assertTrue(uniTransaction.getValueDate().isEqual(dtoOut.getValueDate()));
        assertTrue(uniTransaction.getBookingDate().isEqual(dtoOut.getBookingDate()));
        assertEquals(uniTransaction.getAdditionalInfoDomestic(), dtoOut.getAdditionalInfoDomestic());
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
    void add_Hds() {
        log.info("Add / create transaction test.");

        // Data preparation
        User user = createUser();
        user.setRoles(Collections.singletonList(Role.ADMIN));
        userRepository.save(user);

        assertDatabaseSize(1, 0, 0);

        signInUser(user);

        AddTransactionDtoIn dtoIn = TransactionSupport.createAddTransactionDtoIn();
        dtoIn.setPaymentDate(TIME_INTERVAL);

        BankTransactionDtoOut bankTransactionDtoOut = createAddBankTransactionDtoOut(user.getAccountId());
        Mockito.when(bankTransactionService.add(ArgumentMatchers.any(AddBankTransactionDtoIn.class)))
               .thenReturn(Mono.just(bankTransactionDtoOut));

        TransactionService transactionService = new TransactionServiceImpl(userSupport, bankTransactionService, modelMapper, transactionRepository, daySupport, clock);

        // Execution
        Either<Failure, Success<BankTransactionDtoOut>> add = transactionService.add(dtoIn);

        // Verification
        assertTrue(add.isRight());
        assertEquals(HttpStatus.CREATED, add.get().getHttpStatus());

        compareAddTransactionDtoInWithDtoOut(dtoIn, add.get().getBody(), user.getAccountId());

        Mockito.verify(bankTransactionService).add(addCaptor.capture());
        AddBankTransactionDtoIn bankDtoIn = addCaptor.getValue();

        compareAddBankTransactionDtoInWithDtoOut(bankDtoIn, bankTransactionDtoOut, user.getAccountId());
    }

    @Test
    void getByDateInterval_Hds() {
        log.info("Test of getting transactions according to the account number of the signed-in user and at the specified interval.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0, 0);

        signInUser(user);

        GetByDateIntervalDtoIn dtoIn = new GetByDateIntervalDtoIn(TIME_INTERVAL_FROM.minusYears(1), TIME_INTERVAL_TO);

        List<UniTransaction> dummyBankTransactionDtoOutList = createDummyBankTransactionDtoOutList(user.getAccountId());
        Mockito.when(bankTransactionService.getByDateInterval(ArgumentMatchers.eq(TIME_INTERVAL_FROM.minusYears(1)), ArgumentMatchers.eq(TIME_INTERVAL_TO), ArgumentMatchers.eq(user.getAccountId())))
               .thenReturn(Mono.just(dummyBankTransactionDtoOutList));

        TransactionService transactionService = new TransactionServiceImpl(userSupport, bankTransactionService, modelMapper, transactionRepository, daySupport, clock);

        // Execution
        Either<Failure, Success<GetByDateIntervalDtoOut>> byDateInterval = transactionService.getByDateInterval(dtoIn);

        // Verification
        assertTrue(byDateInterval.isRight());
        assertEquals(HttpStatus.OK, byDateInterval.get().getHttpStatus());

        compareByDateIntervalDtoOut(dummyBankTransactionDtoOutList, byDateInterval.get().getBody().getBankTransactionDtoOutList(), user.getAccountId());
    }

}
