package com.example.lgi.myworkhours;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lgi on 25.10.2016.
 */

public class RecordsResource {

    public static final String recordsFile = "MyWorkHours.csv";
    public static final String tempFile = "Temp.csv";
    private static List<Record> records;

    public static List<Record> getRecords(AppCompatActivity act) {
        if (records == null) {
            readRecords(act);
            Collections.sort(records, new Comparator<Record>() {
                @Override
                public int compare(Record o1, Record o2) {
                    return Long.compare(o1.getStart(), o2.getStart());
                }
            });
        }
        return records;
    }

    private static List<Record> getRecords(AppCompatActivity act, long dateId) {
        List<Record> filtered = new ArrayList<>();
        for (Record rec : getRecords(act)) {
            long id = getDateId(rec.getStart());
            if (dateId != id) {
                continue;
            }
            filtered.add(rec);
        }
        return filtered;
    }

    public static long getWorkTimeToday(AppCompatActivity act) {
        return getWorkTime(act, getDateId(Calendar.getInstance().getTimeInMillis()));
    }

    private static long getWorkTime(AppCompatActivity act, long dateId) {
        long workTime = 0;
        for (Record rec : getRecords(act, dateId)) {
            long end = rec.getEnd() > 0 ? rec.getEnd() : Calendar.getInstance().getTimeInMillis();
            workTime += (end - rec.getStart());
        }
        return workTime;
    }

    public static Record getOpenRecord(AppCompatActivity act, long dateId) {
        for (Record rec : getRecords(act, dateId)) {
            if (rec.getEnd() == 0) {
                return rec;
            }
        }
        return null;
    }

    public static long getDateId(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        String ymd = Integer.toString(y) + Integer.toString(m) + Integer.toString(d);
        return Long.parseLong(ymd);
    }

    public static Uri getRecordsFile(AppCompatActivity act) {
        File file = new File(act.getFilesDir(), tempFile);
        FileOutputStream outputStream;
        String data = "";
        for (Record rec : getRecords(act)) {
            data += generateCsvRow(rec);
        }
        try {
            outputStream = act.openFileOutput(tempFile, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Uri contentUri = FileProvider.getUriForFile(act, "com.example.lgi.myworkhours", file);
        return contentUri;
    }

    public static void readRecords(AppCompatActivity act) {
        records = new ArrayList<>();

        File file = new File(act.getFilesDir(), recordsFile);

        if (!file.exists()) {
            return;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] row = line.split(";");
                Record rec = new Record();
                rec.setStart(Long.parseLong(row[0]));
                rec.setEnd(Long.parseLong(row[1]));
                records.add(rec);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                // handle exception
            }
        }
    }

    public static void removeRecord(AppCompatActivity act, Record rec) {
        if (records == null) {
            readRecords(act);
        }
        records.remove(rec);
        writeRecords(act);
    }

    public static boolean replaceRecord(AppCompatActivity act, int index, Record record) {
        if (!validateRecord(record, act, index)) {
            return false;
        }

        if (records == null) {
            readRecords(act);
        }
        records.set(index, record);
        writeRecords(act);
        return true;
    }

    public static boolean replaceRecord(AppCompatActivity act, Record record) {
        return replaceRecord(act, getRecords(act).indexOf(record), record);
    }

    private static boolean validateRecord(Record rec, AppCompatActivity act, int currentIndex) {
        if (rec.getEnd() > 0 && rec.getStart() >= rec.getEnd()) {
            DialogUtil.showAlertDialog(act, act.getString(R.string.title_invalid_record), act.getString(R.string.alert_end_time_before));
            return false;
        }

        List<Record> currentRecords = getRecords(act);
        for (int i = 0; i < currentRecords.size(); i++) {
            Record current = currentRecords.get(i);
            if (currentIndex > -1 && currentIndex == i) {
                continue;
            }

            if (rec.getEnd() > 0 && current.getEnd() > 0) {
                if (rec.getStart() <= current.getEnd() && rec.getEnd() >= current.getStart()) {
                    DialogUtil.showAlertDialog(act, act.getString(R.string.title_overlapping_record),
                            act.getString(R.string.alert_overlapping_record) + " (" + generateDatetimeRow(rec) + ").");
                    return false;
                }
            }
        }

        Calendar cal = getStartTime(rec);
        CalendarUtil.resetSecondsAndMillis(cal);
        rec.setStart(cal.getTimeInMillis());

        if (rec.getEnd() > 0) {
            cal = getEndTime(rec);
            CalendarUtil.resetSecondsAndMillis(cal);
            rec.setEnd(cal.getTimeInMillis());
        }

        return true;
    }

    public static boolean addRecord(AppCompatActivity act, Record rec) {
        if (!validateRecord(rec, act, -1)) {
            return false;
        }

        File file = new File(act.getFilesDir(), recordsFile);
        FileOutputStream outputStream;
        String row = generateRow(rec);
        try {
            outputStream = act.openFileOutput(recordsFile, Context.MODE_APPEND);
            outputStream.write(row.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        records.add(rec);
        return true;
    }

    private static String generateRow(Record rec) {
        return rec.getStart() + ";" + rec.getEnd() + ";" + "\n";
    }

    private static String generateCsvRow(Record rec) {
        return getStartDateString(rec) + ";" + getStartTimeString(rec) + ";" + getEndTimeString(rec) + ";" + "\n";
    }

    public static String generateDatetimeRow(Record rec) {
        return getStartDateString(rec) + " " + getStartTimeString(rec) + " - " + getEndTimeString(rec);
    }

    private static void writeRecords(AppCompatActivity act) {
        File file = new File(act.getFilesDir(), recordsFile);
        FileOutputStream outputStream;
        try {
            outputStream = act.openFileOutput(recordsFile, Context.MODE_PRIVATE);
            for (Record rec : records) {
                outputStream.write(generateRow(rec).getBytes());
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getStartDateString(Record rec) {
        return CalendarUtil.getFormattedString(getStartTime(rec), "dd.MM.yyyy");
    }

    public static String getEndDateString(Record rec) {
        return CalendarUtil.getFormattedString(getEndTime(rec), "dd.MM.yyyy");
    }

    public static String getStartTimeString(Record rec) {
        return CalendarUtil.getFormattedString(getStartTime(rec), "HH.mm");
    }

    public static String getEndTimeString(Record rec) {
        if (rec.getEnd() == 0) {
            return "-";
        }
        return CalendarUtil.getFormattedString(getEndTime(rec), "HH.mm");
    }

    public static Calendar getStartTime(Record rec) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(rec.getStart());
        return cal;
    }

    public static Calendar getEndTime(Record rec) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(rec.getEnd());
        return cal;
    }

}
