package ua.uhk.mois.financialplanning.model.dto.wish;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author KVN
 * @since 10.04.2021 13:39
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChangePriorityDtoIn {

    private List<ChangePriorityWishInfo> changePriorityWishInfoList;

}
