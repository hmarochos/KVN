package ua.uhk.mois.financialplanning.validation.wish;

import ua.uhk.mois.financialplanning.model.dto.wish.ChangePriorityDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.ChangePriorityWishInfo;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ua.uhk.mois.financialplanning.validation.ValidationSupport.removeListText;

/**
 * @author KVN
 * @since 10.04.2021 13:42
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChangePriorityDtoInValidator {

    private static final int MAX_WISH_ITEM_COUNT = 9;

    public static Validation<Seq<String>, ChangePriorityDtoIn> validate(ChangePriorityDtoIn dtoIn) {
        return Validation.combine(validateItemCount(dtoIn.getChangePriorityWishInfoList()),
                                  validateWishItems(dtoIn.getChangePriorityWishInfoList()))
                         .ap((changePriorityWishInfoList, changePriorityWishInfoList2) -> new ChangePriorityDtoIn(changePriorityWishInfoList));
    }

    private static Validation<String, List<ChangePriorityWishInfo>> validateItemCount(List<ChangePriorityWishInfo> changePriorityWishInfoList) {
        if (changePriorityWishInfoList == null || changePriorityWishInfoList.isEmpty()) {
            return Validation.invalid("There is no information about wishes to change priority.");
        }
        if (changePriorityWishInfoList.size() > MAX_WISH_ITEM_COUNT) {
            String message = String.format("The maximum allowed number of changes in wishes priorities is %s.", MAX_WISH_ITEM_COUNT);
            return Validation.invalid(message);
        }
        return Validation.valid(changePriorityWishInfoList);
    }

    private static Validation<String, List<ChangePriorityWishInfo>> validateWishItems(List<ChangePriorityWishInfo> changePriorityWishInfoList) {
        // This condition was tested in the previous step (method ua.uhk.mois.financialplanning.validation.wish.ChangePriorityDtoInValidator.validateItemCount)
        if (changePriorityWishInfoList == null) {
            return Validation.valid(null);
        }

        for (ChangePriorityWishInfo wishInfo : changePriorityWishInfoList) {
            Validation<Seq<String>, ChangePriorityWishInfo> changePriorityWishInfoValidation = validateWishItem(wishInfo);
            if (changePriorityWishInfoValidation.isInvalid()) {
                String message = String.format("The change for wish '%s' is not valid. %s", wishInfo.getName(), removeListText(changePriorityWishInfoValidation.getError().toString()));
                return Validation.invalid(message);
            }
        }

        Validation<String, List<ChangePriorityWishInfo>> priorityDuplicateValidation = validatePriorityDuplicates(changePriorityWishInfoList);
        if (priorityDuplicateValidation.isInvalid()) {
            return Validation.invalid(priorityDuplicateValidation.getError());
        }

        Validation<String, List<ChangePriorityWishInfo>> idDuplicateValidation = validateIdDuplicates(changePriorityWishInfoList);
        if (idDuplicateValidation.isInvalid()) {
            return Validation.invalid(idDuplicateValidation.getError());
        }

        return Validation.valid(changePriorityWishInfoList);
    }

    private static Validation<Seq<String>, ChangePriorityWishInfo> validateWishItem(ChangePriorityWishInfo changePriorityWishInfo) {
        return Validation.combine(validateId(changePriorityWishInfo.getId()),
                                  WishValidationSupport.validateName(changePriorityWishInfo.getName()),
                                  WishValidationSupport.validatePriority(changePriorityWishInfo.getPriority()))
                         .ap(ChangePriorityWishInfo::new);
    }

    static Validation<String, Long> validateId(Long id) {
        if (id == null) {
            return Validation.invalid("Id not specified.");
        }
        if (id < 0) {
            return Validation.invalid("Id must be a positive number.");
        }
        return Validation.valid(id);
    }

    private static Validation<String, List<ChangePriorityWishInfo>> validatePriorityDuplicates(List<ChangePriorityWishInfo> changePriorityWishInfoList) {
        List<Integer> priorityList = changePriorityWishInfoList.parallelStream()
                                                               .map(ChangePriorityWishInfo::getPriority)
                                                               .collect(Collectors.toList());
        Set<Integer> prioritySet = new HashSet<>(priorityList);
        if (prioritySet.size() < priorityList.size()) {
            return Validation.invalid("Duplicate priorities found.");
        }
        return Validation.valid(changePriorityWishInfoList);
    }

    private static Validation<String, List<ChangePriorityWishInfo>> validateIdDuplicates(List<ChangePriorityWishInfo> changePriorityWishInfoList) {
        List<Long> idList = changePriorityWishInfoList.parallelStream()
                                                      .map(ChangePriorityWishInfo::getId)
                                                      .collect(Collectors.toList());

        Set<Long> idSet = new HashSet<>(idList);
        if (idSet.size() < idList.size()) {
            return Validation.invalid("Duplicate id found.");
        }
        return Validation.valid(changePriorityWishInfoList);
    }
}
