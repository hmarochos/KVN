package ua.uhk.mois.financialplanning.service;

import ua.uhk.mois.financialplanning.configuration.AbsTestConfiguration;
import ua.uhk.mois.financialplanning.model.dto.wish.ChangePriorityDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.ChangePriorityDtoOut;
import ua.uhk.mois.financialplanning.model.dto.wish.ChangePriorityWishInfo;
import ua.uhk.mois.financialplanning.model.dto.wish.CreateWishDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.DeleteAllWishesDtoOut;
import ua.uhk.mois.financialplanning.model.dto.wish.DeleteWishDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.UpdateWishDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.WishDtoOut;
import ua.uhk.mois.financialplanning.model.dto.wish.WishListDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.WishListDtoOut;
import ua.uhk.mois.financialplanning.model.entity.user.User;
import ua.uhk.mois.financialplanning.model.entity.wish.Currency;
import ua.uhk.mois.financialplanning.model.entity.wish.Wish;
import ua.uhk.mois.financialplanning.response.Failure;
import ua.uhk.mois.financialplanning.response.Success;
import io.vavr.control.Either;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ua.uhk.mois.financialplanning.configuration.WishSupport.createChangePriorityWishInfo;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.createUser;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.signInUser;
import static ua.uhk.mois.financialplanning.configuration.user.UserTestSupport.signOutUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class WishServiceImplTest extends AbsTestConfiguration {

    private static final BigDecimal PRICE = BigDecimal.valueOf(1000L);
    private static final String NAME = "House";
    private static final Integer PRIORITY = 1;

    private static final BigDecimal UPDATED_PRICE = BigDecimal.valueOf(5000L);
    private static final String UPDATED_NAME = "car";
    private static final Integer UPDATED_PRIORITY = 2;
    private static final String UPDATED_DESCRIPTION = "Some updated description.";

    private static final String ORDER_ASC = "ASC";
    private static final String ORDER_DESC = "DESC";

    private static final String ORDER_BY_PRIORITY = "priority";
    private static final String ORDER_BY_NAME = "name";

    @Autowired
    private WishService wishService;

    private static CreateWishDtoIn createCreateWishDtoIn() {
        CreateWishDtoIn dtoIn = new CreateWishDtoIn();
        dtoIn.setPrice(PRICE);
        dtoIn.setCurrency(Currency.uaK);
        dtoIn.setName(NAME);
        dtoIn.setDescription("Some description.");
        return dtoIn;
    }

    private static UpdateWishDtoIn createUpdateWishDtoIn(Long id) {
        UpdateWishDtoIn dtoIn = new UpdateWishDtoIn();
        dtoIn.setId(id);
        dtoIn.setPrice(UPDATED_PRICE);
        dtoIn.setCurrency(Currency.uaK);
        dtoIn.setName(UPDATED_NAME);
        dtoIn.setDescription(UPDATED_DESCRIPTION);
        dtoIn.setPriority(UPDATED_PRIORITY);
        return dtoIn;
    }

    private static void assertWishDtoOut(WishDtoOut dtoOut, BigDecimal price, String name, int priority) {
        assertEquals(0, price.compareTo(dtoOut.getPrice()));
        assertEquals(Currency.uaK, dtoOut.getCurrency());
        assertEquals(name, dtoOut.getName());
        assertEquals("Some description.", dtoOut.getDescription());
        assertEquals(priority, dtoOut.getPriority());
    }

    private static WishListDtoIn createWishListDtoIn(Integer page, Integer size) {
        WishListDtoIn dtoIn = new WishListDtoIn();
        dtoIn.setPage(page);
        dtoIn.setSize(size);
        dtoIn.setOrder(ORDER_ASC);
        dtoIn.setOrderBy(ORDER_BY_PRIORITY);
        return dtoIn;
    }

    private static DeleteWishDtoIn createDeleteWishDtoIn(Long wishId) {
        DeleteWishDtoIn dtoIn = new DeleteWishDtoIn();
        dtoIn.setId(wishId);
        return dtoIn;
    }

    private static void setDefaultWishPriorities(List<Wish> wishList) {
        final int startPriority = 5;

        IntStream.range(0, wishList.size())
                 .parallel()
                 .forEach(i -> wishList.get(i).setPriority(startPriority + i));
    }

    private static List<Long> getWishesIdList(List<Wish> wishList) {
        return wishList.parallelStream()
                       .map(Wish::getId)
                       .collect(Collectors.toList());
    }

    private static ChangePriorityDtoIn createChangePriorityDtoIn(List<Long> wishIdList) {
        ChangePriorityDtoIn dtoIn = new ChangePriorityDtoIn();
        dtoIn.setChangePriorityWishInfoList(createChangePriorityWishInfoList(wishIdList));
        return dtoIn;
    }

    private static List<ChangePriorityWishInfo> createChangePriorityWishInfoList(List<Long> wishIdList) {
        return IntStream.range(0, wishIdList.size())
                        .mapToObj(i -> createChangePriorityWishInfo(wishIdList.get(i), i + 1))
                        .collect(Collectors.toList());
    }

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
    void add_Hds() {
        log.info("Test adding / creating a new wish (/ goal) to the database.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 0);

        CreateWishDtoIn dtoIn = createCreateWishDtoIn();

        // Execution
        Either<Failure, Success<WishDtoOut>> add = wishService.add(dtoIn);

        // Verification
        assertTrue(add.isRight());
        assertEquals(HttpStatus.CREATED, add.get().getHttpStatus());

        assertDatabaseSize(1, 1);

        WishDtoOut wishDtoOut = add.get().getBody();
        assertWishDtoOut(wishDtoOut, PRICE, NAME, PRIORITY);

        Optional<Wish> wishById = wishRepository.findById(wishDtoOut.getId());
        assertTrue(wishById.isPresent());

        Wish wish = wishById.get();
        assertEquals(NAME, wish.getName());
        assertEquals(1, wish.getPriority());
    }

    @Test
    @Transactional
    void add_Hds_HandleCorrectPriority() {
        log.info("Test adding / creating a new wish (/ goal) to the database. A wish is added that does not have priority in dtoIn with the correct order. The correct priority will be set before the wish is created.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 3);

        CreateWishDtoIn dtoIn = createCreateWishDtoIn();

        // Execution
        Either<Failure, Success<WishDtoOut>> add = wishService.add(dtoIn);

        // Verification
        assertTrue(add.isRight());
        assertEquals(HttpStatus.CREATED, add.get().getHttpStatus());

        assertDatabaseSize(1, 4);

        WishDtoOut wishDtoOut = add.get().getBody();
        assertWishDtoOut(wishDtoOut, PRICE, NAME, 4);

        Optional<Wish> wishById = wishRepository.findById(wishDtoOut.getId());
        assertTrue(wishById.isPresent());

        Wish wish = wishById.get();
        assertEquals(4, wish.getPriority());
    }

    @Test
    void add_InvalidDtoIn() {
        log.info("Test adding / creating a new wish (/ goal) to the database. The input dtoIn will contain invalid values.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 0);

        CreateWishDtoIn dtoIn = createCreateWishDtoIn();
        dtoIn.setPrice(BigDecimal.valueOf(-1000L));
        dtoIn.setName("¨;`~°´=éíáážřlksjfd.?§");
        dtoIn.setDescription("!<>[]{}()/\\|");

        // Execution
        Either<Failure, Success<WishDtoOut>> add = wishService.add(dtoIn);

        // Verification
        assertTrue(add.isLeft());

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, add.getLeft().getHttpStatus());

        String expectedMessage = "The price must be a positive number., The name contains illegal characters: ';=`~§¨°´'., The description contains illegal characters: '()/<>[\\]{|}'.";
        assertEquals(expectedMessage, add.getLeft().getMessage());

        assertDatabaseSize(1, 0);
    }

    @Test
    void add_UserNotSignedIn() {
        log.info("Test adding / creating a new wish (/ goal) to the database. No user will be signed-in (in the Spring context). Therefore, it would not be possible to determine to whom the new wish should be added.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        CreateWishDtoIn dtoIn = createCreateWishDtoIn();

        // Execution
        Either<Failure, Success<WishDtoOut>> add = wishService.add(dtoIn);

        // Verification
        assertTrue(add.isLeft());

        assertEquals(HttpStatus.UNAUTHORIZED, add.getLeft().getHttpStatus());

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, add.getLeft().getMessage());

        assertDatabaseSize(1, 0);
    }

    @Test
    void add_UserNotFoundById() {
        log.info("Test adding / creating a new wish (/ goal) to the database. The user will not be found in the database according to the id obtained from the token (signed-in user in the Spring context).");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setEmail(user.getEmail());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        assertDatabaseSize(1, 0);

        CreateWishDtoIn dtoIn = createCreateWishDtoIn();

        // Execution
        Either<Failure, Success<WishDtoOut>> add = wishService.add(dtoIn);

        // Verification
        assertTrue(add.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, add.getLeft().getHttpStatus());

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, add.getLeft().getMessage());

        assertDatabaseSize(1, 0);
    }

    @Test
    @Transactional
    void add_DuplicateWish() {
        log.info("Test adding / creating a new wish (/ goal) to the database. A duplicate creation request will be provided. But only one wish with the same name can be entered in the user's races.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 3);

        String name = "Wish 1";
        CreateWishDtoIn dtoIn = createCreateWishDtoIn();
        dtoIn.setName(name);

        // Execution
        Either<Failure, Success<WishDtoOut>> add = wishService.add(dtoIn);

        // Verification
        assertTrue(add.isLeft());
        assertEquals(HttpStatus.CONFLICT, add.getLeft().getHttpStatus());

        String expectedMessage = String.format("Wish '%s' is already used.", name);
        assertEquals(expectedMessage, add.getLeft().getMessage());

        assertDatabaseSize(1, 3);
    }

    @Test
    @Transactional
    void update_Hds() {
        log.info("Test adjustment of wish.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 3);

        Long id = user.getWishList().get(0).getId();
        UpdateWishDtoIn dtoIn = createUpdateWishDtoIn(id);

        // Execution
        Either<Failure, Success<WishDtoOut>> update = wishService.update(dtoIn);

        // Verification
        assertTrue(update.isRight());
        assertEquals(HttpStatus.OK, update.get().getHttpStatus());

        assertDatabaseSize(1, 3);

        WishDtoOut dtoOut = update.get().getBody();
        assertEquals(id, dtoOut.getId());
        assertEquals(UPDATED_PRICE, dtoOut.getPrice());
        assertEquals(Currency.uaK, dtoOut.getCurrency());
        assertEquals(UPDATED_NAME, dtoOut.getName());
        assertEquals(UPDATED_DESCRIPTION, dtoOut.getDescription());
        assertEquals(UPDATED_PRIORITY, dtoOut.getPriority());

        Optional<Wish> wishById = wishRepository.findById(id);
        assertTrue(wishById.isPresent());

        Wish wish = wishById.get();
        assertEquals(UPDATED_PRICE, wish.getPrice());
        assertEquals(Currency.uaK, wish.getCurrency());
        assertEquals(UPDATED_NAME, wish.getName());
        assertEquals(UPDATED_DESCRIPTION, wish.getDescription());
        assertEquals(UPDATED_PRIORITY, wish.getPriority());
        assertTrue(wish.getCreatedAt().isBefore(wish.getUpdatedAt()));
    }

    @Test
    void update_InvalidDtoIn() {
        log.info("Test adjustment of wish. Invalid values will be entered in dtoIn.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 3);

        UpdateWishDtoIn dtoIn = createUpdateWishDtoIn(-1L);
        dtoIn.setPrice(null);
        dtoIn.setCurrency(null);
        dtoIn.setName("                 ");
        dtoIn.setDescription("   654 éíáý!?.\\|(){}[];°`~=--0");
        dtoIn.setPriority(null);

        // Execution
        Either<Failure, Success<WishDtoOut>> update = wishService.update(dtoIn);

        // Verification
        assertTrue(update.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, update.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "Id must be a positive number., Price not specified., Name must contain at least 2 characters., The description contains illegal characters: '();=[\\]`{|}~°'., Priority not specified.";
        assertEquals(expectedMessage, update.getLeft().getMessage());
    }

    @Test
    void update_UserNotSignedIn() {
        log.info("Test adjustment of wish. User will not be signed-in.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        Long id = user.getWishList().get(0).getId();
        UpdateWishDtoIn dtoIn = createUpdateWishDtoIn(id);

        // Execution
        Either<Failure, Success<WishDtoOut>> update = wishService.update(dtoIn);

        // Verification
        assertTrue(update.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, update.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, update.getLeft().getMessage());
    }

    @Test
    @Transactional
    void update_UserNotFoundById() {
        log.info("Test adjustment of wish. The user will not be found by id in the database.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setEmail(user.getEmail());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        assertDatabaseSize(1, 3);

        Long id = user.getWishList().get(0).getId();
        UpdateWishDtoIn dtoIn = createUpdateWishDtoIn(id);

        // Execution
        Either<Failure, Success<WishDtoOut>> update = wishService.update(dtoIn);

        // Verification
        assertTrue(update.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, update.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, update.getLeft().getMessage());
    }

    @Test
    @Transactional
    void update_DuplicateWishWithinUser() {
        log.info("Test adjustment of wish. Test of updating the name of the wish to the name of another wish that is already in the database for a particular user.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 3);

        Long id = user.getWishList().get(0).getId();
        UpdateWishDtoIn dtoIn = createUpdateWishDtoIn(id);
        String name = user.getWishList().get(1).getName();
        dtoIn.setName(name);

        // Execution
        Either<Failure, Success<WishDtoOut>> update = wishService.update(dtoIn);

        // Verification
        assertTrue(update.isLeft());
        assertEquals(HttpStatus.CONFLICT, update.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = String.format("Wish '%s' is already used.", name);
        assertEquals(expectedMessage, update.getLeft().getMessage());
    }

    @Test
    @Transactional
    void update_WishNotFoundById() {
        log.info("Test adjustment of wish. The wish according to the specified id will not be found.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 3);

        Long id = user.getWishList().get(2).getId() + 1;
        UpdateWishDtoIn dtoIn = createUpdateWishDtoIn(id);

        // Execution
        Either<Failure, Success<WishDtoOut>> update = wishService.update(dtoIn);

        // Verification
        assertTrue(update.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, update.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "The user does not contain the wish.";
        assertEquals(expectedMessage, update.getLeft().getMessage());
    }

    @Test
    @Transactional
    void delete_Hds() {
        log.info("Test deleting wishes according to id.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 3);

        // Execution
        Long wishId = user.getWishList().get(0).getId();
        Either<Failure, Success<WishDtoOut>> delete = wishService.delete(createDeleteWishDtoIn(wishId));

        // Verification
        assertTrue(delete.isRight());
        assertEquals(HttpStatus.OK, delete.get().getHttpStatus());

        assertDatabaseSize(1, 2);

        WishDtoOut dtoOut = delete.get().getBody();
        assertEquals(wishId, dtoOut.getId());

        Optional<Wish> wishById = wishRepository.findById(wishId);
        assertFalse(wishById.isPresent());
    }

    @Test
    void delete_InvalidDtoIn() {
        log.info("Test deleting wishes according to id. The data in the input dtoIn will not be valid.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 3);

        // Execution
        Long wishId = -1L;
        Either<Failure, Success<WishDtoOut>> delete = wishService.delete(createDeleteWishDtoIn(wishId));

        // Verification
        assertTrue(delete.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, delete.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "Id must be a positive number.";
        assertEquals(expectedMessage, delete.getLeft().getMessage());
    }

    @Test
    void delete_UserNotSignedIn() {
        log.info("Test deleting wishes according to id. The user will not be signed-in.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Execution
        Long wishId = user.getWishList().get(0).getId();
        Either<Failure, Success<WishDtoOut>> delete = wishService.delete(createDeleteWishDtoIn(wishId));

        // Verification
        assertTrue(delete.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, delete.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, delete.getLeft().getMessage());
    }

    @Test
    @Transactional
    void delete_UserNotFoundById() {
        log.info("Test deleting wishes according to id. The user will not be found in the database by its id.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setEmail(user.getEmail());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        assertDatabaseSize(1, 3);

        // Execution
        Long wishId = user.getWishList().get(0).getId();
        Either<Failure, Success<WishDtoOut>> delete = wishService.delete(createDeleteWishDtoIn(wishId));

        // Verification
        assertTrue(delete.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, delete.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, delete.getLeft().getMessage());
    }

    @Test
    @Transactional
    void delete_UserDoesNotHaveSpecifiedWish() {
        log.info("Test deleting wishes according to id. User does not include wish with id to delete.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 3);

        // Execution
        Long wishId = user.getWishList().get(2).getId() + 1;
        Either<Failure, Success<WishDtoOut>> delete = wishService.delete(createDeleteWishDtoIn(wishId));

        // Verification
        assertTrue(delete.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, delete.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "The user does not have a specified wish.";
        assertEquals(expectedMessage, delete.getLeft().getMessage());
    }

    @Test
    @Transactional
    void deleteAll_Hds() {
        log.info("Test deleting all wishes of signed-in user.");

        // Data preparation
        User user1 = createUser(true);
        userRepository.save(user1);
        User user2 = createUser(true);
        user2.setEmail(user2.getEmail() + "2");
        userRepository.save(user2);

        // Sign-in user
        signInUser(user1);

        assertDatabaseSize(2, 6);

        // Execution
        Either<Failure, Success<DeleteAllWishesDtoOut>> deleteAll = wishService.deleteAll();

        // Verification
        assertTrue(deleteAll.isRight());
        assertEquals(HttpStatus.OK, deleteAll.get().getHttpStatus());

        assertDatabaseSize(2, 3);

        DeleteAllWishesDtoOut dtoOut = deleteAll.get().getBody();
        String expectedMessage = "All wishes have been successfully deleted.";
        assertEquals(expectedMessage, dtoOut.getMessage());

        // Verification of delete wishes of correct signed-in user
        Optional<User> user1ById = userRepository.findById(user1.getId());
        Optional<User> user2ById = userRepository.findById(user2.getId());

        assertTrue(user1ById.isPresent());
        assertTrue(user2ById.isPresent());

        User tmpUser1 = user1ById.get();
        User tmpUser2 = user2ById.get();

        assertThat(tmpUser1.getWishList(), empty());
        assertThat(tmpUser2.getWishList(), hasSize(3));
    }

    @Test
    @Transactional
    void deleteAll_Hds_NoWishesInDb() {
        log.info("Test deleting all wishes of signed-in user. There will be no wishes in the database (no user created them).");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 0);

        // Execution
        Either<Failure, Success<DeleteAllWishesDtoOut>> deleteAll = wishService.deleteAll();

        // Verification
        assertTrue(deleteAll.isRight());
        assertEquals(HttpStatus.OK, deleteAll.get().getHttpStatus());

        assertDatabaseSize(1, 0);

        DeleteAllWishesDtoOut dtoOut = deleteAll.get().getBody();
        String expectedMessage = "All wishes have been successfully deleted.";
        assertEquals(expectedMessage, dtoOut.getMessage());

        // Verification of delete wishes of correct signed-in user
        Optional<User> userById = userRepository.findById(user.getId());
        assertTrue(userById.isPresent());

        User tmpUser = userById.get();
        assertNull(tmpUser.getWishList());
    }

    @Test
    void deleteAll_UserNotSignedIn() {
        log.info("Test deleting all wishes of signed-in user. No user whose wishes should be deleted will be signed-in.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Execution
        Either<Failure, Success<DeleteAllWishesDtoOut>> deleteAll = wishService.deleteAll();

        // Verification
        assertTrue(deleteAll.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, deleteAll.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, deleteAll.getLeft().getMessage());
    }

    @Test
    void deleteAll_UserNotFoundById() {
        log.info("Test deleting all wishes of signed-in user. The signed-in user ID in the Spring context will not be found in the database.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setEmail(user.getEmail());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        assertDatabaseSize(1, 3);

        // Execution
        Either<Failure, Success<DeleteAllWishesDtoOut>> deleteAll = wishService.deleteAll();

        // Verification
        assertTrue(deleteAll.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, deleteAll.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, deleteAll.getLeft().getMessage());
    }

    @Test
    void getList_Hds_AscByPriority() {
        log.info("Test of obtaining a wish list (overview) of signed-in user.");

        // Data preparation
        User user1 = createUser(true);
        userRepository.save(user1);
        User user2 = createUser(true);
        user2.setEmail(user2.getEmail() + "2");
        userRepository.save(user2);

        // Sign-in user
        signInUser(user1);

        assertDatabaseSize(2, 6);

        WishListDtoIn dtoIn = createWishListDtoIn(0, 3);

        // Execution
        Either<Failure, Success<WishListDtoOut>> list = wishService.getList(dtoIn);

        // Verification
        assertTrue(list.isRight());
        assertEquals(HttpStatus.OK, list.get().getHttpStatus());

        assertDatabaseSize(2, 6);

        WishListDtoOut dtoOut = list.get().getBody();

        assertEquals(3, dtoOut.getCount());
        assertThat(dtoOut.getWishList(), hasSize(3));

        WishDtoOut wishDtoOut1 = dtoOut.getWishList().get(0);
        assertWishDtoOut(wishDtoOut1, BigDecimal.valueOf(0L), "Wish 0", 1);

        WishDtoOut wishDtoOut2 = dtoOut.getWishList().get(1);
        assertWishDtoOut(wishDtoOut2, BigDecimal.valueOf(1000L), "Wish 1", 2);

        WishDtoOut wishDtoOut3 = dtoOut.getWishList().get(2);
        assertWishDtoOut(wishDtoOut3, BigDecimal.valueOf(2000L), "Wish 2", 3);
    }

    @Test
    void getList_Hds_DescByName() {
        log.info("Test of obtaining a wish list (overview) of signed-in user.");

        // Data preparation
        User user1 = createUser(true);
        userRepository.save(user1);
        User user2 = createUser(true);
        user2.setEmail(user2.getEmail() + "2");
        userRepository.save(user2);

        // Sign-in user
        signInUser(user1);

        assertDatabaseSize(2, 6);

        WishListDtoIn dtoIn = createWishListDtoIn(0, 3);
        dtoIn.setOrder(ORDER_DESC);
        dtoIn.setOrderBy(ORDER_BY_NAME);

        // Execution
        Either<Failure, Success<WishListDtoOut>> list = wishService.getList(dtoIn);

        // Verification
        assertTrue(list.isRight());
        assertEquals(HttpStatus.OK, list.get().getHttpStatus());

        assertDatabaseSize(2, 6);

        WishListDtoOut dtoOut = list.get().getBody();

        assertEquals(3, dtoOut.getCount());
        assertThat(dtoOut.getWishList(), hasSize(3));

        WishDtoOut wishDtoOut1 = dtoOut.getWishList().get(0);
        assertWishDtoOut(wishDtoOut1, BigDecimal.valueOf(2000L), "Wish 2", 3);

        WishDtoOut wishDtoOut2 = dtoOut.getWishList().get(1);
        assertWishDtoOut(wishDtoOut2, BigDecimal.valueOf(1000L), "Wish 1", 2);

        WishDtoOut wishDtoOut3 = dtoOut.getWishList().get(2);
        assertWishDtoOut(wishDtoOut3, BigDecimal.valueOf(0L), "Wish 0", 1);
    }

    @Test
    void getList_Hds_AscByName_EmptyDatabase() {
        log.info("Test of obtaining a wish list (overview) of signed-in user. There will be no signed-in user wishes (none created) in the database.");

        // Data preparation
        User user1 = createUser();
        userRepository.save(user1);
        User user2 = createUser(true);
        user2.setEmail(user2.getEmail() + "2");
        userRepository.save(user2);

        // Sign-in user
        signInUser(user1);

        assertDatabaseSize(2, 3);

        WishListDtoIn dtoIn = createWishListDtoIn(0, 3);
        dtoIn.setOrderBy(ORDER_BY_NAME);

        // Execution
        Either<Failure, Success<WishListDtoOut>> list = wishService.getList(dtoIn);

        // Verification
        assertTrue(list.isRight());
        assertEquals(HttpStatus.OK, list.get().getHttpStatus());

        assertDatabaseSize(2, 3);

        WishListDtoOut dtoOut = list.get().getBody();

        assertEquals(0, dtoOut.getCount());
        assertThat(dtoOut.getWishList(), empty());
    }

    @Test
    void getList_InvalidDtoIn() {
        log.info("Test of obtaining a wish list (overview) of signed-in user. Invalid values will be specified in the dtoIn input object.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        signInUser(user);

        assertDatabaseSize(1, 3);

        WishListDtoIn dtoIn = createWishListDtoIn(0, 3);
        dtoIn.setPage(-1);
        dtoIn.setSize(null);
        dtoIn.setOrder("      ");
        dtoIn.setOrderBy("unknown column");

        // Execution
        Either<Failure, Success<WishListDtoOut>> list = wishService.getList(dtoIn);

        // Verification
        assertTrue(list.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, list.getLeft().getHttpStatus());

        String expectedMessage = "Page index must be zero or greater., The number of items per page was not specified., The order of items in which they are to be sorted (ascending or descending) has not been defined., The values can only be sorted by the following columns [name, priority].";
        assertEquals(expectedMessage, list.getLeft().getMessage());

        assertDatabaseSize(1, 3);
    }

    @Test
    void getList_NoUserSignedIn() {
        log.info("Test of obtaining a wish list (overview) of signed-in user. No user will be signed-in (permission error), moreover, it would not be possible to load user-specific wishes.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        WishListDtoIn dtoIn = createWishListDtoIn(0, 3);

        // Execution
        Either<Failure, Success<WishListDtoOut>> list = wishService.getList(dtoIn);

        // Verification
        assertTrue(list.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, list.getLeft().getHttpStatus());

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, list.getLeft().getMessage());

        assertDatabaseSize(1, 3);
    }

    @Test
    void getList_UserNotFoundById() {
        log.info("Test of obtaining a wish list (overview) of signed-in user. The user will not be found in the database according to his id obtained from the Spring context (token).");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setEmail(user.getEmail());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        assertDatabaseSize(1, 3);

        WishListDtoIn dtoIn = createWishListDtoIn(1, 1);

        // Execution
        Either<Failure, Success<WishListDtoOut>> list = wishService.getList(dtoIn);

        // Verification
        assertTrue(list.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, list.getLeft().getHttpStatus());

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, list.getLeft().getMessage());

        assertDatabaseSize(1, 3);
    }

    @Test
    @Transactional
    void changePriority_Hds() {
        log.info("Test priority change of wishes listed in dtoIn.");

        // Data preparation
        User user = createUser(true);
        // Setting priorities 5, 6, 7
        setDefaultWishPriorities(user.getWishList());
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        List<Long> wishesIdList = getWishesIdList(user.getWishList());
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(wishesIdList);

        // Sign-in user
        signInUser(user);

        // Execution
        Either<Failure, Success<ChangePriorityDtoOut>> changePriority = wishService.changePriority(dtoIn);

        // Verification
        assertTrue(changePriority.isRight());
        assertEquals(HttpStatus.OK, changePriority.get().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "Successfully adjusted priority for requested wishes.";
        assertEquals(expectedMessage, changePriority.get().getBody().getMessage());

        // Check that there are wishes with priorities 1, 2, 3 in the database
        Optional<Wish> wishById1 = wishRepository.findById(dtoIn.getChangePriorityWishInfoList().get(0).getId());
        Optional<Wish> wishById2 = wishRepository.findById(dtoIn.getChangePriorityWishInfoList().get(1).getId());
        Optional<Wish> wishById3 = wishRepository.findById(dtoIn.getChangePriorityWishInfoList().get(2).getId());

        assertTrue(wishById1.isPresent());
        assertTrue(wishById2.isPresent());
        assertTrue(wishById3.isPresent());

        Wish wish1 = wishById1.get();
        Wish wish2 = wishById2.get();
        Wish wish3 = wishById3.get();

        assertEquals(1, wish1.getPriority());
        assertEquals(2, wish2.getPriority());
        assertEquals(3, wish3.getPriority());

        assertTrue(wish1.getCreatedAt().isBefore(wish1.getUpdatedAt()));
        assertTrue(wish2.getCreatedAt().isBefore(wish2.getUpdatedAt()));
        assertTrue(wish3.getCreatedAt().isBefore(wish3.getUpdatedAt()));
    }

    @Test
    void changePriority_InvalidDtoIn() {
        log.info("Test priority change of wishes listed in dtoIn. Invalid values will be entered in the dtoIn object.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        // Sign-in user
        signInUser(user);

        List<Long> wishesIdList = getWishesIdList(user.getWishList());
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(wishesIdList);
        dtoIn.getChangePriorityWishInfoList().get(0).setId(null);

        // Execution
        Either<Failure, Success<ChangePriorityDtoOut>> changePriority = wishService.changePriority(dtoIn);

        // Verification
        assertTrue(changePriority.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, changePriority.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = String.format("The change for wish '%s' is not valid. %s", "ChangePriorityWishInfo 1", "Id not specified.");
        assertEquals(expectedMessage, changePriority.getLeft().getMessage());
    }

    @Test
    void changePriority_UserNotSignedIn() {
        log.info("Test priority change of wishes listed in dtoIn. The user will not be signed-in, but this should never occur, only the signed-in user can call the method.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        List<Long> wishesIdList = getWishesIdList(user.getWishList());
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(wishesIdList);

        // Execution
        Either<Failure, Success<ChangePriorityDtoOut>> changePriority = wishService.changePriority(dtoIn);

        // Verification
        assertTrue(changePriority.isLeft());
        assertEquals(HttpStatus.UNAUTHORIZED, changePriority.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "No user signed-in.";
        assertEquals(expectedMessage, changePriority.getLeft().getMessage());
    }

    @Test
    void changePriority_UserNotFoundById() {
        log.info("Test priority change of wishes listed in dtoIn. The signed-in user will not be found in the database according to the id obtained from the Spring context (token). Application will not be able to verify the identity of the signed-in user.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        List<Long> wishesIdList = getWishesIdList(user.getWishList());
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(wishesIdList);

        // Sign-in user
        User user1 = new User();
        user1.setId(user.getId() + 1);
        user1.setEmail(user.getEmail());
        user1.setRoles(user.getRoles());
        signInUser(user1);

        // Execution
        Either<Failure, Success<ChangePriorityDtoOut>> changePriority = wishService.changePriority(dtoIn);

        // Verification
        assertTrue(changePriority.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, changePriority.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "User not found.";
        assertEquals(expectedMessage, changePriority.getLeft().getMessage());
    }

    @Test
    @Transactional
    void changePriority_UserDoesNotHasAnyWishInDb() {
        log.info("Test priority change of wishes listed in dtoIn. The signed-in user will not have any wishes created in the database. Therefore, it would not be possible to change the priority specified by values in dtoIn. For example, it may be another user's wish ids.");

        // Data preparation
        User user = createUser();
        userRepository.save(user);

        assertDatabaseSize(1, 0);

        List<Long> wishesIdList = new ArrayList<>(Arrays.asList(1L, 2L, 3L));
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(wishesIdList);

        // Sign-in user
        signInUser(user);

        // Execution
        Either<Failure, Success<ChangePriorityDtoOut>> changePriority = wishService.changePriority(dtoIn);

        // Verification
        assertTrue(changePriority.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, changePriority.getLeft().getHttpStatus());

        assertDatabaseSize(1, 0);

        String expectedMessage = "You have no wishes to change the priority.";
        assertEquals(expectedMessage, changePriority.getLeft().getMessage());
    }

    @Test
    @Transactional
    void changePriority_UserDoesNotHasAllWishesSpecifiedInDtoIn() {
        log.info("Test priority change of wishes listed in dtoIn. The dtoIn will contain a list of wishes id that the user does not have in the database. Therefore, application will not be able to change their priority. These may be the wishes of another user.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        assertDatabaseSize(1, 3);

        Long lastWishId = user.getWishList().get(2).getId();
        List<Long> wishesIdList = new ArrayList<>(Arrays.asList(lastWishId + 1, lastWishId + 2, lastWishId + 3));
        ChangePriorityDtoIn dtoIn = createChangePriorityDtoIn(wishesIdList);

        // Sign-in user
        signInUser(user);

        // Execution
        Either<Failure, Success<ChangePriorityDtoOut>> changePriority = wishService.changePriority(dtoIn);

        // Verification
        assertTrue(changePriority.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, changePriority.getLeft().getHttpStatus());

        assertDatabaseSize(1, 3);

        String expectedMessage = "The following wishes were not found. ChangePriorityWishInfo 1, ChangePriorityWishInfo 2, ChangePriorityWishInfo 3";
        assertEquals(expectedMessage, changePriority.getLeft().getMessage());
    }
}
