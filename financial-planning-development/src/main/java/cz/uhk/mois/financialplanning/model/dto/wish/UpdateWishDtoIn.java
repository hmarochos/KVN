package cz.uhk.mois.financialplanning.model.dto.wish;

import cz.uhk.mois.financialplanning.model.entity.wish.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author Jan Krunčík
 * @since 24.03.2020 21:25
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
