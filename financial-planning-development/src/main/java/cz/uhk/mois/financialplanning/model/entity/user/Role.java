package cz.uhk.mois.financialplanning.model.entity.user;

/**
 * @author Jan Krunčík
 * @since 15.03.2020 19:45
 */

public enum Role {

    /**
     * BFU.
     */
    USER,

    /**
     * User with this role can create a transaction.
     */
    ADMIN

}
