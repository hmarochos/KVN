package ua.uhk.mois.chatbot.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CalendarUtils {

    public static int timeZoneOffset() {
        Calendar cal = Calendar.getInstance();
        return (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000);
    }

    public static String year() {
        Calendar cal = Calendar.getInstance();
        return String.valueOf(cal.get(Calendar.YEAR));
    }

    public static String date() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMMMMMM dd, yyyy");
        dateFormat.setCalendar(cal);
        return dateFormat.format(cal.getTime());
    }

    public static String date(String jformat, String locale, String timezone) {
        if (jformat == null)
            jformat = "EEE MMM dd HH:mm:ss zzz yyyy";
        if (locale == null)
            locale = Locale.US.getISO3Country();
        if (timezone == null)
            timezone = TimeZone.getDefault().getDisplayName();
        String dateAsString = new Date().toString();
        try {
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat(jformat);
            dateAsString = simpleDateFormat.format(new Date());
        } catch (Exception ex) {
            log.info("CalendarUtils.date Bad date: Format = {} Locale = {} Timezone = {}",
                     jformat, locale, timezone);
        }
        log.info("CalendarUtils.date: {}", dateAsString);
        return dateAsString;
    }
}
