package ua.uhk.mois.financialplanning.model.dto.planning.month;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author KVN
 * @since 11.04.2021 1:24
 */

@Data
@Builder
@ToString
public class MonthlyOverview {

    /**
     * Index of the month when the user will be able to afford a particular wish.
     */
    private int monthIndex;

    /**
     * Amount saved in the relevant month (monthIndex).
     */
    private BigDecimal amountSaved;
}
