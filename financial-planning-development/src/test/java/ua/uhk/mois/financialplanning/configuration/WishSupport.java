package ua.uhk.mois.financialplanning.configuration;

import ua.uhk.mois.financialplanning.model.dto.wish.ChangePriorityWishInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author KVN
 * @since 10.04.2021 19:08
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
