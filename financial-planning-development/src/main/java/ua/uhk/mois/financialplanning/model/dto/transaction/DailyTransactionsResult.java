package ua.uhk.mois.financialplanning.model.dto.transaction;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author KVN
 * @since 07.04.2021 8:30
 */

@Data
@Builder
@ToString
public class DailyTransactionsResult {

    /**
     * Index of the day in the month to which the amount relates, ie on which specific day the amount in the "result"
     * variable came to the account or was paid / debited from the account.
     */
    private int dayIndex;

    /**
     * The sum of user earnings and user deduction of expenses for one specific day.
     */
    private BigDecimal result;
}
