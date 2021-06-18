package ua.uhk.mois.financialplanning.model.dto.wish;

import ua.uhk.mois.financialplanning.model.entity.wish.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author KVN
 * @since 24.03.2021 21:25
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateWishDtoIn {

    private Long id;

    private BigDecimal price;

    private Currency currency;

    private String name;

    private String description;

    private Integer priority;
}
