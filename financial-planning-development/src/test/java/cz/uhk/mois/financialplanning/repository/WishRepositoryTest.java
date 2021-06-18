package cz.uhk.mois.financialplanning.repository;

import cz.uhk.mois.financialplanning.configuration.AbsTestConfiguration;
import cz.uhk.mois.financialplanning.configuration.user.UserTestSupport;
import cz.uhk.mois.financialplanning.model.entity.user.User;
import cz.uhk.mois.financialplanning.model.entity.wish.Wish;
import lombok.extern.log4j.Log4j2;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class WishRepositoryTest extends AbsTestConfiguration {

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
    void findAllByUserId() {
        log.info("Test of loading the user's wish list according to his id.");

        // Data preparation
        User user = UserTestSupport.createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Execution
        PageRequest pageRequestAscByPriority = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "priority"));
        Page<Wish> wishPageAscByPriority = wishRepository.findAllByUserId(user.getId(), pageRequestAscByPriority);

        PageRequest pageRequestDescByName = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "name"));
        Page<Wish> wishPageDescByName = wishRepository.findAllByUserId(user.getId(), pageRequestDescByName);

        // Verification
        assertEquals(3, wishPageAscByPriority.getTotalElements());
        assertEquals(3, wishPageDescByName.getTotalElements());

        assertThat(wishPageAscByPriority.getContent(), Matchers.hasSize(3));
        assertThat(wishPageDescByName.getContent(), Matchers.hasSize(3));

        List<Wish> wishListByPriority = wishPageAscByPriority.getContent();
        assertEquals(1, wishListByPriority.get(0).getPriority());
        assertEquals("Wish 0", wishListByPriority.get(0).getName());
        assertEquals(2, wishListByPriority.get(1).getPriority());
        assertEquals("Wish 1", wishListByPriority.get(1).getName());
        assertEquals(3, wishListByPriority.get(2).getPriority());
        assertEquals("Wish 2", wishListByPriority.get(2).getName());

        List<Wish> wishListByName = wishPageDescByName.getContent();
        assertEquals("Wish 2", wishListByName.get(0).getName());
        assertEquals(3, wishListByName.get(0).getPriority());
        assertEquals("Wish 1", wishListByName.get(1).getName());
        assertEquals(2, wishListByName.get(1).getPriority());
        assertEquals("Wish 0", wishListByName.get(2).getName());
        assertEquals(1, wishListByName.get(2).getPriority());
    }
}
