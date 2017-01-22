package com.example.lgi.myworkhours;

import android.support.v7.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by lgi on 21.1.2017.
 */

public class CalendarUtil {

    public static int[] textToHoursAndMinutes(AppCompatActivity act, String text) throws Exception {
        if (text.isEmpty()) {
            DialogUtil.showAlertDialog(act, act.getString(R.string.title_invalid_time),
                    act.getString(R.string.alert_invalid_time_format));
            throw new Exception();
        }
        int hours = 0;
        int mins = 0;
        String hoursStr;
        String minsStr;
        if (text.contains(".")) {
            String[] split = text.split("\\.");
            if (split.length != 2) {
                DialogUtil.showAlertDialog(act, act.getString(R.string.title_invalid_time),
                        act.getString(R.string.alert_invalid_time_format));
                throw new Exception();
            }
            hoursStr = split[0];
            minsStr = split[1];
        } else {
            hoursStr = text;
            minsStr = "0";
        }
        try {
            hours = Integer.parseInt(hoursStr);
            mins = Integer.parseInt(minsStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (hours < 0 || hours > 23) {
            DialogUtil.showAlertDialog(act, act.getString(R.string.title_invalid_time),
                    act.getString(R.string.alert_invalid_hours));
            throw new Exception();
        }

        if (mins < 0 || mins > 23) {
            DialogUtil.showAlertDialog(act, act.getString(R.string.title_invalid_time),
                    act.getString(R.string.alert_invalid_minutes));
            throw new Exception();
        }

        return new int[]{hours, mins};
    }

    public static String getFormattedString(Calendar cal, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(cal.getTime());
    }

    public static void resetSecondsAndMillis(Calendar cal) {
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public static Calendar convertTextToCalendar(AppCompatActivity act, String text) {
        int[] hoursAndMins = new int[0];
        try {
            hoursAndMins = textToHoursAndMinutes(act, text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hoursAndMins[0]);
        cal.set(Calendar.MINUTE, hoursAndMins[1]);
        return cal;
    }

    public static String getHoursMinutesDurationString(long millis, boolean showPrefix) {
        long abs = Math.abs(millis);
        int minutes = (int) ((abs / (1000*60)) % 60);
        int hours = (int) ((abs / (1000*60*60)) % 24);
        String prefix = "";
        if (showPrefix) {
            prefix = millis < 0 ? "- " : "+ ";
        }
        return prefix + hours + "h " + minutes + "min";
    }
}
