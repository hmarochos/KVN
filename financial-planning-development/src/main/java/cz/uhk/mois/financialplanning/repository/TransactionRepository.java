package cz.uhk.mois.financialplanning.repository;

import cz.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Jan Krunčík
 * @since 06.04.2020 0:43
 */

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByAccountId(Long accountId);

    @Transactional
    void deleteAllByAccountId(Long accountId);
}
