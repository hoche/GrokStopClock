/*
 * TimeStore.java
 *
 * Save a bunch of captured times as a JSON file.
 *
 * Copyright (C) 2011 Michel Hoche-Mong, hoche@grok.com
 *
 */
package com.grok.grokclock;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.util.ArrayList;

/* Note: This uses a text file as a backing store. The main reason for that is that it can
 * just append to it without having to reconstruct the file each time a new time is stored.
 * As an option, it could use either JSON store or a SQLite database.
 */
public class TimeStore {

    private static final String LOGTAG = "TimeStore";

    private Context mCtxt;
    private File mDataFile;
    private FileOutputStream mFOut;
    private ArrayList<TimeEntry> mTimes;

    public TimeStore(Context ctx) {
        mCtxt = ctx;
    }

    private String getDataFileDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + mCtxt.getResources().getString(R.string.app_name);
    }

    private boolean loadFile() {
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
            return false;
        }

        String path = getDataFileDir();

        final File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                // TODO: Handle error
                LogUtil.INSTANCE.d(LOGTAG, "Couldn't create storage directory.");
                return false;
            }
        } else if (!dir.isDirectory() || !dir.canWrite()) {
            // TODO: Handle error
            LogUtil.INSTANCE.d(LOGTAG, "Couldn't write to storage directory.");
            return false;
        }

        final File dataFile = new File(path, "data.txt");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                // TODO: Handle error
                LogUtil.INSTANCE.d(LOGTAG, "Couldn't create data file.");
                return false;
            }
        }
        if (!dataFile.canWrite() || !dataFile.canRead()) {
            // TODO: Handle error
            LogUtil.INSTANCE.d(LOGTAG, "Couldn't read or write to data file.");
            return false;
        }

        // Ok, should have a readable/writeable file.

        // Read it and store the times into memory.
        try {
            FileInputStream is = new FileInputStream(dataFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            int lineNo = 1;
            while (line != null) {
                line = reader.readLine();
                try {
                    TimeEntry te = new TimeEntry(line);
                    mTimes.add(te);
                } catch (IllegalArgumentException e) {
                    LogUtil.INSTANCE.d(LOGTAG, "Invalid entry at line " + lineNo);
                }
                ++lineNo;
            }
        } catch (IOException e) {
            LogUtil.INSTANCE.d(LOGTAG, "Couldn't read data file.");
            return false;
        }

        return true;
    }
}
