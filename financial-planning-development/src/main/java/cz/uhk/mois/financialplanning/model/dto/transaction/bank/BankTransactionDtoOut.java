package cz.uhk.mois.financialplanning.model.dto.transaction.bank;

import cz.uhk.mois.financialplanning.model.dto.transaction.AdditionalInfoDomestic;
import cz.uhk.mois.financialplanning.model.dto.transaction.Direction;
import cz.uhk.mois.financialplanning.model.dto.transaction.Value;
import lombok.Data;
import lombok.ToString;

import java.time.ZonedDateTime;

/**
 * @author Jan Krunčík
 * @since 03.04.2020 1:49
 */

@Data
@ToString
public class BankTransactionDtoOut {

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
     * Date of payment.
     */
    private ZonedDateTime valueDate;

    /**
     * This date is required by the used banking API. The value will always be the same as in the
     * cz.uhk.mois.financialplanning.model.dto.transaction.bank.AddBankTransactionDtoIn#valueDate attribute.
     */
    private ZonedDateTime bookingDate;

    /**
     * Additional / Advanced information for the transaction.
     */
    private AdditionalInfoDomestic additionalInfoDomestic;
}
