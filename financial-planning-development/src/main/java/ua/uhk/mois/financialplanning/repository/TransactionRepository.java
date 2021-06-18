package ua.uhk.mois.financialplanning.repository;

import ua.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author KVN
 * @since 06.04.2021 0:43
 */

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByAccountId(Long accountId);

    @Transactional
    void deleteAllByAccountId(Long accountId);
}
