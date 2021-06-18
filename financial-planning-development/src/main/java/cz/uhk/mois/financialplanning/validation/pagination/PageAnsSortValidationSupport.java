package cz.uhk.mois.financialplanning.validation.pagination;

import cz.uhk.mois.financialplanning.validation.wish.WishListDtoInValidator;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Jan Krunčík
 * @since 25.03.2020 1:38
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageAnsSortValidationSupport {

    public static Validation<String, String> validateOrder(String order) {
        if (order == null || order.replaceAll("\\s", "").isEmpty()) {
            return Validation.invalid("The order of items in which they are to be sorted (ascending or descending) has not been defined.");
        }
        if (WishListDtoInValidator.ASCENDING.equalsIgnoreCase(order) || WishListDtoInValidator.DESCENDING.equalsIgnoreCase(order)) {
            return Validation.valid(order.toUpperCase());
        }
        return Validation.invalid("Only 'ASC' and 'DESC' values are allowed to define ascending or descending order.");
    }

    public static Validation<String, String> validateOrderBy(String orderBy, List<String> allowedColumns) {
        if (orderBy == null || orderBy.replaceAll("\\s", "").isEmpty()) {
            return Validation.invalid("No column to sort by has been defined.");
        }
        if (isAllowedColumn(orderBy, allowedColumns)) {
            return Validation.valid(orderBy);
        }
        String message = String.format("The values can only be sorted by the following columns %s.", allowedColumns);
        return Validation.invalid(message);
    }

    public static Validation<String, Integer> validatePage(Integer page) {
        if (page == null) {
            return Validation.invalid("Page index not specified.");
        }
        if (page < 0) {
            return Validation.invalid("Page index must be zero or greater.");
        }
        return Validation.valid(page);
    }

    public static Validation<String, Integer> validateSize(Integer size) {
        if (size == null) {
            return Validation.invalid("The number of items per page was not specified.");
        }
        if (size < 0) {
            return Validation.invalid("The number of items on the page must be zero or greater.");
        }
        return Validation.valid(size);
    }

    private static boolean isAllowedColumn(String column, List<String> allowedColumns) {
        return allowedColumns.stream()
                             .anyMatch(s -> s.equalsIgnoreCase(column));
    }
}
