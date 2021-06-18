package ua.uhk.mois.financialplanning.service;

public interface CopyTransactionService {

    /**
     * Copy a user's transactions made last month from the bank database to our database.
     *
     * @param accountId
     *         accountId of the user whose transactions should be copied
     */
    void copyTransactionsFromLastMonth(Long accountId);

}
