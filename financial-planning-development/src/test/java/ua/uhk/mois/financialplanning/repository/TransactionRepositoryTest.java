package ua.uhk.mois.financialplanning.repository;

import ua.uhk.mois.financialplanning.configuration.AbsTestConfiguration;
import ua.uhk.mois.financialplanning.configuration.TransactionSupport;
import ua.uhk.mois.financialplanning.model.dto.transaction.Direction;
import ua.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.ACCOUNT_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Log4j2
class TransactionRepositoryTest extends AbsTestConfiguration {

    @BeforeEach
    void setUp() {
        log.info("Clear database before test.");
        clearDatabase();
    }

    @AfterEach
    void tearDown() {
        log.info("Clear database after test.");
        clearDatabase();
    }

    @Test
    void findAllByAccountId_Hds() {
        log.info("Test of loading transactions by account number.");

        // Data preparation
        List<Transaction> transactionList = TransactionSupport.createTransactionList(1, 11, ACCOUNT_ID, Direction.INCOMING);
        transactionRepository.saveAll(transactionList);

        List<Transaction> transactionList2 = TransactionSupport.createTransactionList(15, 20, 1L, Direction.OUTGOING);
        transactionRepository.saveAll(transactionList2);

        assertDatabaseSize(0, 0, 15);

        // Execution
        List<Transaction> allByAccountId = transactionRepository.findAllByAccountId(ACCOUNT_ID);

        // Verification
        assertNotNull(allByAccountId);
        assertThat(allByAccountId, hasSize(10));
        allByAccountId.forEach(transaction -> {
            assertEquals(ACCOUNT_ID, transaction.getAccountId());
            assertEquals(Direction.INCOMING, transaction.getDirection());
        });

        assertDatabaseSize(0, 0, 15);
    }

    @Test
    void deleteAllByAccountId() {
        log.info("Test for deleting transactions related to a specific account number.");

        // Data preparation
        List<Transaction> transactionList = TransactionSupport.createTransactionList(1, 11, ACCOUNT_ID, Direction.INCOMING);
        transactionList.addAll(TransactionSupport.createTransactionList(11, 21, 1L, Direction.OUTGOING));
        transactionRepository.saveAll(transactionList);

        assertDatabaseSize(0, 0, 20);

        // Execution
        transactionRepository.deleteAllByAccountId(ACCOUNT_ID);

        // Verification
        assertDatabaseSize(0, 0, 10);

        List<Transaction> all = transactionRepository.findAll();
        all.parallelStream()
           .forEach(transaction -> {
               assertEquals(1L, transaction.getAccountId());
               assertEquals(Direction.OUTGOING, transaction.getDirection());
           });
    }
}
