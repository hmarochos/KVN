package cz.uhk.mois.financialplanning.model.entity.transaction;

import cz.uhk.mois.financialplanning.model.dto.transaction.Direction;
import cz.uhk.mois.financialplanning.model.dto.transaction.bank.TransactionType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.ZonedDateTime;

/**
 * @author Jan Krunčík
 * @since 06.04.2020 0:41
 */

@Data
@Entity
@Table(name = "transactions")
@EqualsAndHashCode
@ToString
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The account number to which the transaction relates (it is required for access to banking API).
     */
    @Column(nullable = false)
    private Long accountId;

    /**
     * Information about the amount to be transferred.
     */
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "value_id", referencedColumnName = "id")
    private Value value;

    /**
     * The bank and bank account information used for this transaction.
     */
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "partyAccount_id", referencedColumnName = "id")
    private PartyAccount partyAccount;

    /**
     * Optional transaction label. <br/>
     * <i>For example, transaction information - what is that money, etc.</i>
     */
    private String partyDescription;

    /**
     * Whether it is an incoming or outgoing payment (or both).
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Direction direction;

    /**
     * This is a type of transaction. <br/>
     * <i>It is required by the used banking API.</i>
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    /**
     * Date of payment.
     */
    @Column(nullable = false)
    private ZonedDateTime valueDate;

    /**
     * This date is required by the used banking API. The value will always be the same as in the
     * cz.uhk.mois.financialplanning.model.dto.transaction.bank.AddBankTransactionDtoIn#valueDate attribute.
     */
    @Column(nullable = false)
    private ZonedDateTime bookingDate;

    /**
     * Additional / Advanced information for the transaction.
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "additionalInfoDomestic_id", referencedColumnName = "id")
    private AdditionalInfoDomestic additionalInfoDomestic;
}
