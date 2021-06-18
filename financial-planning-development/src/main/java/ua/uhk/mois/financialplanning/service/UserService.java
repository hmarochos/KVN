package ua.uhk.mois.financialplanning.service;

import ua.uhk.mois.financialplanning.model.dto.user.ChangePasswordDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.ChangePasswordDtoOut;
import ua.uhk.mois.financialplanning.model.dto.user.CreateUserDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.DeleteUserDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.ProfileDtoOut;
import ua.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoOut;
import ua.uhk.mois.financialplanning.model.dto.user.UserDtoOut;
import ua.uhk.mois.financialplanning.response.Failure;
import ua.uhk.mois.financialplanning.response.Success;
import io.vavr.control.Either;

/**
 * @author KVN
 * @since 15.03.2021 20:18
 */

public interface UserService {

    /**
     * Create a new user.
     *
     * @param dtoIn
     *         data to create a new user
     *
     * @return right with the created user information, otherwise left with the error information.
     */
    Either<Failure, Success<UserDtoOut>> add(CreateUserDtoIn dtoIn);

    /**
     * Get the signed-in user's profile. <br/>
     * <i>The id of the signed-in user is found (in the Spring context), the user is retrieved from the database, and
     * his data to which he can have access will be returned.</i>
     *
     * @return right s data for the signed-in user profile, otherwise left with information about the error.
     */
    Either<Failure, Success<ProfileDtoOut>> getProfile();

    /**
     * Updating an existing user. <br/>
     * <i>An existing user will be searched by email address.</i>
     * <br/>
     * <i>The signed-in user can only change his own data.</i>
     *
     * @param dtoIn
     *         new data for user
     *
     * @return right with information about the modified user (his data), otherwise left with information about the
     * error
     */
    Either<Failure, Success<UpdateUserDtoOut>> update(UpdateUserDtoIn dtoIn);

    /**
     * Delete an existing user by email address.
     *
     * @param dtoIn
     *         the email address of the user to be deleted
     *
     * @return right with information about the deleted user, otherwise left with information about the error
     */
    Either<Failure, Success<UserDtoOut>> delete(DeleteUserDtoIn dtoIn);

    /**
     * Change the password of the signed-in user. <br/>
     * <i>The signed-in user can only change his password and the data in dtoIn must match the signed-in user.</i>
     *
     * @param dtoIn
     *         data needed to change the password
     *
     * @return right with information about successfully changed password, otherwise left with information about error
     */
    Either<Failure, Success<ChangePasswordDtoOut>> changePassword(ChangePasswordDtoIn dtoIn);
}
