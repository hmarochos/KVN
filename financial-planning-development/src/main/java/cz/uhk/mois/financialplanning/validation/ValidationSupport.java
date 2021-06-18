package cz.uhk.mois.financialplanning.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 15.03.2020 20:54
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationSupport {

    /**
     * The text parameter is in the 'List(some messages)' syntax. The method removes the section with 'List(' and ')'.
     *
     * @param text
     *         reports about violation of validation rules
     *
     * @return text without the above mentioned values
     */
    public static String removeListText(String text) {
        return text.substring(5, text.length() - 1);
    }
}
