package cz.uhk.mois.financialplanning.model.dto.wish;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jan Krunčík
 * @since 26.03.2020 6:27
 */

@Data
@ToString
public class DeleteWishDtoIn {

    /**
     * Id of the wish to be deleted.
     */
    private Long id;
}
