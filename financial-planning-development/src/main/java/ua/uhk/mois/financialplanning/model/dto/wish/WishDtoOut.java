package ua.uhk.mois.financialplanning.model.dto.wish;

import ua.uhk.mois.financialplanning.model.entity.wish.Currency;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author KVN
 * @since 22.03.2021 15:46
 */

@Data
@ToString
public class WishDtoOut {

    private Long id;

    private BigDecimal price;

    private Currency currency;

    private String name;

    private String description;

    private Integer priority;
}
