package com.example.lgi.myworkhours;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ModifyRecordActivity.class);
                intent.putExtra("startTime", Calendar.getInstance().getTimeInMillis());
                intent.putExtra("endTime", Calendar.getInstance().getTimeInMillis());
                startActivity(intent);
            }
        });

        final Button startWorkBtn = (Button) getContentActivity().findViewById(R.id.button_start_work);
        final Button endWorkBtn = (Button) getContentActivity().findViewById(R.id.button_end_work);
        final EditText startTimeInput = (EditText) getContentActivity().findViewById(R.id.input_start_time);
        final EditText endTimeInput = (EditText) getContentActivity().findViewById(R.id.input_end_time);
        startWorkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleStatus(true);

                String text = startTimeInput.getText().toString();
                Calendar workStartTime;
                Record record;
                if (text.isEmpty()) {
                    record = addRecord(Calendar.getInstance(), null);
                } else {
                    record = addRecord(CalendarUtil.convertTextToCalendar(MainActivity.this, text), null);
                }

                if (record != null) {
                    fillStartTime(record);
                    refreshWorktimeToday();
                }
            }
        });

        endWorkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleStatus(false);
                String text = endTimeInput.getText().toString();
                if (text.isEmpty()) {
                    saveWorkTime(Calendar.getInstance());
                } else {
                    saveWorkTime(CalendarUtil.convertTextToCalendar(MainActivity.this, text));
                }
                refreshWorktimeToday();
                refreshPlusMinus();
                clearTimeFields();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshWorktimeToday();
        refreshStatus();
        refreshPlusMinus();

        EditText endTimeInput = (EditText) getContentActivity().findViewById(R.id.input_end_time);
        if (!endTimeInput.hasFocus()) {
            endTimeInput.setText("");
        }
    }

    private View getContentActivity() {
        return (View) findViewById(R.id.content_main_activity);
    }

    private void clearTimeFields() {

        EditText startTimeInput = (EditText) getContentActivity().findViewById(R.id.input_start_time);
        startTimeInput.setText("");

        EditText endTimeInput = (EditText) getContentActivity().findViewById(R.id.input_end_time);
        endTimeInput.setText("");
    }

    private void toggleStatus(boolean atWork) {
        final Button startWorkBtn = (Button) getContentActivity().findViewById(R.id.button_start_work);
        final Button endWorkBtn = (Button) getContentActivity().findViewById(R.id.button_end_work);
        final EditText endTimeInput = (EditText) getContentActivity().findViewById(R.id.input_end_time);
        final EditText startTimeInput = (EditText) getContentActivity().findViewById(R.id.input_start_time);
        final TextView statusValueLabel = (TextView) getContentActivity().findViewById(R.id.label_status_value);

        startWorkBtn.setEnabled(!atWork);
        startTimeInput.setEnabled(!atWork);
        endWorkBtn.setEnabled(atWork);
        endTimeInput.setEnabled(atWork);

        if (atWork) {
            statusValueLabel.setText(R.string.label_in);
            statusValueLabel.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        } else {
            statusValueLabel.setText(R.string.label_out);
            statusValueLabel.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem shareItem = menu.findItem(R.id.action_export_records);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Record addRecord(Calendar startTime, Calendar endTime) {
        long endMillis = endTime == null ? 0 : endTime.getTimeInMillis();
        Record rec = new Record(startTime.getTimeInMillis(), endMillis);
        if (RecordsResource.addRecord(this, rec)) {
            return rec;
        }
        return null;
    }

    private void saveWorkTime(Calendar endTime) {
        long dateId = RecordsResource.getDateId(Calendar.getInstance().getTimeInMillis());
        Record rec = RecordsResource.getOpenRecord(this, dateId);
        if (rec == null) {
            DialogUtil.showAlertDialog(this, getString(R.string.title_missing_open_record), getString(R.string.alert_missing_open_record));
        } else {
            rec.setEnd(endTime.getTimeInMillis());
            try {
                RecordsResource.replaceRecord(this, rec);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshStatus() {
        long dateId = RecordsResource.getDateId(Calendar.getInstance().getTimeInMillis());
        Record openRecord = RecordsResource.getOpenRecord(this, dateId);
        toggleStatus(openRecord != null);

        if (openRecord != null) {
           fillStartTime(openRecord);
        }
    }

    private void fillStartTime(Record record) {
        final EditText startTimeInput = (EditText) getContentActivity().findViewById(R.id.input_start_time);
        startTimeInput.setText(RecordsResource.getStartTimeString(record));
    }

    private void refreshPlusMinus() {
        long totalWorkTime = 0;
        int workDays;
        List<Long> uniqueDates = new ArrayList<>();
        for (Record rec : RecordsResource.getRecords(this)) {
            if (rec.getEnd() == 0) {
                continue;
            }
            totalWorkTime += (rec.getEnd() - rec.getStart());
            Long dateId = RecordsResource.getDateId(rec.getStart());
            if (!uniqueDates.contains(dateId)) {
                uniqueDates.add(dateId);
            }
        }
        long exceptedTotal = uniqueDates.size() * 8 * 60 * 60 * 1000;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        long presetPlusMinus = settings.getLong(getString(R.string.pref_plusminus_hours), 0);
        long plusMinus = (totalWorkTime - exceptedTotal) + presetPlusMinus;
        final TextView label = (TextView) getContentActivity().findViewById(R.id.label_plusminus_value);
        label.setText(CalendarUtil.getHoursMinutesDurationString(plusMinus, true));
        if (plusMinus > 0) {
            label.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        } else if (plusMinus < 0) {
            label.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
        }
    }

    private void refreshWorktimeToday() {
        final TextView timeLabel = (TextView) getContentActivity().findViewById(R.id.label_hours_today_value);
        timeLabel.setText(CalendarUtil.getHoursMinutesDurationString(RecordsResource.getWorkTimeToday(this), false));
    }

    public void onEditRecordsClick(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, EditRecordsActivity.class);
        startActivity(intent);
    }

    public void onSettingsClick(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onExportClick(MenuItem item) {

        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        intentShareFile.setType("application/csv");
        intentShareFile.putExtra(Intent.EXTRA_STREAM, RecordsResource.getRecordsFile(this));
        String subject = getString(R.string.app_name) + " " + getString(R.string.label_backup);
        intentShareFile.putExtra(Intent.EXTRA_SUBJECT, subject);
        intentShareFile.putExtra(Intent.EXTRA_TEXT, subject + " " + getString(R.string.label_attached));
        intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intentShareFile.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        setShareIntent(intentShareFile);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
}
