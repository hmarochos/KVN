package ua.uhk.mois.financialplanning.repository;

import ua.uhk.mois.financialplanning.configuration.AbsTestConfiguration;
import ua.uhk.mois.financialplanning.configuration.user.UserTestSupport;
import ua.uhk.mois.financialplanning.model.entity.user.User;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class UserRepositoryTest extends AbsTestConfiguration {

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
    void findByEmail_Hds() {
        log.info("Test for finding a user by email address.");

        // Data preparation
        User user = UserTestSupport.createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Execution
        String email = "homer.simpson@gmail.com";
        Optional<User> userByEmail = userRepository.findByEmail(email);

        // Verification
        assertTrue(userByEmail.isPresent());
    }

    @Test
    void findByEmail_NonExistentEmail() {
        log.info("Test for finding a user by email address. An email that is not in the database will be entered.");

        // Data preparation
        User user = UserTestSupport.createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        // Execution
        String email = "nonExistentEmail@gmail.com";
        Optional<User> userByEmail = userRepository.findByEmail(email);

        // Verification
        assertFalse(userByEmail.isPresent());
    }
}
