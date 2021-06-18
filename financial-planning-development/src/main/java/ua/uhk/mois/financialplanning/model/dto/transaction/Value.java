package ua.uhk.mois.financialplanning.model.dto.transaction;

import ua.uhk.mois.financialplanning.model.entity.wish.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author KVN
 * @since 03.04.2021 1:51
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Value {

    private BigDecimal amount;

    private Currency currency;

}
