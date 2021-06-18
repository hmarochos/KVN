package cz.uhk.mois.financialplanning.validation.wish;

import cz.uhk.mois.financialplanning.model.dto.wish.DeleteWishDtoIn;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 26.03.2020 6:31
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteWishDtoInValidator {

    public static Validation<String, DeleteWishDtoIn> validate(DeleteWishDtoIn dtoIn) {
        return WishValidationSupport.validateId(dtoIn.getId())
                                    .map(id -> dtoIn);
    }
}
