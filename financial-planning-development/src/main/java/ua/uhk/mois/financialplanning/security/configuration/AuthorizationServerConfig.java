package ua.uhk.mois.financialplanning.security.configuration;

import ua.uhk.mois.financialplanning.model.entity.user.User;
import ua.uhk.mois.financialplanning.response.Failure;
import ua.uhk.mois.financialplanning.response.FailureSupport;
import ua.uhk.mois.financialplanning.service.support.UserSupport;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.ZonedDateTime;

/**
 * This is to set up token generation end point i.e. if user provide the properties security.oauth2.client.client-id and
 * security.oauth2.client.client-secret, Spring will give him an authentication server, providing standard Oauth2 tokens
 * at the endpoint /oauth/token
 *
 * @author KVN
 * @since 17.03.2021 21:03
 */

@Configuration
@EnableAuthorizationServer
@Log4j2
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String IMPLICIT = "implicit";
    private static final String SCOPE_READ = "read";
    private static final String SCOPE_WRITE = "write";
    private static final String TRUST = "trust";

    private final AuthenticationManager authenticationManager;
    private final UserSupport userSupport;
    private final Clock clock;

    @Value("${spring.client.id:MOIS-Financial-Planing-API}")
    private String clientId;
    @Value("${spring.client.secret:$2a$10$SqAtW9pU7eritCkLI3.duulCJkzHVzEGUGM6AhSGbPaomTzuTPGMq}")
    private String clientSecret;
    @Value("${access.token.validity.seconds:3600}")
    private int accessTokenValiditySeconds;
    @Value("${refresh.token.validity.seconds:36000}")
    private int refreshTokenValiditySeconds;

    public AuthorizationServerConfig(AuthenticationManager authenticationManager, UserSupport userSupport, Clock clock) {
        this.authenticationManager = authenticationManager;
        this.userSupport = userSupport;
        this.clock = clock;
    }

    private static Either<Failure, Long> getSignedInUserId(AuthenticationSuccessEvent authorizedEvent) {
        return Try.of(() -> {
            Object principal = authorizedEvent.getAuthentication().getPrincipal();
            if (!(principal instanceof org.springframework.security.core.userdetails.User)) {
                return null;
            }
            String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            if (username.matches("^\\d+$")) {
                log.info("The user id '{}' was successfully retrieved from the user's successful login event.", username);
                return Long.valueOf(username);
            }
            log.info("The user id (database entity) could not be retrieved from the user login '{}'.", username);
            return null;
        })
                  .toEither()
                  .mapLeft(throwable -> getSignedInUserIdFailure(throwable, authorizedEvent))
                  .flatMap(AuthorizationServerConfig::checkUserIdPresence);
    }

    private static Failure getSignedInUserIdFailure(Throwable throwable, AuthenticationSuccessEvent authorizedEvent) {
        String logMsg = String.format("There was an error trying to get the id of the currently signed-in user. %s", authorizedEvent);
        log.error(logMsg, throwable);
        String message = "There was an error trying to get the id of the currently signed-in user.";
        return FailureSupport.createFailure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private static Either<Failure, Long> checkUserIdPresence(Long id) {
        log.info("Check the found user id '{}'.", id);
        return id == null ? Either.left(FailureSupport.createFailure(HttpStatus.NOT_FOUND, "Failed to get user id.")) : Either.right(id);
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("as466gf");
        return converter;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer configurer) throws Exception {
        configurer
                .inMemory()
                .withClient(clientId)
                .secret(clientSecret)
                .authorizedGrantTypes(GRANT_TYPE_PASSWORD, AUTHORIZATION_CODE, REFRESH_TOKEN, IMPLICIT)
                .scopes(SCOPE_READ, SCOPE_WRITE, TRUST)
                .accessTokenValiditySeconds(accessTokenValiditySeconds)
                .refreshTokenValiditySeconds(refreshTokenValiditySeconds);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.tokenStore(tokenStore())
                 .authenticationManager(authenticationManager)
                 .accessTokenConverter(accessTokenConverter());
    }

    @Transactional
    @EventListener
    public void authSuccessEventListener(AuthenticationSuccessEvent authorizedEvent) {
        log.info("Update last login date. {}", authorizedEvent);
        getSignedInUserId(authorizedEvent)
                .flatMap(userSupport::findUserById)
                .map(this::updateLastLogin)
                .flatMap(userSupport::saveUser)
                .peek(user -> log.info("Last login successfully changed. {}", user));
    }

    private User updateLastLogin(User user) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        user.setLastLogin(now);
        return user;
    }
}
