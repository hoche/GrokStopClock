package com.grok.groknookclock;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PathUtils {
    private static final String DATE_STRING="yyyy-MM-dd-HH_mm_ss";

    public static String createDatedFilePath(String fileDirPath, String baseName)
    {
        long dateTaken = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_STRING);
        Date date = new Date(dateTaken);
        return fileDirPath + "/" + dateFormat.format(date) + "_" + baseName;
    }

    public static String createDatedFilePath(String fileDirPath, String fileType, String suffix, Context ctx) {
        long dateTaken = System.currentTimeMillis();SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_STRING);
        Date date = new Date(dateTaken);
        return fileDirPath + "/" + dateFormat.format(date) + "_" + fileType + suffix;
    }

    public static File getStorageDir(Context ctx) {

        String filesDirPath = Environment.getExternalStorageDirectory().toString() +
                "/" + ctx.getResources().getString(R.string.app_name);

        File ret = ctx.getFilesDir(); // never null (supposedly)
        if (!ret.exists()) {
          ret.mkdirs();
        }
        return ret;
    }

}
