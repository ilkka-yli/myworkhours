package com.example.lgi.myworkhours;

/**
 * Created by lgi on 25.10.2016.
 */

public class Record {
    private long start;
    private long end;

    public Record() {
    }

    public Record(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}
