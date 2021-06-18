package cz.uhk.mois.financialplanning.model.dto.transaction;

import cz.uhk.mois.financialplanning.model.entity.wish.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author Jan Krunčík
 * @since 03.04.2020 1:51
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Value {

    private BigDecimal amount;

    private Currency currency;

}
