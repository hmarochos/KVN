package ua.uhk.mois.financialplanning.service.support;

import ua.uhk.mois.financialplanning.model.entity.user.User;
import ua.uhk.mois.financialplanning.response.Failure;
import io.vavr.control.Either;

/**
 * @author KVN
 * @since 22.03.2021 20:04
 */

public interface UserSupport {

    /**
     * Load user with id in method parameter from database.
     *
     * @param id
     *         user id according to which the user (database entity / record) is to be read from the database
     *
     * @return right with the loaded user from the database, otherwise left with information about the error
     */
    Either<Failure, User> findUserById(Long id);

    /**
     * Save the user to the database. <br/>
     * <i>This can be to create a new user or edit an existing one.</i>
     *
     * @param user
     *         user to be saved to the database (or its changes)
     *
     * @return right with the saved user (or its changes), otherwise left with information about the error
     */
    Either<Failure, User> saveUser(User user);
}
