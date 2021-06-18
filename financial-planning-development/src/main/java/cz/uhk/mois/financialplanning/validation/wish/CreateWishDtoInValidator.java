package cz.uhk.mois.financialplanning.validation.wish;

import cz.uhk.mois.financialplanning.model.dto.wish.CreateWishDtoIn;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 22.03.2020 19:07
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateWishDtoInValidator {

    public static Validation<Seq<String>, CreateWishDtoIn> validate(CreateWishDtoIn dtoIn) {
        return Validation.combine(WishValidationSupport.validatePrice(dtoIn.getPrice()),
                                  WishValidationSupport.validateCurrency(dtoIn.getCurrency()),
                                  WishValidationSupport.validateName(dtoIn.getName()),
                                  WishValidationSupport.validateDescription(dtoIn.getDescription()))
                         .ap(CreateWishDtoIn::new);
    }
}
