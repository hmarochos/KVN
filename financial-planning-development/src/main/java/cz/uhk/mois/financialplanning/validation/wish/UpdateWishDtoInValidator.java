package cz.uhk.mois.financialplanning.validation.wish;

import cz.uhk.mois.financialplanning.model.dto.wish.UpdateWishDtoIn;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 24.03.2020 21:30
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateWishDtoInValidator {

    public static Validation<Seq<String>, UpdateWishDtoIn> validate(UpdateWishDtoIn dtoIn) {
        return Validation.combine(WishValidationSupport.validateId(dtoIn.getId()),
                                  WishValidationSupport.validatePrice(dtoIn.getPrice()),
                                  WishValidationSupport.validateCurrency(dtoIn.getCurrency()),
                                  WishValidationSupport.validateName(dtoIn.getName()),
                                  WishValidationSupport.validateDescription(dtoIn.getDescription()),
                                  WishValidationSupport.validatePriority(dtoIn.getPriority()))
                         .ap(UpdateWishDtoIn::new);
    }
}
