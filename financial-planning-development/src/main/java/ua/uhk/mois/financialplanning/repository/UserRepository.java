package ua.uhk.mois.financialplanning.repository;

import ua.uhk.mois.financialplanning.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author KVN
 * @since 15.03.2021 20:11
 */

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
