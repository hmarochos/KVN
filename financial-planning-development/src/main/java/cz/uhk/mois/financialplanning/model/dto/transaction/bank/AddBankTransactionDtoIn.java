package cz.uhk.mois.financialplanning.model.dto.transaction.bank;

import cz.uhk.mois.financialplanning.model.dto.transaction.AdditionalInfoDomestic;
import cz.uhk.mois.financialplanning.model.dto.transaction.Direction;
import cz.uhk.mois.financialplanning.model.dto.transaction.Value;
import lombok.Builder;
import lombok.Data;

/**
 * The object used to create a transaction (database record) in the provided (summation) Unicorn banking API. <br/>
 * <i>Not all values ​​are needed, but some are required for unknown validation (I did not find any data and the
 * teachers did not give me anything either - this result is only a result of trial and error).</i>
 *
 * @author Jan Krunčík
 * @since 03.04.2020 2:15
 */

@Data
@Builder
public class AddBankTransactionDtoIn {

    /**
     * The value will not be used or filled, it is only required by the banking API simulation used. <br/>
     * <i>This variable must be written like this and is final, so that it is no longer used, it cannot be static to be
     * used in the API where it is required.</i>
     */
    private final String id = "";

    /**
     * The account number to which the transaction relates (it is required for access to banking API).
     */
    private Long accountId;

    /**
     * Information about the amount to be transferred.
     */
    private Value value;

    /**
     * The bank and bank account information used for this transaction.
     */
    private PartyAccount partyAccount;

    /**
     * Optional transaction label. <br/>
     * <i>For example, transaction information - what is that money, etc.</i>
     */
    private String partyDescription;

    /**
     * Whether it is an incoming or outgoing payment (or both).
     */
    private Direction direction;

    /**
     * This is a type of transaction. <br/>
     * <i>It is required by the used banking API.</i>
     */
    private TransactionType transactionType;

    /**
     * Date of payment. <br/>
     * <i>Syntax: 2020-03-15T14:05:30Z</i>
     */
    private String valueDate;

    /**
     * This date is required by the used banking API. The value will always be the same as in the
     * cz.uhk.mois.financialplanning.model.dto.transaction.bank.AddBankTransactionDtoIn#valueDate attribute. <br/>
     * <i>Syntax: 2020-03-15T14:05:30Z</i>
     */
    private String bookingDate;

    /**
     * Additional / Advanced information for the transaction.
     */
    private AdditionalInfoDomestic additionalInfoDomestic;
}
