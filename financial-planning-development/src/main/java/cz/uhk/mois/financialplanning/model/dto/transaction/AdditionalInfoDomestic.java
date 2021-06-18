package cz.uhk.mois.financialplanning.model.dto.transaction;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jan Krunčík
 * @since 03.04.2020 1:58
 */

@Data
@ToString
public class AdditionalInfoDomestic {

    private String constantSymbol;
    private String variableSymbol;
    private String specificSymbol;

}
