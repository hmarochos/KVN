package ua.uhk.mois.financialplanning.model.dto.wish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author KVN
 * @since 25.03.2021 1:30
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WishListDtoOut {

    private List<WishDtoOut> wishList;

    /**
     * Total number of wishes in the database related to the signed-in user. <br/>
     * <i>To set paging to FE so the user can see how many items are available.</i>
     */
    private Long count;
}
