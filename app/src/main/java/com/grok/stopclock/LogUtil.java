/*
 * LogUtil.java
 *
 * Generic Logging utilities
 *
 * Copyright (C) 2010 Michel Hoche-Mong, hoche@grok.com
 *
 */

package com.grok.stopclock;

import android.util.Log;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
    public final static LogUtil INSTANCE = new LogUtil();

    // release values
    @SuppressWarnings("FieldCanBeLocal")
    private final long MAX_FILE_SIZE = (50 * 1024 * 1024); // 50 MB
    //private final long MAX_FILE_SIZE = (50 * 1024 * 1024); // 5 MB (debug)
    @SuppressWarnings("FieldCanBeLocal")
    private final int MAX_FILE_COUNT = 20;
    //private final int MAX_FILE_COUNT = 5; // (debug)
    @SuppressWarnings("FieldCanBeLocal")
    private final long MAX_FILE_AGE = (1000 * 3600 * 24 * 14);  // 2 weeks
    //private final long MAX_FILE_AGE = (1000 * 3600 * 2);  // 2 hours (debug)

    private String mLogFileDirPath = null;
    private String mBaseFileName = null;
    private File mFile = null;
    private FileOutputStream mFileStream = null;
    private DataOutputStream mNetStream = null;
    private Socket mNetSocket = null;
    private String mHost;
    private int mPort;
    private WeakReference<TextView> mTv = null;

    String mLineEnd = "\r\n";

    private LogUtil() {
        // Exists only to defeat instantiation
    }

    private static final String DATE_STRING="yyyy-MM-dd-HH_mm_ss";

    /**
     * Given a directory path and a filename, concatenate them together, add the current date
     * to the filename, and return the resulting string.
     *
     * For instance given fileDirPath="/sdcard/myappdir" and baseName="log", the
     * return value will be something like "/sdcard/myappdir/log_2010-03-22_18_21".
     *
     * @param  fileDirPath  The directory path to use
     * @param  baseName the file name
     * @return the constructed full filepath
     */
    public static String createDatedFilePath(String fileDirPath, String baseName)
    {
        long dateTaken = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_STRING);
        Date date = new Date(dateTaken);
        return fileDirPath + "/" + dateFormat.format(date) + "_" + baseName;
    }

    public synchronized void SetLogFile(String logFileDirPath, String filename) {
        mLogFileDirPath = logFileDirPath;
        mBaseFileName = filename;

        OpenNewLogFile(); // this will take care of closing one that's already open
    }

    private void CullLogFiles()
    {
        String msg;

        String dirpath = new File(mFile.getPath()).getParent();

        try {
            msg = "Culling files in " + dirpath + "\n";
            if (mFileStream != null) mFileStream.write(msg.getBytes());
            Log.v("LogUtil", msg);

            File directory = new File(dirpath);
            File[] files = directory.listFiles();

            if (files.length > MAX_FILE_COUNT) {
                msg = "Only " + files.length + " files in directory - no culling needed.\n";
                if (mFileStream != null) mFileStream.write(msg.getBytes());
                Log.v("LogUtil", msg);
                return;
            }

            for (File file : files) {

                msg = "FileName:" + file.getName();

                // Get the last modified date. Milliseconds since 1970
                Long lastmodified = file.lastModified();

                // delete files older than MAX_FILE_AGE
                if (lastmodified + MAX_FILE_AGE < System.currentTimeMillis()) {
                    msg += " - deleted\n";
                    file.delete();
                } else {
                    msg += " - OK\n";
                }

                if (mFileStream != null) mFileStream.write(msg.getBytes());
                Log.v("LogUtil", msg);
            }
        } catch (IOException e) {
            // Not much we can do here.
            e.printStackTrace();
        }
    }

    private synchronized void OpenNewLogFile()
    {
        // create a nice file name, complete with datetime stamp
        String filepath = createDatedFilePath(mLogFileDirPath, mBaseFileName);

        // Close the old stream
        if (mFileStream != null) {
            Log.v("LogUtil", "Closing filestream");
            try {
                mFileStream.close();
            } catch (IOException e) {
                // Not much we can do here.
                mFile = null;
                mFileStream = null;
                e.printStackTrace();
            }
            mFileStream = null;
        }

        Log.v("LogUtil", "Creating file" + filepath);
        mFile = new File(filepath);

        // see if it's already there. if not create it.
        if (!mFile.exists()) {
            try {
                mFile.createNewFile();
            } catch (IOException e) {
                mFile = null;
                e.printStackTrace();
                return;
            }
        }

        try {
            Log.v("LogUtil", "Opening filestream to " + filepath);
            mFileStream = new FileOutputStream(filepath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            mFile = null;
            e.printStackTrace();
        }

        // We do this after we've set up the new log file so we can make
        // sure we have something to log to when we do the culling.
        CullLogFiles();
    }

    public synchronized void SetLogServer(String host, int port) {

        // Android won't let us do networking on the main thread for performance reasons.
        // However, if we've called this, we're trying to log remotely, and we
        // want to do that on the main thread. (Our alternative is to buffer it
        // up and send later and hope we don't crash in the meantime, or send it
        // to a log service to forward). So turn off the strict mode.
        //
        // Disabled for the Nook - Android doesn't care since it's so old
        //
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        mHost = host;
        mPort = port;
    }

    public synchronized void SetTextView(TextView tv) {
        // Use a WeakReference to ensure the TextView can be garbage collected
        //mTv = new WeakReference<>(tv);
        mTv = new WeakReference(tv);
    }

    private void ConnectToLogServer() {
        if (mHost != null) {
            try {
                if (mNetSocket == null) {
                    mNetSocket = new Socket();
                    mNetSocket.connect(new InetSocketAddress(mHost, mPort), 500); // 100 millisecond timeout
                }

                if (mNetStream == null) {
                    mNetStream = new DataOutputStream(mNetSocket.getOutputStream());
                }

            } catch (IOException e) {
                e.printStackTrace();
                mNetSocket = null;
                mNetStream = null;
            }
        }
    }

    protected void finalize() {
        try {
            if (mFileStream != null) {
                mFileStream.close();
            }
            if (mNetStream != null) {
                mNetStream.flush();
                mNetStream.close();
            }
            if (mNetSocket != null) {
                mNetSocket.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void fileWrite(String msg) {
        if (mFile == null) {
            return;
        }
        try {
            // see if it's bigger than our max size. If so, create a new one.
            long fileSize = mFile.length();
            if (fileSize > MAX_FILE_SIZE) {
                OpenNewLogFile();
            }

            if (mFileStream != null) {
                mFileStream.write(msg.getBytes());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void textViewWrite(final String msg) {
        if (mTv != null) {
            mTv.get().post(new Runnable() {
                @Override
                public void run() {
                    mTv.get().append(msg);
                    mTv.get().invalidate();
                }
            });
        }
    }

    private void netWrite(String msg) {
        try {
            ConnectToLogServer();
            if (mNetStream != null) {
                mNetStream.write(msg.getBytes());
                mNetStream.flush();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void v(String tag, String msg) {
        msg = System.currentTimeMillis() + " TID:" + Thread.currentThread().getId() + " " + tag + " " + msg + "\n";
        fileWrite(msg);
        textViewWrite(msg);
        netWrite(msg);
        Log.v(tag, msg);
    }

    public synchronized void d(String tag, String msg) {
        msg = System.currentTimeMillis() + " TID:" + Thread.currentThread().getId() + " " + tag + " " + msg + "\n";
        fileWrite(msg);
        textViewWrite(msg);
        netWrite(msg);
        Log.d(tag, msg);
    }

    public synchronized void i(String tag, String msg) {
        msg = System.currentTimeMillis() + " TID:" + Thread.currentThread().getId() + " " + tag + " " + msg + "\n";
        fileWrite(msg);
        textViewWrite(msg);
        netWrite(msg);
        Log.i(tag, msg);
    }

    public synchronized void w(String tag, String msg) {
        msg = System.currentTimeMillis() + " TID:" + Thread.currentThread().getId() + " " + tag + " " + msg + "\n";
        fileWrite(msg);
        textViewWrite(msg);
        netWrite(msg);
        Log.w(tag, msg);
    }

    public synchronized void e(String tag, String msg, Exception e) {
        msg = System.currentTimeMillis() + " TID:" + Thread.currentThread().getId() + " " + tag + " " + msg + e.getMessage() + "\n";
        fileWrite(msg);
        textViewWrite(msg);
        netWrite(msg);
        Log.e(tag, msg);
    }
}
