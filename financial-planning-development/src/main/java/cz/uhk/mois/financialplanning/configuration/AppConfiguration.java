package cz.uhk.mois.financialplanning.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Clock;

/**
 * @author Jan Krunčík
 * @since 15.03.2020 21:56
 */

@Configuration
public class AppConfiguration {

    @Value("${uni.bank.simulatin.api:https://mois-banking.herokuapp.com/v1/}")
    private String bankApiProUrl;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                        .baseUrl(bankApiProUrl);
    }
}
