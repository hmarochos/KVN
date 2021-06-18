package ua.uhk.mois.financialplanning.model.dto.wish;

import ua.uhk.mois.financialplanning.model.entity.wish.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author KVN
 * @since 22.03.2021 15:43
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateWishDtoIn {

    private BigDecimal price;

    private Currency currency;

    private String name;

    private String description;

}
