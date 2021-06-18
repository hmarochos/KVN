package cz.uhk.mois.financialplanning.model.dto.transaction.bank;

/**
 * @author Jan Krunčík
 * @since 03.04.2020 23:06
 */

public enum TransactionType {

    PAYMENT_HOME,
    PAYMENT_ABROAD,
    PAYMENT_PERSONAL,
    PAYMENT_ACCOUNT,
    STANDING_ORDER,
    SAVING,
    DIRECT_DEBIT,
    DIRECT_DEBIT_SIPO,
    CARD,
    CASH,
    FEE,
    TAX,
    INTEREST,
    INSURANCE,
    LOAN,
    MORTGAGE,
    SAZKA,
    OTHER,
    BLOCKING
}
