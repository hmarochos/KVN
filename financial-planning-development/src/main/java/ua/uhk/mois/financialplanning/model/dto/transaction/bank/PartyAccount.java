package ua.uhk.mois.financialplanning.model.dto.transaction.bank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author KVN
 * @since 03.04.2021 1:54
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PartyAccount {

    /**
     * Same as ua.uhk.mois.financialplanning.model.dto.transaction.AddTransactionDtoIn#accountId.
     */
    private String accountNumber;

    /**
     * The bank code used for the transaction (money transfer).
     */
    private String bankCode;
}
