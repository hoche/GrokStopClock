/*
 * TimeEntry.java
 *
 * Storage
 *
 * Copyright (C) 2011 Michel Hoche-Mong, hoche@grok.com
 *
 */

package com.grok.stopclock;

import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeEntry {
    public String mId;
    public int mYear;
    public int mMonth;
    public int mDay;
    public int mHour;
    public int mMin;
    public int mSec;
    public int mTenth;

    // TODO: Improve these regexps
    private final static String idPattern = "(\\w+)";
    private final static String timePattern = "(\\d+):(\\d\\d):(\\d\\d).(\\d)";
    private final static String datePattern = "(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)";

    private final static Pattern linePat = Pattern.compile(idPattern + "," + timePattern + "," + datePattern);

    public TimeEntry(String id, int year, int month, int day, int hour, int min, int sec, int tenth) {
        mId = id;
        mYear = year;
        mMonth = month;
        mDay = day;
        mHour = hour;
        mMin = min;
        mSec = sec;
        mTenth = tenth;

    }

    public TimeEntry(String line) {
        Matcher m = linePat.matcher(line);
        if (!m.matches()) {  // whole thing has to match
            throw new IllegalArgumentException("Line not in correct format");
        }
        mId = m.group(1);

        mHour = Integer.parseInt(m.group(2));
        mMin = Integer.parseInt(m.group(3));
        mSec = Integer.parseInt(m.group(4));
        mTenth = Integer.parseInt(m.group(5));

        mYear = Integer.parseInt(m.group(6));
        mMonth = Integer.parseInt(m.group(7));
        mDay = Integer.parseInt(m.group(8));

    }

    public final String getId() {
        return mId;
    }

    public static final String headersToString() {
        String val = "ID,Time,Date\n";
        return val;
    }

    public final String toString() {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format("%s,%d:%02d:%02d.%d,%04d-%02d-%02d\n",
                mId, mHour, mMin, mSec, mTenth, mYear, mMonth, mDay);
        return sbuf.toString();
    }

}
