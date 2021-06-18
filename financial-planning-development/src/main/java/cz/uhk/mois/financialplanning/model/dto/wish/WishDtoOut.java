package cz.uhk.mois.financialplanning.model.dto.wish;

import cz.uhk.mois.financialplanning.model.entity.wish.Currency;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author Jan Krunčík
 * @since 22.03.2020 15:46
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
