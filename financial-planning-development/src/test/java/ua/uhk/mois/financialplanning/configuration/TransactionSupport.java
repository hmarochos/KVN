package ua.uhk.mois.financialplanning.configuration;

import ua.uhk.mois.financialplanning.model.dto.transaction.AddTransactionDtoIn;
import ua.uhk.mois.financialplanning.model.dto.transaction.AdditionalInfoDomestic;
import ua.uhk.mois.financialplanning.model.dto.transaction.Direction;
import ua.uhk.mois.financialplanning.model.dto.transaction.Value;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.TransactionType;
import ua.uhk.mois.financialplanning.model.entity.transaction.PartyAccount;
import ua.uhk.mois.financialplanning.model.entity.transaction.Transaction;
import ua.uhk.mois.financialplanning.model.entity.wish.Currency;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author KVN
 * @since 09.04.2021 18:03
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionSupport {

    public static List<Transaction> createTransactionList(int start, int end, Long accountId, Direction direction) {
        return IntStream.range(start, end)
                        .mapToObj(i -> createTransaction(i, accountId, direction))
                        .collect(Collectors.toList());
    }

    private static Transaction createTransaction(int day, Long accountId, Direction direction) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);

        ua.uhk.mois.financialplanning.model.entity.transaction.Value value = new ua.uhk.mois.financialplanning.model.entity.transaction.Value();
        value.setAmount(BigDecimal.valueOf(500L));
        value.setCurrency(Currency.uaK);
        value.setTransaction(transaction);
        transaction.setValue(value);

        PartyAccount partyAccount = new PartyAccount();
        partyAccount.setAccountNumber(accountId.toString());
        partyAccount.setBankCode("456");
        partyAccount.setTransaction(transaction);
        transaction.setPartyAccount(partyAccount);

        transaction.setPartyDescription("Test description.");
        transaction.setDirection(direction);
        transaction.setTransactionType(TransactionType.CARD);

        ZonedDateTime dateTime = getDateTime(day);
        transaction.setValueDate(dateTime);
        transaction.setBookingDate(dateTime);

        ua.uhk.mois.financialplanning.model.entity.transaction.AdditionalInfoDomestic additionalInfoDomestic = new ua.uhk.mois.financialplanning.model.entity.transaction.AdditionalInfoDomestic();
        additionalInfoDomestic.setConstantSymbol("123");
        additionalInfoDomestic.setVariableSymbol("456");
        additionalInfoDomestic.setSpecificSymbol("789");
        transaction.setAdditionalInfoDomestic(additionalInfoDomestic);

        return transaction;
    }

    private static ZonedDateTime getDateTime(int dayInMonth) {
        return ZonedDateTime.of(2021, 3, dayInMonth, 6, 6, 6, 0, ZoneId.systemDefault());
    }

    public static AddTransactionDtoIn createAddTransactionDtoIn() {
        AddTransactionDtoIn dtoIn = new AddTransactionDtoIn();
        dtoIn.setAccountId(123L);
        dtoIn.setValue(new Value(BigDecimal.valueOf(1000L), Currency.uaK));
        dtoIn.setBankCode(123L);
        dtoIn.setPartyDescription("Test description.");
        dtoIn.setDirection(Direction.OUTGOING);
        dtoIn.setTransactionType(TransactionType.CARD);
        dtoIn.setPaymentDate(ZonedDateTime.now(ZoneId.systemDefault()));
        dtoIn.setAdditionalInfoDomestic(createAdditionalInfoDomestic());
        return dtoIn;
    }

    private static AdditionalInfoDomestic createAdditionalInfoDomestic() {
        AdditionalInfoDomestic additionalInfoDomestic = new AdditionalInfoDomestic();
        additionalInfoDomestic.setConstantSymbol("1234");
        additionalInfoDomestic.setVariableSymbol("123456789");
        additionalInfoDomestic.setSpecificSymbol("987654321");
        return additionalInfoDomestic;
    }
}
