package cz.uhk.mois.financialplanning.repository;

import cz.uhk.mois.financialplanning.model.entity.wish.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Jan Krunčík
 * @since 22.03.2020 15:28
 */

public interface WishRepository extends JpaRepository<Wish, Long> {

    Page<Wish> findAllByUserId(Long id, Pageable pageable);
}
