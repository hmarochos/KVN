package cz.uhk.mois.financialplanning.model.dto.transaction.bank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jan Krunčík
 * @since 03.04.2020 1:54
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PartyAccount {

    /**
     * Same as cz.uhk.mois.financialplanning.model.dto.transaction.AddTransactionDtoIn#accountId.
     */
    private String accountNumber;

    /**
     * The bank code used for the transaction (money transfer).
     */
    private String bankCode;
}
