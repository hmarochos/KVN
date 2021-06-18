package cz.uhk.mois.financialplanning.service.support;

import cz.uhk.mois.financialplanning.configuration.AbsTestConfiguration;
import cz.uhk.mois.financialplanning.configuration.user.UserTestSupport;
import cz.uhk.mois.financialplanning.model.entity.user.User;
import cz.uhk.mois.financialplanning.response.Failure;
import io.vavr.control.Either;
import lombok.extern.log4j.Log4j2;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.ACCOUNT_ID;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.CITY;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.EMAIL;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.FIRST_NAME;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.LAST_NAME;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.PSC;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.STREET;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.TELEPHONE_NUMBER;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.signOutUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class UserSupportImplTest extends AbsTestConfiguration {

    @Autowired
    private UserSupport userSupport;

    @BeforeEach
    void setUp() {
        log.info("Clear database before test.");
        clearDatabase();

        log.info("Logout user before test (if any).");
        signOutUser();
    }

    @AfterEach
    void tearDown() {
        log.info("Clear database after test.");
        clearDatabase();

        log.info("Logout user after test.");
        signOutUser();
    }

    @Test
    @Transactional
    void findUserById_Hds() {
        log.info("User load test by id.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Execution
        Either<Failure, User> userById = userSupport.findUserById(user.getId());

        // Verification
        assertTrue(userById.isRight());

        assertEquals(userById.get().getId(), user.getId());
        assertEquals(FIRST_NAME, userById.get().getFirstName());
        assertEquals(LAST_NAME, userById.get().getLastName());
        assertEquals(EMAIL, userById.get().getEmail());
        assertNotNull(userById.get().getPasswordHash());
        assertEquals(ACCOUNT_ID, userById.get().getAccountId());
        assertEquals(TELEPHONE_NUMBER, userById.get().getTelephoneNumber());
        assertEquals(STREET, userById.get().getAddress().getStreet());
        assertEquals(CITY, userById.get().getAddress().getCity());
        assertEquals(PSC, userById.get().getAddress().getPsc());
        assertThat(userById.get().getWishList(), Matchers.hasSize(3));
        assertThat(userById.get().getRoles(), Matchers.hasSize(1));
        assertNotNull(userById.get().getLastLogin());

        assertNotNull(userById.get().getLastLogin());
        assertNotNull(userById.get().getCreatedAt());
        assertNotNull(userById.get().getUpdatedAt());

        assertEquals(user.getLastLogin(), userById.get().getLastLogin());
        assertEquals(user.getCreatedAt(), userById.get().getCreatedAt());
        assertEquals(user.getUpdatedAt(), userById.get().getUpdatedAt());

        assertDatabaseSize(1, 3);
    }

    @Test
    void findUserById_NotFoundById() {
        log.info("User load test by id. The non-existent user id in the database is specified.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        Long unknownId = user.getId() + 1;

        // Execution
        Either<Failure, User> userById = userSupport.findUserById(unknownId);

        // Verification
        assertTrue(userById.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, userById.getLeft().getHttpStatus());

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, userById.getLeft().getMessage());

        assertDatabaseSize(1, 3);
    }
}
