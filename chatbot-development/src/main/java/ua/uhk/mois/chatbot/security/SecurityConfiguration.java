package ua.uhk.mois.chatbot.security;

import ua.uhk.mois.chatbot.controller.path.UrlConstant;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author KVN
 * @since 28.03.2021 2:35
 */

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf()
                .disable();

        // Allowed URL
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, UrlConstant.CHAT_BOT + UrlConstant.CHAT_BOT_QUESTION)
                .permitAll()
                .anyRequest()
                .authenticated();

        // Headers
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
