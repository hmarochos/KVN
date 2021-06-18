package cz.uhk.mois.financialplanning.service;

import cz.uhk.mois.financialplanning.model.dto.wish.ChangePriorityDtoIn;
import cz.uhk.mois.financialplanning.model.dto.wish.ChangePriorityDtoOut;
import cz.uhk.mois.financialplanning.model.dto.wish.ChangePriorityWishInfo;
import cz.uhk.mois.financialplanning.model.dto.wish.CreateWishDtoIn;
import cz.uhk.mois.financialplanning.model.dto.wish.DeleteAllWishesDtoOut;
import cz.uhk.mois.financialplanning.model.dto.wish.DeleteWishDtoIn;
import cz.uhk.mois.financialplanning.model.dto.wish.UpdateWishDtoIn;
import cz.uhk.mois.financialplanning.model.dto.wish.WishDtoOut;
import cz.uhk.mois.financialplanning.model.dto.wish.WishListDtoIn;
import cz.uhk.mois.financialplanning.model.dto.wish.WishListDtoOut;
import cz.uhk.mois.financialplanning.model.entity.user.User;
import cz.uhk.mois.financialplanning.model.entity.wish.Wish;
import cz.uhk.mois.financialplanning.repository.WishRepository;
import cz.uhk.mois.financialplanning.response.Failure;
import cz.uhk.mois.financialplanning.response.FailureSupport;
import cz.uhk.mois.financialplanning.response.Success;
import cz.uhk.mois.financialplanning.service.support.AuthSupport;
import cz.uhk.mois.financialplanning.service.support.UserSupport;
import cz.uhk.mois.financialplanning.validation.wish.ChangePriorityDtoInValidator;
import cz.uhk.mois.financialplanning.validation.wish.CreateWishDtoInValidator;
import cz.uhk.mois.financialplanning.validation.wish.DeleteWishDtoInValidator;
import cz.uhk.mois.financialplanning.validation.wish.UpdateWishDtoInValidator;
import cz.uhk.mois.financialplanning.validation.wish.WishListDtoInValidator;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cz.uhk.mois.financialplanning.service.support.AuthSupport.getSignedInUser;

/**
 * @author Jan Krunčík
 * @since 22.03.2020 15:28
 */

@Component
@Log4j2
public class WishServiceImpl implements WishService {

    private final WishRepository wishRepository;
    private final UserSupport userSupport;
    private final ModelMapper modelMapper;
    private final Clock clock;

    public WishServiceImpl(WishRepository wishRepository, UserSupport userSupport, ModelMapper modelMapper, Clock clock) {
        this.wishRepository = wishRepository;
        this.userSupport = userSupport;
        this.modelMapper = modelMapper;
        this.clock = clock;
    }

    /**
     * Check if the appropriate name of the wish / goal has not been used yet.
     *
     * @param user
     *         the user to determine if he or she has not yet created the same wish (/ goal)
     * @param wishName
     *         name of the goal / wish
     *
     * @return right with the user, if the user has not yet created a wish, otherwise left with information about the
     * conflict (duplicate)
     */
    private static Either<Failure, User> checkUniqueWishWithinUser(User user, String wishName) {
        if (user.getWishList() == null || user.getWishList().isEmpty()) {
            return Either.right(user);
        }

        Optional<Wish> wishInList = isWishInList(user.getWishList(), wishName);
        if (wishInList.isPresent()) {
            String message = String.format("Wish '%s' is already used.", wishName);
            log.error(message);
            return Either.left(FailureSupport.createFailure(HttpStatus.CONFLICT, message));
        }

        return Either.right(user);
    }

    /**
     * Checking whether a wish with a specific name within a user has been used yet. But wish with a particular id will
     * be considered. When editing, the name of the wish does not need to be edited and it could still duplicate one
     * item. <br/>
     * <i>It is necessary to test whether it is not an update of the same wish, then the name may match.</i>
     *
     * @param user
     *         the user to determine if he or she has not yet created the same wish (/ goal)
     * @param dtoIn
     *         modified wish for which a duplicate name should be found, but wishes with the same id will not be
     *         considered
     *
     * @return right with the wish, if the user has not yet created a wish, otherwise left with information about the
     * conflict (duplicate)
     */
    private static Either<Failure, UpdateWishDtoIn> checkUniqueWishWithinUserIgnoreWishWithId(User user, UpdateWishDtoIn dtoIn) {
        if (user.getWishList() == null || user.getWishList().isEmpty()) {
            return Either.right(dtoIn);
        }

        Optional<Wish> wishInList = isWishInList(user.getWishList(), dtoIn.getName());

        if (!wishInList.isPresent()) {
            return Either.right(dtoIn);
        }

        Wish wish = wishInList.get();
        if (wish.getId().equals(dtoIn.getId())) {
            return Either.right(dtoIn);
        }

        String message = String.format("Wish '%s' is already used.", dtoIn.getName());
        log.error(message);
        return Either.left(FailureSupport.createFailure(HttpStatus.CONFLICT, message));
    }

    private static Optional<Wish> isWishInList(List<Wish> wishList, String wishName) {
        return wishList.parallelStream()
                       .filter(wish -> wish.getName().equalsIgnoreCase(wishName))
                       .findFirst();
    }

    private static Failure saveWishFailure(Throwable throwable, Wish wish) {
        String logMsg = String.format("There was an error trying to save the goal (/ wish) to the database. %s", wish);
        log.error(logMsg, throwable);
        String message = "There was an error trying to save the goal (/ wish) to the database.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private static Success<WishDtoOut> wishSuccessDtoOut(WishDtoOut dtoOut, String msgOperation, HttpStatus httpStatus) {
        log.info("Wish successfully {}. {}", msgOperation, dtoOut);
        return Success.<WishDtoOut>builder()
                .httpStatus(httpStatus)
                .body(dtoOut)
                .build();
    }

    private static Success<WishListDtoOut> getListSuccessDtoOut(WishListDtoOut dtoOut) {
        log.info("WishListDtoOut successfully obtained. {}", dtoOut);
        return Success.<WishListDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    /**
     * Finding out whether the user has a wish with the appropriate id, if yes, the wish returns.
     *
     * @param user
     *         the user who is determined to have a wish with id wishId
     *
     * @return right with the wish with id wishId, if the user contains it, otherwise left with the information that the
     * user is not making wishes with the given id
     */
    private static Either<Failure, Wish> getWishToUpdateFromUser(User user, Long wishId) {
        Optional<Wish> optionalWish = user.getWishList().stream()
                                          .filter(wish -> wish.getId().equals(wishId))
                                          .findFirst();

        if (optionalWish.isPresent()) {
            return Either.right(optionalWish.get());
        }

        String logMessage = String.format("User does not include wish with id '%s'. %s", wishId, user);
        log.error(logMessage);
        return Either.left(FailureSupport.createFailure(HttpStatus.NOT_FOUND, "The user does not contain the wish."));
    }

    private static Failure loadWishListFailure(Throwable throwable, WishListDtoIn dtoIn) {
        String logMsg = String.format("There was an error trying to load the wish list with following requirements. %s", dtoIn);
        log.error(logMsg, throwable);
        String message = "There was an error trying to load the wish list.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private static Failure deleteAllUserWishesFailure(Throwable throwable, User user) {
        String logMsg = String.format("There was an error trying to delete all wishes of the signed-in user %s.", user);
        log.error(logMsg, throwable);
        String message = "There was an error trying to delete all wishes of the signed-in user.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private static DeleteAllWishesDtoOut createDeleteAllWishesDtoOut() {
        return DeleteAllWishesDtoOut.builder()
                                    .message("All wishes have been successfully deleted.")
                                    .build();
    }

    private static Success<DeleteAllWishesDtoOut> deleteAllSuccessDtoOut(DeleteAllWishesDtoOut dtoOut) {
        log.info("All signed-in user wishes have been successfully deleted. {}", dtoOut);
        return Success.<DeleteAllWishesDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    private static Either<Failure, Wish> getWishByIdFromUser(User user, DeleteWishDtoIn dtoIn) {
        if (user.getWishList() != null) {
            Optional<Wish> optionalWish = user.getWishList().stream()
                                              .filter(wish -> wish.getId().equals(dtoIn.getId()))
                                              .findFirst();
            if (optionalWish.isPresent()) {
                return Either.right(optionalWish.get());
            }
        }
        return Either.left(FailureSupport.createFailure(HttpStatus.NOT_FOUND, "The user does not have a specified wish."));
    }

    private static Failure deleteWishByIdFailure(Throwable throwable, Wish wish) {
        String logMsg = String.format("There was an error trying to delete wish. %s", wish);
        log.error(logMsg, throwable);
        String message = "There was an error trying to delete wish.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * Check that the user has (stored in the database) any wishes to be prioritized (specified in dtoIn).
     *
     * @param user
     *         the user to determine if it contains wishes that are in dtoIn
     * @param dtoIn
     *         a list of wishes to change the priority, here to check whether the user has all the wishes listed here
     *         (in dtoIn)
     *
     * @return right with the user (and his / her wishes) if the user contains all the wishes specified in dtoIn,
     * otherwise left with information about the error (validation violation)
     */
    private static Either<Failure, User> checkUserHasAllWishesInDtoIn(User user, ChangePriorityDtoIn dtoIn) {
        List<Wish> wishList = user.getWishList();
        if (wishList == null || wishList.isEmpty()) {
            String logMessage = String.format("User has no wishes. %s, %s", user, dtoIn);
            log.error(logMessage);
            return Either.left(FailureSupport.createFailure(HttpStatus.NOT_FOUND, "You have no wishes to change the priority."));
        }

        StringBuilder missingWishesBuilder = new StringBuilder("The following wishes were not found. ");

        boolean allWishesFound = true;
        for (ChangePriorityWishInfo wishInfo : dtoIn.getChangePriorityWishInfoList()) {
            if (!hasUserWishWithId(wishList, wishInfo.getId())) {
                allWishesFound = false;
                missingWishesBuilder.append(wishInfo.getName())
                                    .append(", ");
            }
        }

        if (allWishesFound) {
            return Either.right(user);
        }

        missingWishesBuilder.setLength(missingWishesBuilder.length() - 2);

        String logMessage = String.format("In dtoIn there are id of wishes that the user does not have stored in the database. %s", missingWishesBuilder);
        log.error(logMessage);
        return Either.left(FailureSupport.createFailure(HttpStatus.NOT_FOUND, missingWishesBuilder.toString()));
    }

    private static Failure saveWishListFailure(Throwable throwable, List<Wish> wishList) {
        String logMsg = String.format("There was an error trying to save the wish list with adjusted priorities. %s", wishList);
        log.error(logMsg, throwable);
        String message = "There was an error trying to save the wish list with adjusted priorities.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private static ChangePriorityDtoOut createChangePriorityDtoOut() {
        ChangePriorityDtoOut dtoOut = new ChangePriorityDtoOut();
        dtoOut.setMessage("Successfully adjusted priority for requested wishes.");
        return dtoOut;
    }

    private static Success<ChangePriorityDtoOut> changePrioritySuccessDtoOut(ChangePriorityDtoOut dtoOut) {
        log.info("Successfully adjusted priority for requested wishes. {}", dtoOut);
        return Success.<ChangePriorityDtoOut>builder()
                .httpStatus(HttpStatus.OK)
                .body(dtoOut)
                .build();
    }

    private static boolean hasUserWishWithId(List<Wish> wishList, Long id) {
        return wishList.stream()
                       .anyMatch(wish -> wish.getId().equals(id));
    }

    /**
     * Get (/ find) wish by id wishId.
     *
     * @param wishId
     *         Id of the wish to be filtered
     * @param wishList
     *         a list to filter out specific wishes
     *
     * @return found (/ filtered) wishes according to wishId, the wish will always be found according to id, it is
     * already verified that such a wish exists in cz.uhk.mois.financialplanning.service.WishServiceImpl#checkUserHasAllWishesInDtoIn(cz.uhk.mois.financialplanning.model.entity.user.User,
     * cz.uhk.mois.financialplanning.model.dto.wish.ChangePriorityDtoIn)
     */
    private static Wish getWishById(Long wishId, List<Wish> wishList) {
        return wishList.parallelStream()
                       .filter(wish -> wish.getId().equals(wishId))
                       .findFirst()
                       .orElseGet(Wish::new);
    }

    /**
     * Finding a priority that is next in order with respect to the existing wishes.
     *
     * @param user
     *         the user to which a new wish is to be added, the following priority for the new item is determined from
     *         his / her existing wishes
     *
     * @return priority (which is next in order) for new wishes
     */
    private static Integer findNextPriority(User user) {
        if (user.getWishList() == null || user.getWishList().isEmpty()) {
            return 1;
        }

        Comparator<Wish> compareByPriority = Comparator.comparing(Wish::getPriority);
        user.getWishList().sort(compareByPriority);
        Integer lastPriority = user.getWishList().get(user.getWishList().size() - 1).getPriority();
        return lastPriority + 1;
    }

    @Override
    public Either<Failure, Success<WishDtoOut>> add(CreateWishDtoIn dtoIn) {
        log.info("Creating a new financial goal (/ wish). {}", dtoIn);

        return CreateWishDtoInValidator.validate(dtoIn)
                                       .mapError(validationViolations -> FailureSupport.validationFailure(validationViolations, "CreateWishDtoIn"))
                                       .toEither()
                                       .flatMap(validatedDtoIn -> getSignedInUser()
                                                        .flatMap(AuthSupport::checkSignedInUser)
                                                        .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                                        .flatMap(userSupport::findUserById)
                                                        .flatMap(user -> checkUniqueWishWithinUser(user, validatedDtoIn.getName())
                                                                .map(WishServiceImpl::findNextPriority)
                                                                .map(priority -> createWish(validatedDtoIn, user, priority)))
                                               )
                                       .flatMap(this::saveWish)
                                       .map(this::convertToDto)
                                       .map(wishDtoOut -> wishSuccessDtoOut(wishDtoOut, "created", HttpStatus.CREATED));
    }

    @Override
    public Either<Failure, Success<WishDtoOut>> update(UpdateWishDtoIn dtoIn) {
        log.info("Adjust wish (financial goal). {}", dtoIn);

        return UpdateWishDtoInValidator.validate(dtoIn)
                                       .mapError(validationViolations -> FailureSupport.validationFailure(validationViolations, "UpdateWishDtoIn"))
                                       .toEither()
                                       .flatMap(validatedDtoIn -> getSignedInUser()
                                               .flatMap(AuthSupport::checkSignedInUser)
                                               .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                               .flatMap(userSupport::findUserById)
                                               .flatMap(user -> checkUniqueWishWithinUserIgnoreWishWithId(user, validatedDtoIn)
                                                       .flatMap(uniqueWishDtoIn -> getWishToUpdateFromUser(user, uniqueWishDtoIn.getId())))
                                               .map(wish -> updateWish(wish, validatedDtoIn)))
                                       .flatMap(this::saveWish)
                                       .map(this::convertToDto)
                                       .map(wishDtoOut -> wishSuccessDtoOut(wishDtoOut, "updated", HttpStatus.OK));
    }

    @Override
    public Either<Failure, Success<WishDtoOut>> delete(DeleteWishDtoIn dtoIn) {
        log.info("Delete wish by id. {}", dtoIn);

        return DeleteWishDtoInValidator.validate(dtoIn)
                                       .mapError(validationViolation -> FailureSupport.validationFailure(validationViolation, "DeleteWishDtoIn"))
                                       .toEither()
                                       .flatMap(validatedDtoIn -> getSignedInUser()
                                               .flatMap(AuthSupport::checkSignedInUser)
                                               .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                               .flatMap(userSupport::findUserById)
                                               .flatMap(user -> getWishByIdFromUser(user, validatedDtoIn)
                                                       .flatMap(wish -> deleteWishById(user, wish))))
                                       .map(this::convertToDto)
                                       .map(dtoOut -> wishSuccessDtoOut(dtoOut, "deleted", HttpStatus.OK));
    }

    @Transactional
    @Override
    public Either<Failure, Success<DeleteAllWishesDtoOut>> deleteAll() {
        log.info("Delete all signed-in user wishes.");

        return getSignedInUser()
                .flatMap(AuthSupport::checkSignedInUser)
                .flatMap(AuthSupport::getIdFromSignedInUserToken)
                .flatMap(userSupport::findUserById)
                .flatMap(this::deleteAllUserWishes)
                .map(wishList -> createDeleteAllWishesDtoOut())
                .map(WishServiceImpl::deleteAllSuccessDtoOut);
    }

    @Override
    public Either<Failure, Success<WishListDtoOut>> getList(WishListDtoIn dtoIn) {
        log.info("Load wish list based on sorting and paging requirements. {}", dtoIn);

        return WishListDtoInValidator.validate(dtoIn)
                                     .mapError(validationViolations -> FailureSupport.validationFailure(validationViolations, "WishListDtoIn"))
                                     .toEither()
                                     .flatMap(validatedDtoIn -> getSignedInUser()
                                             .flatMap(AuthSupport::checkSignedInUser)
                                             .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                             .flatMap(userSupport::findUserById)
                                             .flatMap(user -> loadWishList(validatedDtoIn, user.getId())))
                                     .map(this::createWishListDtoOut)
                                     .map(WishServiceImpl::getListSuccessDtoOut);
    }

    @Override
    public Either<Failure, Success<ChangePriorityDtoOut>> changePriority(ChangePriorityDtoIn dtoIn) {
        log.info("Change the priority of the following wishes. {}", dtoIn);

        return ChangePriorityDtoInValidator.validate(dtoIn)
                                           .mapError(validationViolations -> FailureSupport.validationFailure(validationViolations, "ChangePriorityDtoIn"))
                                           .toEither()
                                           .flatMap(validatedDtoIn -> getSignedInUser()
                                                   .flatMap(AuthSupport::checkSignedInUser)
                                                   .flatMap(AuthSupport::getIdFromSignedInUserToken)
                                                   .flatMap(userSupport::findUserById)
                                                   .flatMap(user -> checkUserHasAllWishesInDtoIn(user, validatedDtoIn))
                                                   .map(user -> changeWishesPriority(user, validatedDtoIn)))
                                           .flatMap(user -> saveWishList(user.getWishList()))
                                           .map(user -> createChangePriorityDtoOut())
                                           .map(WishServiceImpl::changePrioritySuccessDtoOut);
    }

    private User changeWishesPriority(User user, ChangePriorityDtoIn dtoIn) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        dtoIn.getChangePriorityWishInfoList()
             .parallelStream()
             .forEach(changePriorityWishInfo -> {
                 Wish wishById = getWishById(changePriorityWishInfo.getId(), user.getWishList());
                 wishById.setPriority(changePriorityWishInfo.getPriority());
                 wishById.setUpdatedAt(now);
             });
        return user;
    }

    private Either<Failure, List<Wish>> saveWishList(List<Wish> wishList) {
        return Try.of(() -> wishRepository.saveAll(wishList))
                  .toEither()
                  .mapLeft(throwable -> saveWishListFailure(throwable, wishList));
    }

    private Either<Failure, Wish> deleteWishById(User user, Wish wish) {
        return Try.of(() -> {
            user.getWishList().remove(wish);
            wishRepository.deleteById(wish.getId());
            return wish;
        })
                  .toEither()
                  .mapLeft(throwable -> deleteWishByIdFailure(throwable, wish));
    }

    private Either<Failure, Void> deleteAllUserWishes(User user) {
        return Try.run(() -> {
            if (user.getWishList() != null && !user.getWishList().isEmpty()) {
                for (Iterator<Wish> iterator = user.getWishList().iterator(); iterator.hasNext(); ) {
                    Wish next = iterator.next();
                    wishRepository.deleteById(next.getId());
                    iterator.remove();
                }
            }
        })
                  .toEither()
                  .mapLeft(throwable -> deleteAllUserWishesFailure(throwable, user));
    }

    /**
     * Retrieve the wish list of a particular user by hist (/ her) id.
     *
     * @param dtoIn
     *         paging and sorting requirements
     * @param userId
     *         id of the user whose wishes are to be retrieved
     *
     * @return right with the user's wishes retrieved by his id, otherwise left with information about the error
     */
    private Either<Failure, Page<Wish>> loadWishList(WishListDtoIn dtoIn, Long userId) {
        return Try.of(() -> {
            Sort.Direction direction = Sort.Direction.valueOf(dtoIn.getOrder());
            PageRequest pageRequest = PageRequest.of(dtoIn.getPage(), dtoIn.getSize(), Sort.by(direction, dtoIn.getOrderBy()));
            return wishRepository.findAllByUserId(userId, pageRequest);
        })
                  .toEither()
                  .mapLeft(throwable -> loadWishListFailure(throwable, dtoIn));
    }

    private WishListDtoOut createWishListDtoOut(Page<Wish> wishPage) {
        List<WishDtoOut> wishDtoOutList = wishPage.getContent().stream()
                                                  .map(this::convertToDto)
                                                  .collect(Collectors.toList());

        return WishListDtoOut.builder()
                             .wishList(wishDtoOutList)
                             .count(wishPage.getTotalElements())
                             .build();
    }

    private Wish createWish(CreateWishDtoIn dtoIn, User user, Integer priority) {
        Wish wish = modelMapper.map(dtoIn, Wish.class);
        wish.setPriority(priority);

        ZonedDateTime now = ZonedDateTime.now(clock);
        wish.setCreatedAt(now);
        wish.setUpdatedAt(now);

        wish.setUser(user);

        if (user.getWishList() == null) {
            user.setWishList(new ArrayList<>());
        }

        user.getWishList().add(wish);

        return wish;
    }

    private Either<Failure, Wish> saveWish(Wish wish) {
        return Try.of(() -> wishRepository.save(wish))
                  .toEither()
                  .mapLeft(throwable -> saveWishFailure(throwable, wish));
    }

    private WishDtoOut convertToDto(Wish wish) {
        return modelMapper.map(wish, WishDtoOut.class);
    }

    private Wish updateWish(Wish wish, UpdateWishDtoIn dtoIn) {
        wish.setPrice(dtoIn.getPrice());
        wish.setCurrency(dtoIn.getCurrency());
        wish.setName(dtoIn.getName());
        wish.setDescription(dtoIn.getDescription());
        wish.setPriority(dtoIn.getPriority());

        ZonedDateTime now = ZonedDateTime.now(clock);
        wish.setUpdatedAt(now);

        return wish;
    }
}
