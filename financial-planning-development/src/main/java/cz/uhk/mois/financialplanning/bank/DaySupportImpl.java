package cz.uhk.mois.financialplanning.bank;

import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZonedDateTime;

/**
 * @author Jan Krunčík
 * @since 09.04.2020 2:57
 */

@Component
public class DaySupportImpl implements DaySupport {

    /**
     * Get the previous month's index. <br/>
     * <i>If this is the first month (index = 1), then the 12th month (last) will be returned.</i>
     *
     * @param month
     *         index of the current month (1 - 12)
     *
     * @return index of the previous month
     */
    private static int getPreviousMonth(int month) {
        return month == 1 ? 12 : month - 1;
    }

    @Override
    public int getCountOfDaysInLastMonth(ZonedDateTime now) {
        YearMonth yearMonthObject = YearMonth.of(now.getYear(), getPreviousMonth(now.getMonth().getValue()));
        return yearMonthObject.lengthOfMonth();
    }
}
