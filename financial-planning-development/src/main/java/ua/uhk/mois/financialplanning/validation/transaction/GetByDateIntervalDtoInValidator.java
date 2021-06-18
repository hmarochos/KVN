package ua.uhk.mois.financialplanning.validation.transaction;

import ua.uhk.mois.financialplanning.model.dto.transaction.GetByDateIntervalDtoIn;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * @author KVN
 * @since 04.04.2021 14:50
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetByDateIntervalDtoInValidator {

    public static Validation<Seq<String>, GetByDateIntervalDtoIn> validate(GetByDateIntervalDtoIn dtoIn) {
        return Validation.combine(validateDateFrom(dtoIn.getDateFrom()),
                                  validateDateTo(dtoIn.getDateTo()))
                         .ap(GetByDateIntervalDtoIn::new);
    }

    private static Validation<String, ZonedDateTime> validateDateFrom(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return Validation.invalid("DateFrom (beginning of interval) is not specified.");
        }
        return Validation.valid(zonedDateTime);
    }

    private static Validation<String, ZonedDateTime> validateDateTo(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return Validation.invalid("DateTo (end of interval) is not specified.");
        }
        return Validation.valid(zonedDateTime);
    }
}
