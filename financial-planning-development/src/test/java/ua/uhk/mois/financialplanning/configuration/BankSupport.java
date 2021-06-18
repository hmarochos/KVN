package ua.uhk.mois.financialplanning.configuration;

import ua.uhk.mois.financialplanning.model.dto.transaction.AdditionalInfoDomestic;
import ua.uhk.mois.financialplanning.model.dto.transaction.Direction;
import ua.uhk.mois.financialplanning.model.dto.transaction.Value;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.PartyAccount;
import ua.uhk.mois.financialplanning.model.dto.transaction.bank.TransactionType;
import ua.uhk.mois.financialplanning.model.entity.wish.Currency;
import ua.uhk.mois.financialplanning.service.bank.UniTransaction;
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
 * @since 24.04.2021 0:31
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankSupport {

    private static final ZonedDateTime ZONED_DATE_TIME = ZonedDateTime.of(2021, 1, 5, 15, 30, 35, 0, ZoneId.systemDefault());

    public static List<UniTransaction> createDummyBankTransactionDtoOutList(Long accountId) {
        return IntStream.range(0, 2)
                        .mapToObj(i -> createBankTransactionDtoOut(accountId))
                        .collect(Collectors.toList());
    }

    private static UniTransaction createBankTransactionDtoOut(Long accountId) {
        UniTransaction uniTransaction = new UniTransaction();
        uniTransaction.setAccountId(accountId);
        uniTransaction.setValue(new Value(BigDecimal.valueOf(1000L), Currency.uaK));

        PartyAccount partyAccount = new PartyAccount();
        partyAccount.setAccountNumber(accountId.toString());
        partyAccount.setBankCode("123");
        uniTransaction.setPartyAccount(partyAccount);

        uniTransaction.setPartyDescription("Test description.");
        uniTransaction.setDirection(Direction.OUTGOING);
        uniTransaction.setTransactionType(TransactionType.CASH);
        uniTransaction.setValueDate(ZONED_DATE_TIME);
        uniTransaction.setBookingDate(ZONED_DATE_TIME);

        AdditionalInfoDomestic additionalInfoDomestic = new AdditionalInfoDomestic();
        additionalInfoDomestic.setConstantSymbol("123");
        additionalInfoDomestic.setVariableSymbol("456");
        additionalInfoDomestic.setSpecificSymbol("789");
        uniTransaction.setAdditionalInfoDomestic(additionalInfoDomestic);

        return uniTransaction;
    }
}
