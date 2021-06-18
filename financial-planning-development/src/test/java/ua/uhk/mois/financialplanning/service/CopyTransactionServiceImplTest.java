package ua.uhk.mois.financialplanning.service;

import ua.uhk.mois.financialplanning.bank.DaySupport;
import ua.uhk.mois.financialplanning.bank.DaySupportImpl;
import ua.uhk.mois.financialplanning.configuration.BankSupport;
import ua.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import ua.uhk.mois.financialplanning.repository.TransactionRepository;
import ua.uhk.mois.financialplanning.service.bank.BankTransactionService;
import ua.uhk.mois.financialplanning.service.bank.BankTransactionServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;

import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.ACCOUNT_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CopyTransactionServiceImplTest.CopyTransactionServiceImplTestConfig.class)
@Log4j2
class CopyTransactionServiceImplTest {

    @Autowired
    private CopyTransactionService copyTransactionService;

    @Autowired
    private BankTransactionService bankTransactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Captor
    private ArgumentCaptor<List<Transaction>> transactionRepositoryArgumentCaptor;

    @Test
    void copyTransactionsFromLastMonth() {
        log.info("Test of copying the user's transactions for the last month according to his account number (account Id). Loading transactions from the bank database needs to be mocked, because it is not 'our' database that will always be available.");

        // Data preparation
        when(bankTransactionService.getByDateInterval(any(ZonedDateTime.class), any(ZonedDateTime.class), anyLong()))
                .thenReturn(Mono.just(BankSupport.createDummyBankTransactionDtoOutList(ACCOUNT_ID)));

        // Execution
        copyTransactionService.copyTransactionsFromLastMonth(ACCOUNT_ID);

        // Verification
        verify(transactionRepository, times(1))
                .saveAll(transactionRepositoryArgumentCaptor.capture());
        List<List<Transaction>> allValues = transactionRepositoryArgumentCaptor.getAllValues();

        assertNotNull(allValues);
        assertThat(allValues, hasSize(1));

        List<Transaction> transactionList = allValues.get(0);
        assertNotNull(transactionList);
        assertThat(transactionList, hasSize(2));

        transactionList.parallelStream()
                       .forEach(transaction -> assertEquals(ACCOUNT_ID, transaction.getAccountId()));
    }

    @Configuration
    static class CopyTransactionServiceImplTestConfig {

        @Bean
        TransactionRepository transactionRepository() {
            return Mockito.mock(TransactionRepository.class);
        }

        @Bean
        BankTransactionService bankTransactionService() {
            return Mockito.mock(BankTransactionServiceImpl.class);
        }

        @Bean
        ModelMapper modelMapper() {
            return new ModelMapper();
        }

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }

        @Bean
        DaySupport daySupport() {
            return new DaySupportImpl();
        }

        @Bean
        CopyTransactionServiceImpl copyTransactionService() {
            return new CopyTransactionServiceImpl(transactionRepository(), bankTransactionService(), modelMapper(), daySupport(), clock());
        }
    }

}
