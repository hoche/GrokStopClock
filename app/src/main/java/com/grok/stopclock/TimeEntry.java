/*
 * TimeEntry.java
 *
 * Storage
 *
 * Copyright (C) 2011 Michel Hoche-Mong, hoche@grok.com
 *
 */

package com.grok.stopclock;

import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeEntry {
    private String mId;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMin;
    private int mSec;
    private int mTenth;

    // These should match the ordering of values in strings/time_format_items
    public enum TimeFormat {
        SECONDS_ONLY,
        SECONDS_AND_TENTHS,
        HUNDREDTHS_OF_MINUTE
    }

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

    public final String getDate() {
        DecimalFormat yf = new DecimalFormat("0000");
        DecimalFormat df = new DecimalFormat("00");
        return yf.format(mYear) + "-" +
                df.format(mMonth) + "-" +
                df.format(mDay);
    }

    public final String getTime(TimeFormat timeFormat) {
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);

        switch (timeFormat) {
            case SECONDS_AND_TENTHS:
                fmt.format("%d:%02d:%02d.%d", mHour, mMin, mSec, mTenth);
                break;

            case HUNDREDTHS_OF_MINUTE:
                fmt.format("%d:%02d.%02d", mHour, mMin, ((mSec * 1000) + mTenth)/600);
                break;

            case SECONDS_ONLY:
            default:
                fmt.format("%d:%02d:%02d", mHour, mMin, mSec + ((mTenth >= 5) ? 1 : 0) );
        }
        return sbuf.toString();
    }

    public static final String headersToString() {
        String val = "ID,Time,Date\n";
        return val;
    }

    public final String toString() {
        String val = mId + "," + getTime(TimeFormat.SECONDS_AND_TENTHS) + "," + getDate() + "\n";
        return val;
    }

    public static TimeFormat getTimeFormat(int idx) {
        switch (idx) {
            case 1:
                return TimeFormat.SECONDS_AND_TENTHS;
            case 2:
                return TimeFormat.HUNDREDTHS_OF_MINUTE;
            case 0:
            default:
                return TimeFormat.SECONDS_ONLY;
        }
    }
}
