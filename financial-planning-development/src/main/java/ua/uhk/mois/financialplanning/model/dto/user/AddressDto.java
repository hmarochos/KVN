package ua.uhk.mois.financialplanning.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author KVN
 * @since 15.03.2021 23:30
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AddressDto {

    private String street;

    private String city;

    private Integer psc;
}
