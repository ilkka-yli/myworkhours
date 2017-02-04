package com.example.lgi.myworkhours;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        long plusminus = settings.getLong(getString(R.string.pref_plusminus_hours), 0);
        final EditText hoursInput = (EditText) findViewById(R.id.input_plusminus_hours);
        final EditText minsInput = (EditText) findViewById(R.id.input_plusminus_mins);
        hoursInput.setText(getHoursString(plusminus));
        minsInput.setText(getMinutesString(plusminus));
    }

    private String getHoursString(long millis) {
        int hours = (int) ((millis / (1000*60*60)) % 24);
        return Integer.toString(hours);
    }

    private String getMinutesString(long millis) {
        long abs = Math.abs(millis);
        int minutes = (int) ((abs / (1000*60)) % 60);
        return Integer.toString(minutes);
    }

    public void onSettingsSaveButtonClick(View view) {
        final EditText hoursInput = (EditText) findViewById(R.id.input_plusminus_hours);
        final EditText minsInput = (EditText) findViewById(R.id.input_plusminus_mins);
        int hours, mins;
        try {
            hours = Integer.parseInt(hoursInput.getText().toString());
            mins = Integer.parseInt(minsInput.getText().toString());
            if (mins < 0 || mins > 59) {
                DialogUtil.showAlertDialog(this, getString(R.string.title_invalid_time),
                        getString(R.string.alert_invalid_minutes));
                return;
            }
            long factor = hours < 0 ? -1 : 1;
            long hoursAsMillis = Math.abs(hours) * 3600000;
            long minsAsMillis = Math.abs(mins) * 60000;
            long millis = (hoursAsMillis + minsAsMillis) * factor;

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(getString(R.string.pref_plusminus_hours), millis);
            editor.commit();

            finish();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
