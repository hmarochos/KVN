package cz.uhk.mois.financialplanning.model.entity.transaction;

import cz.uhk.mois.financialplanning.model.entity.wish.Currency;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author Jan Krunčík
 * @since 06.04.2020 1:27
 */

@Data
@Entity
@Table(name = "\"values\"")
@EqualsAndHashCode(exclude = "transaction")
@ToString(exclude = "transaction")
public class Value {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @OneToOne(mappedBy = "value", optional = false)
    private Transaction transaction;
}
