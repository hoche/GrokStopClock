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
        switch (timeFormat) {

            case SECONDS_AND_TENTHS: {
                DecimalFormat hf = new DecimalFormat("#0");
                DecimalFormat df = new DecimalFormat("00");
                DecimalFormat sf = new DecimalFormat("0");
                return hf.format(mHour) + ":" +
                        df.format(mMin) + ":" +
                        df.format(mSec) + "." +
                        sf.format(mTenth);
            }

            case HUNDREDTHS_OF_MINUTE: {
                DecimalFormat hf = new DecimalFormat("#0");
                DecimalFormat df = new DecimalFormat("00");
                return hf.format(mHour) + ":" +
                        df.format(mMin) + ":" +
                        df.format(((mSec * 1000) + mTenth)/600);
            }

            case SECONDS_ONLY:
            default: {
                DecimalFormat hf = new DecimalFormat("#0");
                DecimalFormat df = new DecimalFormat("00");
                // TODO: fix rounding of seconds
                return hf.format(mHour) + ":" +
                        df.format(mMin) + ":" +
                        df.format(mSec);
            }
        }
    }

    public final byte[] toByteArray() {
        String val = mId + "," + getTime(TimeFormat.SECONDS_AND_TENTHS) + "," + getDate() + "\n";
        return val.getBytes();
    }
}
