package cz.uhk.mois.financialplanning.security.service;

import cz.uhk.mois.financialplanning.model.entity.user.User;
import cz.uhk.mois.financialplanning.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * It is used when the user sign-in to verify that the user is in the database (it is registered in the application) and
 * can generate an access token for the application.
 *
 * @author Jan Krunčík
 * @since 17.03.2020 20:39
 */

@Service
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * A regular expression for a user id (database id).
     */
    private static final String REG_EXP_ID = "^\\d+$";

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static List<SimpleGrantedAuthority> getAuthorityList(User user) {
        return user.getRoles()
                   .parallelStream()
                   .map(role -> new SimpleGrantedAuthority(role.name()))
                   .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Load user with email '{}'. This is an sign-in attempt.", email);

        /*
         * If the user wants to sign-in to the application and get the access token, he / she will enter his / her email address and password. In this case, the email parameter will have its email address.
         *
         * <br/>
         *
         * If the access token has expired, the application will send a refresh token, the application will extract the user id from it, because, in that token will be the user's id, see creating UserDetails below. Therefore, it is necessary to write another way that will load the user according to its id.
         */

        User user = email.matches(REG_EXP_ID) ? userRepository.findById(Long.valueOf(email))
                                                              .orElseThrow(() -> {
                                                                  String message = String.format("User not found with id '%s'.", email);
                                                                  log.error(message);
                                                                  return new UsernameNotFoundException(message);
                                                              })
                                              : userRepository.findByEmail(email)
                                                              .orElseThrow(() -> {
                                                                  String message = String.format("User not found with email '%s'.", email);
                                                                  log.error(message);
                                                                  return new UsernameNotFoundException(message);
                                                              });

        return new org.springframework.security.core.userdetails.User(user.getId().toString(), user.getPasswordHash(), getAuthorityList(user));
    }
}
