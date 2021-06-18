package cz.uhk.mois.financialplanning.model.dto.transaction;

import cz.uhk.mois.financialplanning.model.dto.transaction.bank.BankTransactionDtoOut;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Jan Krunčík
 * @since 04.04.2020 14:33
 */

@Data
@ToString
public class GetByDateIntervalDtoOut {

    private List<BankTransactionDtoOut> bankTransactionDtoOutList;

}
