package cz.uhk.mois.financialplanning.configuration;

import cz.uhk.mois.financialplanning.model.dto.wish.ChangePriorityWishInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jan Krunčík
 * @since 10.04.2020 19:08
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WishSupport {

    public static ChangePriorityWishInfo createChangePriorityWishInfo(Long wishId, Integer priority) {
        ChangePriorityWishInfo changePriorityWishInfo = new ChangePriorityWishInfo();
        changePriorityWishInfo.setId(wishId);
        changePriorityWishInfo.setName("ChangePriorityWishInfo " + priority);
        changePriorityWishInfo.setPriority(priority);
        return changePriorityWishInfo;
    }
}
