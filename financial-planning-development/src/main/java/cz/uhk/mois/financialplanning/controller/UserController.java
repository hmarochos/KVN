package cz.uhk.mois.financialplanning.controller;

import cz.uhk.mois.financialplanning.controller.path.UrlConstant;
import cz.uhk.mois.financialplanning.model.dto.user.ChangePasswordDtoIn;
import cz.uhk.mois.financialplanning.model.dto.user.ChangePasswordDtoOut;
import cz.uhk.mois.financialplanning.model.dto.user.CreateUserDtoIn;
import cz.uhk.mois.financialplanning.model.dto.user.DeleteUserDtoIn;
import cz.uhk.mois.financialplanning.model.dto.user.ProfileDtoOut;
import cz.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoIn;
import cz.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoOut;
import cz.uhk.mois.financialplanning.model.dto.user.UserDtoOut;
import cz.uhk.mois.financialplanning.response.FailureResponse;
import cz.uhk.mois.financialplanning.response.ServerResponse;
import cz.uhk.mois.financialplanning.response.Success;
import cz.uhk.mois.financialplanning.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;

/**
 * @author Jan Krunčík
 * @since 15.03.2020 19:40
 */

@RestController
@RequestMapping(UrlConstant.USER)
@Log4j2
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<UserDtoOut>> add(@RequestBody CreateUserDtoIn dtoIn) {
        log.info("Add / Create New User. {}", dtoIn);
        return userService.add(dtoIn)
                          .mapLeft(failure -> new FailureResponse<UserDtoOut>().createResponse(failure))
                          .fold(Function.identity(), Success::createResponse);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<ProfileDtoOut>> get() {
        log.info("Get the signed-in user's profile.");
        return userService.getProfile()
                          .mapLeft(failure -> new FailureResponse<ProfileDtoOut>().createResponse(failure))
                          .fold(Function.identity(), Success::createResponse);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<UpdateUserDtoOut>> update(@RequestBody UpdateUserDtoIn dtoIn) {
        log.info("Update user data (profile). {}", dtoIn);
        return userService.update(dtoIn)
                          .mapLeft(failure -> new FailureResponse<UpdateUserDtoOut>().createResponse(failure))
                          .fold(Function.identity(), Success::createResponse);
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<UserDtoOut>> delete(@RequestBody DeleteUserDtoIn dtoIn) {
        log.info("Delete user by email. {}", dtoIn);
        return userService.delete(dtoIn)
                          .mapLeft(failure -> new FailureResponse<UserDtoOut>().createResponse(failure))
                          .fold(Function.identity(), Success::createResponse);
    }

    @PostMapping(path = UrlConstant.USER_CHANGE_PASSWORD, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServerResponse<ChangePasswordDtoOut>> changePassword(@RequestBody ChangePasswordDtoIn dtoIn) {
        log.info("Change the password of the signed-in user. {}", dtoIn);
        return userService.changePassword(dtoIn)
                          .mapLeft(failure -> new FailureResponse<ChangePasswordDtoOut>().createResponse(failure))
                          .fold(Function.identity(), Success::createResponse);
    }
}
