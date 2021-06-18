package ua.uhk.mois.financialplanning.security.configuration;

import ua.uhk.mois.financialplanning.controller.path.UrlConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

/**
 * Resource in our context is the REST API which we have exposed for the crud operation. To access these resources,
 * client must be authenticated.In real-time scenarios, whenever an user tries to access these resources, the user will
 * be asked to provide his authenticity and once the user is authorized then he will be allowed to access these
 * protected resources.
 *
 * @author KVN
 * @since 17.03.2021 20:54
 */

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "resource_id";

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID).stateless(false);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf()
                .disable();

        http
                .exceptionHandling()
                .accessDeniedHandler(new OAuth2AccessDeniedHandler());

        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, UrlConstant.USER)
                .permitAll()
                .antMatchers(HttpMethod.POST, UrlConstant.TRANSACTION)
                .hasAuthority("ADMIN")
                .anyRequest()
                .authenticated();

        http
                .sessionManagement()
                .sessionFixation()
                .changeSessionId();

        http
                .headers()
                // Cache Control
                .cacheControl()
                // X-XSS-Protection
                .and()
                .xssProtection()
                // HTTP Strict Transport Security (HSTS)
                .and()
                .httpStrictTransportSecurity()
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000)
                // X-Frame-Options to allow any request from same domain
                .and()
                .frameOptions()
                .sameOrigin();
    }
}
