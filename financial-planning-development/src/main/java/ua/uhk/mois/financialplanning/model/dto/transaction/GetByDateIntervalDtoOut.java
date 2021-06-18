package ua.uhk.mois.financialplanning.model.dto.transaction;

import ua.uhk.mois.financialplanning.model.dto.transaction.bank.BankTransactionDtoOut;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author KVN
 * @since 04.04.2021 14:33
 */

@Data
@ToString
public class GetByDateIntervalDtoOut {

    private List<BankTransactionDtoOut> bankTransactionDtoOutList;

}
