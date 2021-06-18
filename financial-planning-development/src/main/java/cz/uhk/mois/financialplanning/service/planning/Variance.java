package cz.uhk.mois.financialplanning.service.planning;

/**
 * To set the difference for saving calculation. <br/>
 * <i>For example, a user would expect, for example, a 2.5% higher monthly earnings than he / she specified. The
 * monthly plan will be calculated for this.</i>
 *
 * @author Jan Krunčík
 * @since 11.04.2020 6:16
 */

public enum Variance {

    TWO_FIVE_PERCENT_LESS,
    FIVE_PERCENT_LESS,
    ZERO,
    TWO_FIVE_PERCENT_MORE,
    FIVE_PERCENT_MORE,
}
