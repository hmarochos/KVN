package cz.uhk.mois.financialplanning.model.entity.transaction;

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
 * @author Jan Krunčík
 * @since 06.04.2020 1:29
 */

@Data
@Entity
@Table(name = "additional_info_domestic")
@EqualsAndHashCode(exclude = "transaction")
@ToString(exclude = "transaction")
public class AdditionalInfoDomestic {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String constantSymbol;

    @Column(nullable = false)
    private String variableSymbol;

    @Column(nullable = false)
    private String specificSymbol;

    @OneToOne(mappedBy = "additionalInfoDomestic", optional = false)
    private Transaction transaction;
}
