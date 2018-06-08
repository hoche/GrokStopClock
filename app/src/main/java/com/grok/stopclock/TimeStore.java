/*
 * TimeStore.java
 *
 * Save a bunch of captured times as a JSON file.
 *
 * Copyright (C) 2011 Michel Hoche-Mong, hoche@grok.com
 *
 */
package com.grok.stopclock;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/* Note: This uses a text file as a backing store. The main reason for that is that it can
 * just append to it without having to reconstruct the file each time a new time is stored.
 * As an option, it could use either JSON store or a SQLite database.
 */
public class TimeStore {

    private final String LOGTAG = "TimeStore";

    private Context mCtxt;
    private FileOutputStream mFOut;
    private ArrayList<TimeEntry> mTimes;

    public TimeStore(Context ctx) {
        mCtxt = ctx;
        mTimes = new ArrayList<TimeEntry>();
    }

    private String getExternalDataFileDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + mCtxt.getResources().getString(R.string.app_dir_name);
    }

    private String getDataFileDir() {
        return mCtxt.getFilesDir().getAbsolutePath() +
                "/" + mCtxt.getResources().getString(R.string.app_dir_name);
    }

    public boolean init() {

        String path = getExternalDataFileDir();
        Boolean isNew = false;

        // Return the primary external storage directory. This directory may not currently be
        // accessible if it has been mounted by the user on their computer, has been removed from
        // the device, or some other problem has happened. You can determine its current state
        // with getExternalStorageState(). Check its StorageState first.
        // need
        // <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
        // <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
        // set in the manifest.
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // TODO: Put up an error message dialog about the SD card not being available and complain
            path = getDataFileDir();
        }

        LogUtil.INSTANCE.d(LOGTAG, "Using datafile in " + path);

        final File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                // TODO: Handle error with proper notification
                LogUtil.INSTANCE.d(LOGTAG, "Couldn't create storage directory.");
                return false;
            }
        } else if (!dir.isDirectory() || !dir.canWrite()) {
            // TODO: Handle error with proper notification
            LogUtil.INSTANCE.d(LOGTAG, "Couldn't write to storage directory.");
            return false;
        }

        final File dataFile = new File(path, "data.txt");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                isNew = true;
            } catch (IOException e) {
                // TODO: Handle error with proper notification
                LogUtil.INSTANCE.d(LOGTAG, "Couldn't create data file.");
                return false;
            }
        }
        if (!dataFile.canWrite() || !dataFile.canRead()) {
            // TODO: Handle error with proper notification
            LogUtil.INSTANCE.d(LOGTAG, "Couldn't read or write to data file.");
            return false;
        }

        // Ok, should have a readable/writeable file.

        // Read it and store the times into memory.
        try {
            FileInputStream is = new FileInputStream(dataFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            int lineNo = 1;
            while ((line = reader.readLine())!= null) {
                try {
                    TimeEntry te = new TimeEntry(line);
                    mTimes.add(te);
                } catch (IllegalArgumentException e) {
                    LogUtil.INSTANCE.d(LOGTAG, "Invalid entry at line " + lineNo);
                }
                ++lineNo;
            }
            is.close();
        } catch (IOException e) {
            LogUtil.INSTANCE.d(LOGTAG, "Couldn't read data file.");
            return false;
        }

        try {
            FileOutputStream os = new FileOutputStream(dataFile, true); // open for appending
            mFOut = os;
            // write header if necessary
            if (isNew) {
                try {
                    mFOut.write(TimeEntry.headersToString().getBytes());
                } catch (IOException e) {
                    LogUtil.INSTANCE.d(LOGTAG, "Couldn't append to data file.");
                }
            }
        } catch (IOException e) {
            LogUtil.INSTANCE.d(LOGTAG, "Couldn't append to data file.");
            return false;
        }

        return true;
    }

    public void addToList(TimeEntry te) {
        mTimes.add(te);
        if (mFOut != null) {
            try {
                mFOut.write(te.toString().getBytes());
            } catch (IOException e) {
                LogUtil.INSTANCE.d(LOGTAG, "Couldn't append to data file.");
            }
        }
    }

    public int getEntryCount() {
        return mTimes.size();
    }
    public TimeEntry get(int position) { return mTimes.get(position); }
    public ArrayList<TimeEntry> getList() {
        return mTimes;
    }
}
