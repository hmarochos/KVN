package ua.uhk.mois.financialplanning.controller;

import ua.uhk.mois.financialplanning.controller.path.UrlConstant;
import ua.uhk.mois.financialplanning.model.dto.wish.ChangePriorityDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.ChangePriorityDtoOut;
import ua.uhk.mois.financialplanning.model.dto.wish.CreateWishDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.DeleteAllWishesDtoOut;
import ua.uhk.mois.financialplanning.model.dto.wish.DeleteWishDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.UpdateWishDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.WishDtoOut;
import ua.uhk.mois.financialplanning.model.dto.wish.WishListDtoIn;
import ua.uhk.mois.financialplanning.model.dto.wish.WishListDtoOut;
import ua.uhk.mois.financialplanning.response.FailureResponse;
import ua.uhk.mois.financialplanning.response.ServerResponse;
import ua.uhk.mois.financialplanning.response.Success;
import ua.uhk.mois.financialplanning.service.WishService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;

/**
 * @author KVN
 * @since 22.03.2021 14:53
 */

@RestController
@RequestMapping(UrlConstant.WISH)
@Log4j2
public class WishController {

    private final WishService wishService;

    public WishController(WishService wishService) {
        this.wishService = wishService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<WishDtoOut>> add(@RequestBody CreateWishDtoIn dtoIn) {
        log.info("Creating a new financial goal (/ wish). {}", dtoIn);
        return wishService.add(dtoIn)
                          .mapLeft(failure -> new FailureResponse<WishDtoOut>().createResponse(failure))
                          .fold(Function.identity(), Success::createResponse);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<WishDtoOut>> update(@RequestBody UpdateWishDtoIn dtoIn) {
        log.info("Adjust wish (financial goal). {}", dtoIn);
        return wishService.update(dtoIn)
                          .mapLeft(failure -> new FailureResponse<WishDtoOut>().createResponse(failure))
                          .fold(Function.identity(), Success::createResponse);
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<WishDtoOut>> delete(@RequestBody DeleteWishDtoIn dtoIn) {
        log.info("Delete wish by id. {}", dtoIn);
        return wishService.delete(dtoIn)
                          .mapLeft(failure -> new FailureResponse<WishDtoOut>().createResponse(failure))
                          .fold(Function.identity(), Success::createResponse);
    }

    @DeleteMapping(path = UrlConstant.WISH_DELETE_ALL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<DeleteAllWishesDtoOut>> deleteAll() {
        log.info("Delete all signed-in user wishes.");
        return wishService.deleteAll()
                          .mapLeft(failure -> new FailureResponse<DeleteAllWishesDtoOut>().createResponse(failure))
                          .fold(Function.identity(), Success::createResponse);
    }

    @PostMapping(path = UrlConstant.WISH_LIST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<WishListDtoOut>> getList(@RequestBody WishListDtoIn dtoIn) {
        log.info("Load wish list based on sorting and paging requirements. {}", dtoIn);
        return wishService.getList(dtoIn)
                          .mapLeft(failure -> new FailureResponse<WishListDtoOut>().createResponse(failure))
                          .fold(Function.identity(), Success::createResponse);
    }

    @PutMapping(path = UrlConstant.WISH_CHANGE_PRIORITY, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<ChangePriorityDtoOut>> changePriority(@RequestBody ChangePriorityDtoIn dtoIn) {
        log.info("Change the priority of the following wishes. {}", dtoIn);
        return wishService.changePriority(dtoIn)
                          .mapLeft(failure -> new FailureResponse<ChangePriorityDtoOut>().createResponse(failure))
                          .fold(Function.identity(), Success::createResponse);
    }
}
