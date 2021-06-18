package ua.uhk.mois.financialplanning.service.bank;

import ua.uhk.mois.financialplanning.model.dto.transaction.bank.AddBankTransactionDtoIn;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.BankTransactionDtoOut;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Communication with the banking API (simulation) provided by Unicorn to meet the requirements for communication with
 * the external API.
 *
 * @author KVN
 * @since 03.04.2021 14:59
 */

@Component
@Log4j2
public class BankTransactionServiceImpl implements BankTransactionService {

    private final WebClient.Builder webClientBuilder;

    public BankTransactionServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<BankTransactionDtoOut> add(AddBankTransactionDtoIn dtoIn) {
        log.info("Sending a transaction to a bank API (simulation) to create / save in a database. {}", dtoIn);

        return webClientBuilder.build()
                               .post()
                               .uri("transaction")
                               .body(BodyInserters.fromValue(dtoIn))
                               .retrieve()
                               .bodyToMono(BankTransactionDtoOut.class);
    }

    @Override
    public Mono<List<UniTransaction>> getByDateInterval(ZonedDateTime dateFrom, ZonedDateTime dateTo, Long accountId) {
        log.info("Retrieve transactions with account id '{}' that were created at between < {} ; {} >.", accountId, dateFrom, dateTo);

        String uri = "transaction/findByDate?dateFrom=" +
                dateFrom.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT) +
                "&dateTo=" +
                dateTo.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT) +
                "&accountId=" +
                accountId;

        return webClientBuilder.build()
                               .get()
                               .uri(uri)
                               .retrieve()
                               .bodyToMono(new ParameterizedTypeReference<List<UniTransaction>>() {
                               });
    }
}
