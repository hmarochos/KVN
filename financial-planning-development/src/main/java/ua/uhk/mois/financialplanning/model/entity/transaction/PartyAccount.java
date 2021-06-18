package ua.uhk.mois.financialplanning.model.entity.transaction;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author KVN
 * @since 06.04.2021 1:28
 */

@Data
@Entity
@Table(name = "party_accounts")
@EqualsAndHashCode(exclude = "transaction")
@ToString(exclude = "transaction")
public class PartyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Same as ua.uhk.mois.financialplanning.model.entity.transaction.Transaction#accountId.
     */
    @Column(nullable = false)
    private String accountNumber;

    /**
     * The bank code used for the transaction (money transfer).
     */
    @Column(nullable = false)
    private String bankCode;

    @OneToOne(mappedBy = "partyAccount", optional = false)
    private Transaction transaction;
}
