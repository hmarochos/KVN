package ua.uhk.mois.financialplanning.security.loader;

import ua.uhk.mois.financialplanning.model.entity.user.Address;
import ua.uhk.mois.financialplanning.model.entity.user.Role;
import ua.uhk.mois.financialplanning.model.entity.user.User;
import ua.uhk.mois.financialplanning.repository.UserRepository;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

/**
 * When the application is started, it tests whether a particular user with the admin role is in the database, if not,
 * it creates it. <br/>
 * <i>Only this admin user can create transactions (for testing purposes).</i>
 *
 * @author KVN
 * @since 14.04.2021 7:26
 */

@Component
@Log4j2
public class AdminCreator implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin.admin@gmail.com";

    private final UserRepository userRepository;
    private final Clock clock;

    public AdminCreator(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    private static Address createAddress() {
        Address address = new Address();
        address.setStreet("Hell 666");
        address.setCity("Hell");
        address.setPsc(48169);
        return address;
    }

    @Override
    public void run(String... args) {
        findAdminByEmail()
                .onSuccess(user -> {
                    if (user.isPresent()) {
                        log.info("Admin was found in the database. {}", user.get());
                    } else {
                        log.info("Admin is not found in the database, it will be created.");
                        User adminUser = createAdminUser();
                        saveAdmin(adminUser);
                    }
                });
    }

    private Try<Optional<User>> findAdminByEmail() {
        return Try.of(() -> userRepository.findByEmail(ADMIN_EMAIL))
                  .onFailure(throwable -> log.error("An error occurred while trying to load a user (admin) from the database.", throwable));
    }

    private void saveAdmin(User user) {
        Try.run(() -> {
            log.info("Save the user (admin) to the database. {}", user);
            userRepository.save(user);
        })
           .onFailure(throwable -> log.error("There was an error trying to save the user (admin) to the database.", throwable))
           .onSuccess(aVoid -> log.info("User (admin) successfully created."));
    }

    private User createAdminUser() {
        ZonedDateTime now = ZonedDateTime.now(clock);

        User user = new User();
        user.setFirstName("Admin");
        user.setLastName("Admin");
        user.setEmail(ADMIN_EMAIL);
        // Hash for: Admin_Hell_3_?_21-789+éíá!
        user.setPasswordHash("$2a$10$vzbyU9hudka/BB3op4q1rOEY3EH2LkUW8m1Vv2FMisobjGBa6b3iW");
        user.setTelephoneNumber("+420 123 456 789");
        user.setAddress(createAddress());
        user.setRoles(Collections.singletonList(Role.ADMIN));
        user.setLastLogin(now);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }
}
