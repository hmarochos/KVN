package cz.uhk.mois.financialplanning.model.dto.transaction;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Jan Krunčík
 * @since 07.04.2020 8:33
 */

@Data
@ToString
public class LastMonthTransactionsOverviewDtoOut {

    private List<DailyTransactionsResult> dailyTransactionsResultList;
}
