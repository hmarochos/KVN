package cz.uhk.mois.financialplanning.security.configuration;

import cz.uhk.mois.financialplanning.configuration.AbsTestConfiguration;
import cz.uhk.mois.financialplanning.model.entity.user.User;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.EMAIL;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.PASSWORD;
import static cz.uhk.mois.financialplanning.configuration.user.UserTestSupport.createUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
class AuthorizationServerConfigTest extends AbsTestConfiguration {

    private static final String CLIENT_SECRET_PASSWORD = "MOIS-Financial-Planing-API-Secret-$&s6EY9CVr^dz#vS";

    @Autowired
    private WebApplicationContext context;

    /**
     * <i>https://howtodoinjava.com/spring-boot2/spring-boot-mockmvc-example/</i>
     */
    private MockMvc mvc;

    @Value("${spring.client.id}")
    private String clientId;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        log.info("Clear database before test.");
        clearDatabase();
    }

    @AfterEach
    void tearDown() {
        log.info("Clear database after test.");
        clearDatabase();
    }

    @Test
    void authSuccessEventListener_UserSignIn() throws Exception {
        // https://www.baeldung.com/oauth-api-testing-with-spring-mvc#2-obtaining-an-access-token
        log.info("Test user login to see if an access token is generated and last login date updated.");

        // Data preparation
        User user = createUser(true);
        userRepository.save(user);

        assertTrue(user.getCreatedAt().isEqual(user.getLastLogin()));
        assertDatabaseSize(1, 3);

        // Execution
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", EMAIL);
        params.add("password", PASSWORD);
        params.add("grant_type", "password");

        ResultActions resultActions
                = mvc.perform(post("/oauth/token")
                                      .params(params)
                                      .with(SecurityMockMvcRequestPostProcessors.httpBasic(clientId, CLIENT_SECRET_PASSWORD))
                                      .accept("application/json;charset=UTF-8"))
                     .andExpect(status().isOk())
                     .andExpect(content().contentType("application/json;charset=UTF-8"));

        // Verification
        assertDatabaseSize(1, 3);

        MockHttpServletResponse response = resultActions.andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        String resultString = response.getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        String access_token = jsonParser.parseMap(resultString).get("access_token").toString();
        String token_type = jsonParser.parseMap(resultString).get("token_type").toString();
        String refresh_token = jsonParser.parseMap(resultString).get("refresh_token").toString();
        String expires_in = jsonParser.parseMap(resultString).get("expires_in").toString();
        String scope = jsonParser.parseMap(resultString).get("scope").toString();
        String jti = jsonParser.parseMap(resultString).get("jti").toString();

        assertNotNull(access_token);
        assertTrue(access_token.startsWith("eyJhbG"));
        assertThat(access_token.length(), is(greaterThan(100)));

        assertEquals("bearer", token_type);

        assertNotNull(refresh_token);
        assertTrue(refresh_token.startsWith("eyJhbG"));
        assertThat(refresh_token.length(), is(greaterThan(100)));

        assertNotNull(expires_in);
        // Expires in should be between 1700 and 1800 seconds
        // It is set to 1800, but by the time that the request is made and received, the validity may be less
        long expiresIn = Long.parseLong(expires_in);
        assertThat(expiresIn, lessThanOrEqualTo(1800L));
        assertThat(expiresIn, greaterThan(1700L));

        assertEquals("read write trust", scope);

        assertNotNull(jti);
        assertThat(jti.length(), is(greaterThan(35)));

        // Check if the last login date has been updated
        Optional<User> optUserById = userRepository.findById(user.getId());
        assertTrue(optUserById.isPresent());

        User userById = optUserById.get();
        assertTrue(userById.getCreatedAt().isBefore(userById.getLastLogin()));
    }
}
