package cz.uhk.mois.financialplanning.bank;

import cz.uhk.mois.financialplanning.model.entity.user.User;
import cz.uhk.mois.financialplanning.repository.TransactionRepository;
import cz.uhk.mois.financialplanning.repository.UserRepository;
import cz.uhk.mois.financialplanning.service.CopyTransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * It is used to copy transactions of all users in the last month to "our" database for further work. <br/>
 * <i>The user then has data for the last month.</i>
 */

@Component
@Log4j2
public class CopyTransactionSchedule {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CopyTransactionService copyTransactionService;

    public CopyTransactionSchedule(UserRepository userRepository, TransactionRepository transactionRepository, CopyTransactionService copyTransactionService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.copyTransactionService = copyTransactionService;
    }

    /**
     * Delete all transactions currently stored in "our" database. And subsequent copying of all users' transactions
     * from last month. <br/>
     * <i>This is a task that is performed every first day of each month.</i>
     * <br/>
     * <i>In this way, the user will always work with all transactions in the last month.</i>
     * <br/>
     * <i>So, for example, a "banking" database (its simulation) may not be available and the user can still view his
     * transactions backed up in our database.</i>
     */
    @Scheduled(cron = "0 0 0 1 * *")
    public void copyTransactionsFromLastMonth() {
        log.info("Scheduled copying of all transactions (all of our users) made last month.");

        // Delete all current records (transactions)
        transactionRepository.deleteAll();

        userRepository.findAll()
                      .parallelStream()
                      .filter(user -> user.getAccountId() != null)
                      .map(User::getAccountId)
                      .forEach(copyTransactionService::copyTransactionsFromLastMonth);
    }

}
