package cz.uhk.mois.financialplanning.repository;

import cz.uhk.mois.financialplanning.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Jan Krunčík
 * @since 15.03.2020 20:11
 */

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
