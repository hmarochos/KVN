package ua.uhk.mois.chatbot.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Months;
import org.joda.time.Years;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.LenientChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class Interval {

    public static final String AN_ERROR_OCCURRED = "An error occurred.";

    public static void test() {
        String date1 = "23:59:59.00";
        String date2 = "12:00:00.00";
        String format = "HH:mm:ss.SS";
        int hours = getHoursBetween(date2, date1, format);
        log.info("Hours = " + hours);
        date1 = "January 30, 2013";
        date2 = "August 2, 1960";
        format = "MMMMMMMMM dd, yyyy";
        int years = getYearsBetween(date2, date1, format);
        log.info("Years = " + years);
    }

    // http://docs.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
    public static int getHoursBetween(String date1, String date2, String format) {
        try {
            DateTimeFormatter fmt =
                    DateTimeFormat
                            .forPattern(format)
                            .withChronology(
                                    LenientChronology.getInstance(
                                            GregorianChronology.getInstance()));
            return Hours.hoursBetween(
                    fmt.parseDateTime(date1),
                    fmt.parseDateTime(date2)
                                     ).getHours();
        } catch (Exception ex) {
            log.error(AN_ERROR_OCCURRED, ex);
            return 0;
        }
    }

    public static int getYearsBetween(String date1, String date2, String format) {
        try {
            DateTimeFormatter fmt =
                    DateTimeFormat
                            .forPattern(format)
                            .withChronology(
                                    LenientChronology.getInstance(
                                            GregorianChronology.getInstance()));
            return Years.yearsBetween(
                    fmt.parseDateTime(date1),
                    fmt.parseDateTime(date2)
                                     ).getYears();
        } catch (Exception ex) {
            log.error(AN_ERROR_OCCURRED, ex);
            return 0;
        }
    }

    public static int getMonthsBetween(String date1, String date2, String format) {
        try {
            DateTimeFormatter fmt =
                    DateTimeFormat
                            .forPattern(format)
                            .withChronology(
                                    LenientChronology.getInstance(
                                            GregorianChronology.getInstance()));
            return Months.monthsBetween(
                    fmt.parseDateTime(date1),
                    fmt.parseDateTime(date2)
                                       ).getMonths();
        } catch (Exception ex) {
            log.error(AN_ERROR_OCCURRED, ex);
            return 0;
        }
    }

    public static int getDaysBetween(String date1, String date2, String format) {
        try {
            DateTimeFormatter fmt =
                    DateTimeFormat
                            .forPattern(format)
                            .withChronology(
                                    LenientChronology.getInstance(
                                            GregorianChronology.getInstance()));
            return Days.daysBetween(
                    fmt.parseDateTime(date1),
                    fmt.parseDateTime(date2)
                                   ).getDays();
        } catch (Exception ex) {
            log.error(AN_ERROR_OCCURRED, ex);
            return 0;
        }
    }
}
