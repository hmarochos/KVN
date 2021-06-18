package ua.uhk.mois.financialplanning.configuration.user;

import ua.uhk.mois.financialplanning.model.dto.user.AddressDto;
import ua.uhk.mois.financialplanning.model.dto.user.ChangePasswordDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.CreateUserDtoIn;
import ua.uhk.mois.financialplanning.model.dto.user.UpdateUserDtoIn;
import ua.uhk.mois.financialplanning.model.entity.user.Address;
import ua.uhk.mois.financialplanning.model.entity.user.Role;
import ua.uhk.mois.financialplanning.model.entity.user.User;
import ua.uhk.mois.financialplanning.model.entity.wish.Currency;
import ua.uhk.mois.financialplanning.model.entity.wish.Wish;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author KVN
 * @since 17.03.2021 0:26
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserTestSupport {

    public static final String PASSWORD = "Pwd-ě_123@/!";
    public static final String CHANGED_PASSWORD = "Changed" + PASSWORD;
    // Hash for: Pwd-ě_123@/!
    public static final String PASSWORD_HASH = "$2a$10$qpA69lJJf2DnQnXQTASzkuF/l1eZG8QjHP/qeBGKYr4AI4ME9V0c.";

    public static final String FIRST_NAME = "Homer";
    public static final String LAST_NAME = "Simpson";
    public static final String EMAIL = "homer.simpson@gmail.com";
    public static final Long ACCOUNT_ID = 123456L;
    public static final String TELEPHONE_NUMBER = "+420 123 456 789";

    public static final String STREET = "Rokitanského 123";
    public static final String CITY = "Hradec Králové";
    public static final Integer PSC = 15678;

    public static User createUser() {
        return createUser(false);
    }

    public static User createUser(boolean includeWishes) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setEmail(EMAIL);
        user.setPasswordHash(PASSWORD_HASH);
        user.setAccountId(ACCOUNT_ID);
        user.setTelephoneNumber(TELEPHONE_NUMBER);
        user.setAddress(createAddress());
        user.setRoles(Collections.singletonList(Role.USER));
        user.setLastLogin(now);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        if (includeWishes) {
            user.setWishList(createWishList(user));
        }

        return user;
    }

    private static Address createAddress() {
        Address address = new Address();
        address.setStreet(STREET);
        address.setCity(CITY);
        address.setPsc(PSC);
        return address;
    }

    private static List<Wish> createWishList(User user) {
        return IntStream.range(0, 3)
                        .parallel()
                        .mapToObj(i -> createWish(BigDecimal.valueOf(1000L * i), "Wish " + i, i + 1, user))
                        .collect(Collectors.toList());
    }

    public static Wish createWish(BigDecimal price, String name, Integer priority, User user) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        Wish wish = new Wish();
        wish.setPrice(price);
        wish.setCurrency(Currency.uaK);
        wish.setName(name);
        wish.setDescription("Some description.");
        wish.setPriority(priority);
        wish.setUser(user);
        wish.setCreatedAt(now);
        wish.setUpdatedAt(now);
        return wish;
    }

    public static AddressDto createAddressDto() {
        return createAddressDto(STREET, CITY, PSC);
    }

    private static AddressDto createAddressDto(String street, String city, Integer psc) {
        AddressDto addressDto = new AddressDto();
        addressDto.setStreet(street);
        addressDto.setCity(city);
        addressDto.setPsc(psc);
        return addressDto;
    }

    public static UpdateUserDtoIn createUpdateUserDtoIn() {
        UpdateUserDtoIn dtoIn = new UpdateUserDtoIn();
        dtoIn.setFirstName("Ned");
        dtoIn.setLastName("Flanders");
        dtoIn.setOriginalEmail(EMAIL);
        dtoIn.setUpdatedEmail("ned.flanders@seznam.ua");
        dtoIn.setAccountId(ACCOUNT_ID);
        dtoIn.setTelephoneNumber("987 654 321");
        dtoIn.setAddress(createAddressDto("Changed Street 123", "Changed City", 987654));
        return dtoIn;
    }

    public static ChangePasswordDtoIn createChangePasswordDtoIn() {
        ChangePasswordDtoIn dtoIn = new ChangePasswordDtoIn();
        dtoIn.setEmail(EMAIL);
        dtoIn.setOriginalPassword(PASSWORD);
        dtoIn.setNewPassword(CHANGED_PASSWORD);
        dtoIn.setConfirmationPassword(CHANGED_PASSWORD);
        return dtoIn;
    }

    /**
     * Login (/ setup) of user to Spring context. <br/>
     * <i>This is a simulation of the singed-in user.</i>
     *
     * @param user
     *         user who will be written to Spring context (singed-in user in application)
     */
    public static void signInUser(User user) {
        List<SimpleGrantedAuthority> authorityList = getAuthorityList(user.getRoles());
        Authentication authToken = new UsernamePasswordAuthenticationToken(user.getId().toString(), user.getPasswordHash(), authorityList);

        Map<String, String> requestParameters = new HashMap<>();
        String clientId = "MOIS-Financial-Planing-API";

        Set<String> scope = new HashSet<>();
        scope.add("scope");

        Set<String> resourceIds = new HashSet<>();

        Set<String> responseTypes = new HashSet<>();
        responseTypes.add("code");

        Map<String, Serializable> extensionProperties = new HashMap<>();

        OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, clientId, authorityList, true, scope, resourceIds, null, responseTypes, extensionProperties);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authToken);
        SecurityContextHolder.getContext().setAuthentication(oAuth2Authentication);
    }

    private static List<SimpleGrantedAuthority> getAuthorityList(List<Role> roles) {
        return roles
                .parallelStream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }

    public static void signOutUser() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public static CreateUserDtoIn createCreateUserDtoIn() {
        CreateUserDtoIn userDtoIn = new CreateUserDtoIn();
        userDtoIn.setFirstName(FIRST_NAME);
        userDtoIn.setLastName(LAST_NAME);
        userDtoIn.setEmail(EMAIL);
        userDtoIn.setPassword(PASSWORD);
        userDtoIn.setPasswordConfirmation(PASSWORD);
        userDtoIn.setAccountId(ACCOUNT_ID);
        userDtoIn.setTelephoneNumber(TELEPHONE_NUMBER);
        userDtoIn.setAddress(createAddressDto());
        userDtoIn.setRoles(Collections.singletonList(Role.USER));
        return userDtoIn;
    }
}
