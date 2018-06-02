/*
 * TimeEntry.java
 *
 * Storage
 *
 * Copyright (C) 2011 Michel Hoche-Mong, hoche@grok.com
 *
 */

package com.grok.grokclock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeEntry {
    private int mId;
    private String mTime;
    private String mDate;

    // TODO: Improve these regexps
    private final static String idPattern = "(\\d+)";
    private final static String timePattern = "(\\d\\d:\\d\\d:\\d\\d.\\d)";
    private final static String datePattern = "(\\d\\d\\d\\d-\\d\\d-\\d\\d)";

    private final static Pattern linePat = Pattern.compile(idPattern + "," + timePattern + "," + datePattern);

    public TimeEntry(int id, String time, String date) {
        mId = id;

        // TODO: check against patterns
        mTime = time;
        mDate = date;
    }

    public TimeEntry(String line) {
        Matcher m = linePat.matcher(line);
        if (!m.matches()) {  // whole thing has to match
            throw new IllegalArgumentException("Line not in correct format");
        }
        mId = Integer.getInteger(m.group(1));
        mTime = m.group(2);
        mDate = m.group(3);

    }

    public final int getId() { return mId; }
    public final String getTime() { return mTime; }
    public final String getDate() { return mDate; }

    public final String toString() {
        return Integer.toString(mId) + "," + mTime + "," + mDate;
    }
}
