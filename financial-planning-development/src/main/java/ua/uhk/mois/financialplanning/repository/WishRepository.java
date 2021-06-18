package ua.uhk.mois.financialplanning.repository;

import ua.uhk.mois.financialplanning.model.entity.wish.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author KVN
 * @since 22.03.2021 15:28
 */

public interface WishRepository extends JpaRepository<Wish, Long> {

    Page<Wish> findAllByUserId(Long id, Pageable pageable);
}
