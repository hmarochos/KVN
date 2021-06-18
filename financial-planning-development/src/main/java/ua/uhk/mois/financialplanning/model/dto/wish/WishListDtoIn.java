package ua.uhk.mois.financialplanning.model.dto.wish;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author KVN
 * @since 25.03.2021 1:30
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WishListDtoIn {

    /**
     * Page Index.
     */
    private Integer page;

    /**
     * The number of items per page.
     */
    private Integer size;

    /**
     * Order of items. <br/>
     * <i>This can be ascending ('ASC') or descending ('DESC').</i>
     */
    private String order;

    /**
     * The name of the column by which the items are to be sorted.
     */
    private String orderBy;
}
