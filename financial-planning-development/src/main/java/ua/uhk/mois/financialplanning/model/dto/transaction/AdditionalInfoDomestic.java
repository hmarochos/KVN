package ua.uhk.mois.financialplanning.model.dto.transaction;

import lombok.Data;
import lombok.ToString;

/**
 * @author KVN
 * @since 03.04.2021 1:58
 */

@Data
@ToString
public class AdditionalInfoDomestic {

    private String constantSymbol;
    private String variableSymbol;
    private String specificSymbol;

}
