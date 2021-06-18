package ua.uhk.mois.financialplanning.configuration;

import ua.uhk.mois.financialplanning.repository.TransactionRepository;
import ua.uhk.mois.financialplanning.repository.UserRepository;
import ua.uhk.mois.financialplanning.repository.WishRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author KVN
 * @since 16.03.2021 18:59
 */

@SpringBootTest
@WebAppConfiguration
@TestPropertySource(locations = "/application.properties")
@Log4j2
public class AbsTestConfiguration {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected WishRepository wishRepository;

    @Autowired
    protected TransactionRepository transactionRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected void clearDatabase() {
        log.info("Clear database.");
        userRepository.deleteAll();
        wishRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    protected void assertEmptyDatabase() {
        assertDatabaseSize(0, 0);
    }

    protected void assertDatabaseSize(int users, int wishes) {
        assertDatabaseSize(users, wishes, 0);
    }

    protected void assertDatabaseSize(int users, int wishes, int transactions) {
        assertThat(userRepository.findAll(), hasSize(users));
        assertThat(wishRepository.findAll(), hasSize(wishes));
        assertThat(transactionRepository.findAll(), hasSize(transactions));
    }
}
