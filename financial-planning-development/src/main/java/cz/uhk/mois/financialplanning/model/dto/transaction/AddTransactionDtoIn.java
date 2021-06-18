package cz.uhk.mois.financialplanning.model.dto.transaction;

import cz.uhk.mois.financialplanning.model.dto.transaction.bank.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;

/**
 * @author Jan Krunčík
 * @since 03.04.2020 1:48
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AddTransactionDtoIn {

    /**
     * The account number to which the transaction relates. <br/>
     * <i>Thus, it is the identification of the transaction itself.</i>
     */
    private Long accountId;

    /**
     * Information about the amount to be transferred.
     */
    private Value value;

    /**
     * The bank code used for the transaction (money transfer).
     */
    private Long bankCode;

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
     * The date the payment was made.
     */
    private ZonedDateTime paymentDate;

    /**
     * Additional / Advanced information for the transaction.
     */
    private AdditionalInfoDomestic additionalInfoDomestic;
}
