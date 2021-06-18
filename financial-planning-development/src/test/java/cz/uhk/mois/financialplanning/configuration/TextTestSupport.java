package cz.uhk.mois.financialplanning.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 17.03.2020 0:35
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextTestSupport {

    /**
     * Compose text in the text variable (separated by an underscore) until the text exceeds the length in the length
     * variable.
     *
     * @param length
     *         the length that the composite text must overcome
     * @param text
     *         text to be composed (underscores join in a cycle)
     *
     * @return compound text that is greater than length
     */
    public static String generateInvalidText(Long length, String text) {
        StringBuilder stringBuilder = new StringBuilder();

        boolean iterate = true;
        while (iterate) {
            stringBuilder.append(text);

            if (stringBuilder.length() > length) {
                iterate = false;
            } else {
                stringBuilder.append("_");
            }
        }

        return stringBuilder.toString();
    }
}
