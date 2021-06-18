package cz.uhk.mois.financialplanning.model.dto.wish;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author Jan Krunčík
 * @since 10.04.2020 13:39
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChangePriorityDtoIn {

    private List<ChangePriorityWishInfo> changePriorityWishInfoList;

}
