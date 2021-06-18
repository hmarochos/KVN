package ua.uhk.mois.financialplanning.validation.wish;

import ua.uhk.mois.financialplanning.model.dto.wish.WishListDtoIn;
import ua.uhk.mois.financialplanning.validation.pagination.PageAnsSortValidationSupport;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author KVN
 * @since 25.03.2021 1:35
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WishListDtoInValidator {

    public static final String ASCENDING = "ASC";
    public static final String DESCENDING = "DESC";

    private static final List<String> ALLOWED_COLUMNS = Collections.unmodifiableList(Arrays.asList("name", "priority"));

    public static Validation<Seq<String>, WishListDtoIn> validate(WishListDtoIn dtoIn) {
        return Validation.combine(PageAnsSortValidationSupport.validatePage(dtoIn.getPage()),
                                  PageAnsSortValidationSupport.validateSize(dtoIn.getSize()),
                                  PageAnsSortValidationSupport.validateOrder(dtoIn.getOrder()),
                                  PageAnsSortValidationSupport.validateOrderBy(dtoIn.getOrderBy(), ALLOWED_COLUMNS))
                         .ap(WishListDtoIn::new);
    }
}
