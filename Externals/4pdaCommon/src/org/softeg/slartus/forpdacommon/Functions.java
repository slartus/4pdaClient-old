package org.softeg.slartus.forpdacommon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by slinkin on 26.08.13.
 */
public class Functions {
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    public static SimpleDateFormat parseDateTimeFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");

    public static String getToday() {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        return dateFormat.format(nowCalendar.getTime());
    }

    public static String getYesterToday() {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        nowCalendar.add(Calendar.DAY_OF_MONTH, -1);
        return dateFormat.format(nowCalendar.getTime());
    }

    public static String getForumDateTime(Date date) {

        if (date == null) return "";
        return parseDateTimeFormat.format(date);
    }

    public static Date parseForumDateTime(String dateTime, String today, String yesterday) {
        try {
            Date res = parseDateTimeFormat.parse(dateTime.toString().replace("Сегодня", today).replace("Вчера", yesterday));
            if (res.getYear() < 100)
                res.setYear(2000 + res.getYear());
            return res;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
