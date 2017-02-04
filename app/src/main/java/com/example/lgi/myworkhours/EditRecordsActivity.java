package com.example.lgi.myworkhours;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditRecordsActivity extends AppCompatActivity {

    private Intent editRecordIntent;
    private int selectedMonth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_records);

        populateMonthsSpinner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        listRecords(selectedMonth);
    }

    private void populateMonthsSpinner() {
        Calendar cal = Calendar.getInstance();
        final Spinner spin = (Spinner) findViewById(R.id.spinner_months);
        final SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        String[] arr = new String[13];
        arr[0] = getString(R.string.label_all_recods);
        for (int i = 1; i < 13; i++) {
            arr[i] = sdf.format(cal.getTime());
            cal.add(Calendar.MONTH, -1);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arr);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spin.setAdapter(adapter);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedMonth = -1;
                    listRecords(selectedMonth);
                    return;
                }

                Calendar cal = Calendar.getInstance();
                try {
                    cal.setTime(sdf.parse(spin.getSelectedItem().toString()));
                    selectedMonth = cal.get(Calendar.MONTH);
                    listRecords(selectedMonth);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            final Spinner spin = (Spinner) findViewById(R.id.spinner_months);
            spin.setSelection(spin.getSelectedItemPosition());
        }
    }

    private void listRecords(final int monthNum) {
        LinearLayout recordsLayout = (LinearLayout) findViewById(R.id.records_layout);
        recordsLayout.removeAllViews();

        for (final Record record : RecordsResource.getRecords(this)) {
            Calendar start = RecordsResource.getStartTime(record);
            Calendar end = RecordsResource.getEndTime(record);
            if (monthNum > -1 && start.get(Calendar.MONTH) != monthNum) {
                continue;
            }

            TextView dateLbl = new TextView(this);
            dateLbl.setText(RecordsResource.getStartDateString(record));
            dateLbl.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.30f));

            TextView startTimeLbl = new TextView(this);
            startTimeLbl.setText(RecordsResource.getStartTimeString(record));
            startTimeLbl.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.20f));

            TextView endTimeLbl = new TextView(this);
            endTimeLbl.setText(RecordsResource.getEndTimeString(record));
            endTimeLbl.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0.20f));

            Button editBtn = new Button(this);
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editRecordIntent == null) {
                        editRecordIntent = new Intent(EditRecordsActivity.this, ModifyRecordActivity.class);
                    }
                    editRecordIntent.putExtra("startTime", record.getStart());
                    editRecordIntent.putExtra("endTime", record.getEnd());
                    editRecordIntent.putExtra("recordIndex", RecordsResource.getRecords(EditRecordsActivity.this).indexOf(record));
                    startActivityForResult(editRecordIntent, 1);
                }
            });
            editBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mode_edit, 0, 0, 0);
            editBtn.setLayoutParams(new LinearLayout.LayoutParams(88,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            editBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorDarkGray));

            Button deleteBtn = new Button(this);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(EditRecordsActivity.this).create();
                    alertDialog.setTitle(getString(R.string.title_remove_record));
                    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == AlertDialog.BUTTON_POSITIVE) {
                                RecordsResource.removeRecord(EditRecordsActivity.this, record);
                                listRecords(monthNum);
                            }
                            dialog.dismiss();
                        }
                    };
                    alertDialog.setMessage(getString(R.string.alert_remove_record_confirm) + " " + RecordsResource.generateDatetimeRow(record) + "?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.button_yes), dialogListener);
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.button_no), dialogListener);
                    alertDialog.show();
                }
            });
            deleteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_close, 0, 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(85,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            deleteBtn.setLayoutParams(params);
            deleteBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorDarkGray));

            LinearLayout LL = new LinearLayout(this);
            LL.setOrientation(LinearLayout.HORIZONTAL);
            LL.addView(dateLbl);

            LL.addView(startTimeLbl);
            LL.addView(endTimeLbl);

            LL.addView(editBtn);
            LL.addView(deleteBtn);

            LL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            ((LinearLayout) findViewById(R.id.records_layout)).addView(LL);
        }
    }
}
