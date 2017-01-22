package com.example.lgi.myworkhours;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ModifyRecordActivity extends AppCompatActivity {

    private Calendar startDateTime = null;
    private Calendar endDateTime = null;
    private int recordIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_record);

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();

        Bundle extras = getIntent().getExtras();
        startDateTime.setTimeInMillis(extras.getLong("startTime", Calendar.getInstance().getTimeInMillis()));
        endDateTime.setTimeInMillis(extras.getLong("endTime", Calendar.getInstance().getTimeInMillis()));
        recordIndex = extras.getInt("recordIndex", -1);

        setDateFieldProperties();
        boolean fillEndTime = extras.containsKey("endTime") && extras.getLong("endTime") > 0;
        setTimeFieldProperties(extras.containsKey("startTime"), fillEndTime);
        setTitle();
    }

    private void setTitle() {
        if (recordIndex == -1) {
            setTitle(R.string.label_add_worktime);
        } else {
            setTitle(R.string.label_modify_worktime);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setTimeFieldProperties(boolean fillStartTime, boolean fillEndTime) {
        final EditText startTimeInput = (EditText) findViewById(R.id.input_start_time);
        final EditText endTimeInput = (EditText) findViewById(R.id.input_end_time);

        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
        if (fillStartTime) {
            startTimeInput.setText(sdf.format(startDateTime.getTime()));
        } else {
            startTimeInput.setText("");
        }

        if (fillEndTime) {
            endTimeInput.setText(sdf.format(endDateTime.getTime()));
        } else {
            endTimeInput.setText("");
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText) v).setText("");
            }
        };
        startTimeInput.setOnClickListener(clickListener);
        endTimeInput.setOnClickListener(clickListener);
    }

    private void setDateFieldProperties() {
        final Button startDateBtn = (Button) findViewById(R.id.button_start_date);
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format));
        startDateBtn.setText(sdf.format(startDateTime.getTime()));

        final DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                startDateTime.set(Calendar.YEAR, year);
                startDateTime.set(Calendar.MONTH, monthOfYear);
                startDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                endDateTime.set(Calendar.YEAR, year);
                endDateTime.set(Calendar.MONTH, monthOfYear);
                endDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(startDateBtn, startDateTime);
            }
        };

        startDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ModifyRecordActivity.this, R.style.DatePickerTheme, startDateListener,
                        startDateTime.get(Calendar.YEAR), startDateTime.get(Calendar.MONTH),
                        startDateTime.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
                datePickerDialog.show();
            }
        });
    }

    private void updateLabel(Button btn, Calendar date) {
        String myFormat = getString(R.string.date_format);
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        btn.setText(sdf.format(date.getTime()));
    }

    public void onSaveButtonClick(View view) {
        EditText startTimeInput = (EditText) findViewById(R.id.input_start_time);
        EditText endTimeInput = (EditText) findViewById(R.id.input_end_time);
        insertHoursAndMinutes(startDateTime, startTimeInput.getText().toString());
        insertHoursAndMinutes(endDateTime, endTimeInput.getText().toString());
        if (recordIndex == -1) {
            saveRecord();
        } else {
            modifyRecord();
        }
    }

    public void onCancelButtonClick(View view) {
        finish();
    }

    private void insertHoursAndMinutes(Calendar cal, String text) {
        int[] hoursAndMins = new int[0];
        try {
            hoursAndMins = CalendarUtil.textToHoursAndMinutes(this, text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cal.set(Calendar.HOUR_OF_DAY, hoursAndMins[0]);
        cal.set(Calendar.MINUTE, hoursAndMins[1]);
    }

    private void modifyRecord() {
        Record rec = new Record(startDateTime.getTimeInMillis(), endDateTime.getTimeInMillis());
        if (RecordsResource.replaceRecord(this, recordIndex, rec)) {
            finish();
        }
    }

    private void saveRecord() {
        Record rec = new Record(startDateTime.getTimeInMillis(), endDateTime.getTimeInMillis());
        if (RecordsResource.addRecord(this, rec)) {
            finish();
        }
    }
}
