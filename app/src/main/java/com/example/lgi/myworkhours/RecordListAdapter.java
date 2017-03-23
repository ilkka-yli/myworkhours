package com.example.lgi.myworkhours;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by lgi on 19.3.2017.
 */

public class RecordListAdapter extends BaseAdapter {

    private AppCompatActivity context;
    private List<Record> data;
    private Intent editRecordIntent;
    private static LayoutInflater inflater = null;

    public RecordListAdapter(AppCompatActivity context) {
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<Record> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = inflater.inflate(R.layout.record, viewGroup, false);

        final Record record = data.get(i);
        TextView text = (TextView) view.findViewById(R.id.label_date);
        text.setText(RecordsResource.getStartDateString(record));
        text = (TextView) view.findViewById(R.id.label_start);
        text.setText(RecordsResource.getStartTimeString(record));
        text = (TextView) view.findViewById(R.id.label_end);
        text.setText(RecordsResource.getEndTimeString(record));

        final ViewGroup parent = viewGroup;
        final int position = i;

        Button editBtn = (Button) view.findViewById(R.id.button_edit);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editRecordIntent == null) {
                    editRecordIntent = new Intent(context, ModifyRecordActivity.class);
                }
                editRecordIntent.putExtra("startTime", record.getStart());
                editRecordIntent.putExtra("endTime", record.getEnd());
                editRecordIntent.putExtra("recordIndex", RecordsResource.getRecords(context).indexOf(record));
                context.startActivityForResult(editRecordIntent, 1);
            }
        });

        Button deleteBtn = (Button) view.findViewById(R.id.button_delete);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle(context.getString(R.string.title_remove_record));
                DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == AlertDialog.BUTTON_POSITIVE) {
                            RecordsResource.removeRecord(context, record);
                            data.remove(position);
                            notifyDataSetChanged();
                        }
                        dialog.dismiss();
                    }
                };
                alertDialog.setMessage(context.getString(R.string.alert_remove_record_confirm) + " " + RecordsResource.generateDatetimeRow(record) + "?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.button_yes), dialogListener);
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.button_no), dialogListener);
                alertDialog.show();
            }
        });
        return view;
    }
}
