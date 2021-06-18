package ua.uhk.mois.financialplanning.security.loader;

import ua.uhk.mois.financialplanning.model.entity.user.Role;
import ua.uhk.mois.financialplanning.model.entity.user.User;
import ua.uhk.mois.financialplanning.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.signOutUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "/application.properties")
@Log4j2
class AdminCreatorTest {

    private static final String ADMIN_EMAIL = "admin.admin@gmail.com";

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        log.info("Logout user before test (if any).");
        signOutUser();
    }

    @AfterEach
    void tearDown() {
        log.info("Clear database after test.");
        userRepository.deleteAll();

        log.info("Logout user after test.");
        signOutUser();
    }

    @Test
    @Transactional
    void run() {
        log.info("Test (verification) that after running the application in the database creates a user with the role of admin (if there is no such user).");

        assertThat(userRepository.findAll(), hasSize(1));

        Optional<User> adminByEmail = userRepository.findByEmail(ADMIN_EMAIL);
        assertTrue(adminByEmail.isPresent());

        User user = adminByEmail.get();

        assertEquals("Admin", user.getFirstName());
        assertEquals("Admin", user.getLastName());
        assertEquals("+420 123 456 789", user.getTelephoneNumber());

        assertNotNull(user.getPasswordHash());
        assertEquals("$2a$10$vzbyU9hudka/BB3op4q1rOEY3EH2LkUW8m1Vv2FMisobjGBa6b3iW", user.getPasswordHash());

        assertEquals("Hell 666", user.getAddress().getStreet());
        assertEquals("Hell", user.getAddress().getCity());
        assertEquals(48169, user.getAddress().getPsc());

        assertNotNull(user.getRoles());
        assertThat(user.getRoles(), hasSize(1));
        assertEquals(Role.ADMIN, user.getRoles().get(0));

        assertNotNull(user.getLastLogin());
        assertTrue(user.getLastLogin().isEqual(user.getCreatedAt()));
        assertTrue(user.getCreatedAt().isEqual(user.getUpdatedAt()));
    }
}
