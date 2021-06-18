package cz.uhk.mois.financialplanning.bank;

import cz.uhk.mois.financialplanning.configuration.BankSupport;
import cz.uhk.mois.financialplanning.configuration.user.UserTestSupport;
import cz.uhk.mois.financialplanning.model.dto.transaction.Direction;
import cz.uhk.mois.financialplanning.model.dto.transaction.bank.TransactionType;
import cz.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import cz.uhk.mois.financialplanning.model.entity.user.User;
import cz.uhk.mois.financialplanning.model.entity.wish.Currency;
import cz.uhk.mois.financialplanning.repository.TransactionRepository;
import cz.uhk.mois.financialplanning.repository.UserRepository;
import cz.uhk.mois.financialplanning.service.CopyTransactionService;
import cz.uhk.mois.financialplanning.service.CopyTransactionServiceImpl;
import cz.uhk.mois.financialplanning.service.bank.BankTransactionService;
import cz.uhk.mois.financialplanning.service.bank.BankTransactionServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CopyTransactionScheduleTest.CopyTransactionScheduleTestConfig.class})
@Log4j2
class CopyTransactionScheduleTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankTransactionService bankTransactionService;

    @Autowired
    private CopyTransactionService copyTransactionService;

    @Captor
    private ArgumentCaptor<List<Transaction>> transactionRepositoryCaptor;

    private static void assertCreatedBankTransaction(Long accountId, Transaction transaction) {
        Assertions.assertEquals(accountId, Long.valueOf(transaction.getPartyAccount().getAccountNumber()));

        Assertions.assertEquals(BigDecimal.valueOf(1000), transaction.getValue().getAmount());
        Assertions.assertEquals(Currency.CZK, transaction.getValue().getCurrency());
        Assertions.assertNotNull(transaction.getValue().getTransaction());

        Assertions.assertEquals(accountId.toString(), transaction.getPartyAccount().getAccountNumber());
        Assertions.assertEquals("123", transaction.getPartyAccount().getBankCode());
        Assertions.assertNotNull(transaction.getPartyAccount().getTransaction());

        Assertions.assertEquals("Test description.", transaction.getPartyDescription());
        Assertions.assertEquals(Direction.OUTGOING, transaction.getDirection());
        Assertions.assertEquals(TransactionType.CASH, transaction.getTransactionType());
    }

    @Test
    void copyTransactionsFromLastMonth() {
        log.info("Test copying transactions from last month.");

        // Data preparation
        Long accountId1 = 1L;
        Long accountId2 = 2L;

        User user1 = UserTestSupport.createUser(true);
        user1.setAccountId(accountId1);
        User user2 = UserTestSupport.createUser(true);
        user2.setAccountId(accountId2);

        Mockito.when(userRepository.findAll())
               .thenReturn(Arrays.asList(user1, user2));

        Mockito.when(bankTransactionService.getByDateInterval(ArgumentMatchers.any(ZonedDateTime.class), ArgumentMatchers.any(ZonedDateTime.class), ArgumentMatchers.eq(accountId1)))
               .thenReturn(Mono.just(BankSupport.createDummyBankTransactionDtoOutList(accountId1)));
        Mockito.when(bankTransactionService.getByDateInterval(ArgumentMatchers.any(ZonedDateTime.class), ArgumentMatchers.any(ZonedDateTime.class), ArgumentMatchers.eq(accountId2)))
               .thenReturn(Mono.just(BankSupport.createDummyBankTransactionDtoOutList(accountId2)));

        CopyTransactionSchedule copyTransactionSchedule = new CopyTransactionSchedule(userRepository, transactionRepository, copyTransactionService);

        // Execution
        copyTransactionSchedule.copyTransactionsFromLastMonth();

        // Verification
        Mockito.verify(transactionRepository, Mockito.times(2)).saveAll(transactionRepositoryCaptor.capture());
        List<List<Transaction>> allValues = transactionRepositoryCaptor.getAllValues();

        allValues.sort(Comparator.comparing(o -> o.get(0).getPartyAccount().getAccountNumber()));

        IntStream.range(0, allValues.size())
                 .forEach(i -> allValues.get(i).parallelStream()
                                        .forEach(transaction -> assertCreatedBankTransaction(i + 1L, transaction)));
    }

    @Configuration
    static class CopyTransactionScheduleTestConfig {

        @Bean
        BankTransactionService bankTransactionService() {
            return Mockito.mock(BankTransactionServiceImpl.class);
        }

        @Bean
        DaySupport daySupport() {
            return new DaySupportImpl();
        }

        @Bean
        CopyTransactionService copyTransactionService() {
            return new CopyTransactionServiceImpl(transactionRepository(), bankTransactionService(), new ModelMapper(), daySupport(), Clock.systemUTC());
        }

        @Bean
        UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        TransactionRepository transactionRepository() {
            return Mockito.mock(TransactionRepository.class);
        }
    }
}
