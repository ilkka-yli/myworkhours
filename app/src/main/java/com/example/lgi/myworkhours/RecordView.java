package com.example.lgi.myworkhours;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by lgi on 18.3.2017.
 */

public class RecordView extends RelativeLayout {

    private TextView date;
    private TextView startTime;
    private TextView endTime;

    public RecordView(Context context) {
        super(context);
        init();
    }

    public RecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RecordView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.record, this);
        this.date = (TextView)findViewById(R.id.label_date);
        this.startTime = (TextView)findViewById(R.id.label_start);
        this.endTime = (TextView)findViewById(R.id.label_end);
    }
}
