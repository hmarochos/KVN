package ua.uhk.mois.financialplanning.bank;

import java.time.ZonedDateTime;

/**
 * @author KVN
 * @since 24.05.2021 21:54
 */

public interface DaySupport {

    /**
     * Get the number of days in the last (previous) month.
     *
     * @param now
     *         the current date and time, from which the last month will taken
     *
     * @return the number of days the previous month has
     */
    int getCountOfDaysInLastMonth(ZonedDateTime now);
}
