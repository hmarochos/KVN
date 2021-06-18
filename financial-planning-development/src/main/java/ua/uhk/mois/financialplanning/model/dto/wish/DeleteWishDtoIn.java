package ua.uhk.mois.financialplanning.model.dto.wish;

import lombok.Data;
import lombok.ToString;

/**
 * @author KVN
 * @since 26.03.2021 6:27
 */

@Data
@ToString
public class DeleteWishDtoIn {

    /**
     * Id of the wish to be deleted.
     */
    private Long id;
}
