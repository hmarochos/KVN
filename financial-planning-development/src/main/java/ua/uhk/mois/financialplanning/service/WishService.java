package ua.uhk.mois.financialplanning.service;

import ua.uhk.mois.financialplanning.model.dto.wish.ChangePriorityDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.ChangePriorityDtoOut;
import ua.uhk.mois.financialplanning.model.dto.wish.CreateWishDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.DeleteAllWishesDtoOut;
import ua.uhk.mois.financialplanning.model.dto.wish.DeleteWishDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.UpdateWishDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.WishDtoOut;
import ua.uhk.mois.financialplanning.model.dto.wish.WishListDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.WishListDtoOut;
import ua.uhk.mois.financialplanning.response.Failure;
import ua.uhk.mois.financialplanning.response.Success;
import io.vavr.control.Either;

/**
 * @author KVN
 * @since 22.03.2021 15:28
 */

public interface WishService {

    /**
     * Creating a new financial goal (/ wish) that the user wants to achieve.
     *
     * @param dtoIn
     *         data needed to create a goal the user wants to achieve
     *
     * @return right with the target created, otherwise left with information about the error
     */
    Either<Failure, Success<WishDtoOut>> add(CreateWishDtoIn dtoIn);

    /**
     * Edit an existing wish. <br/>
     * <i>By dtoIn.id the existing wish will be searched in the database and its data modified to those in dtoIn.</i>
     *
     * @param dtoIn
     *         new data to store the database
     *
     * @return right if the changes were successfully saved, otherwise left with information about the error
     */
    Either<Failure, Success<WishDtoOut>> update(UpdateWishDtoIn dtoIn);

    /**
     * Delete wish with a specific id owned by the signed-in user.
     *
     * @param deleteWishDtoIn
     *         information about the ID of the wish to be deleted
     *
     * @return right with deleted wish, otherwise left with information about the error
     */
    Either<Failure, Success<WishDtoOut>> delete(DeleteWishDtoIn deleteWishDtoIn);

    /**
     * Delete all wishes that the signed-in user owns.
     *
     * @return right if all signed-in user's wishes are safely deleted, otherwise left with information about the error
     */
    Either<Failure, Success<DeleteAllWishesDtoOut>> deleteAll();

    /**
     * Load wish list based on sorting and paging requirements.
     *
     * @param dtoIn
     *         requirements for the wish list to be loaded (number, sort, page, etc.)
     *
     * @return right with loaded wish list according to the above requirements, otherwise left with information about
     * the error
     */
    Either<Failure, Success<WishListDtoOut>> getList(WishListDtoIn dtoIn);

    /**
     * Change the priority of the wishes listed in dtoIn.
     *
     * @param dtoIn
     *         wishes to change their priority and their new priority to set
     *
     * @return right with information that wishes have been successfully prioritized, otherwise left with error
     * information
     */
    Either<Failure, Success<ChangePriorityDtoOut>> changePriority(ChangePriorityDtoIn dtoIn);
}
