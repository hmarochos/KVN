package cz.uhk.mois.financialplanning.model.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;

/**
 * @author Jan Krunčík
 * @since 04.04.2020 14:33
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GetByDateIntervalDtoIn {

    private ZonedDateTime dateFrom;

    private ZonedDateTime dateTo;

}
