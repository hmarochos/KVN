package ua.uhk.mois.financialplanning.controller.path;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author KVN
 * @since 15.03.2021 19:41
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlConstant {

    public static final String USER = "/user";
    public static final String USER_CHANGE_PASSWORD = "/change-password";

    public static final String WISH = "/wish";
    public static final String WISH_DELETE_ALL = "/delete-all";
    public static final String WISH_LIST = "/list";
    public static final String WISH_CHANGE_PRIORITY = "/change-priority";

    public static final String TRANSACTION = "/transaction";
    public static final String TRANSACTION_GET_BY_DATE_INTERVAL = "/get-by-date-interval";
    public static final String TRANSACTION_LAST_MONTH_TRANSACTIONS_OVERVIEW = "/last-month-transactions-overview";

    public static final String FINANCIAL_PLANNING = "/financial-planning";
    public static final String FINANCIAL_PLANNING_MONTHLY_PLAN = "/monthly-plan";
    public static final String FINANCIAL_PLANNING_ANNUAL_PLAN = "/annual-plan";
}
