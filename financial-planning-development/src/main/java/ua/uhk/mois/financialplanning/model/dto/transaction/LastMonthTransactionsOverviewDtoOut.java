package ua.uhk.mois.financialplanning.model.dto.transaction;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author KVN
 * @since 07.04.2021 8:33
 */

@Data
@ToString
public class LastMonthTransactionsOverviewDtoOut {

    private List<DailyTransactionsResult> dailyTransactionsResultList;
}
