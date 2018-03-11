package com.journeyOS.liteprovider.utils;

import android.os.Build;
import android.util.Log;

public class LogUtils {
    public static final String TAG = "GP";
    //add user type for more debug log with beta user...
    public final static boolean BUILD_TYPE = "eng".equals(Build.TYPE) || "userdebug".equals(Build.TYPE);
    public static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    public static void v(String message, Object... args) {
        if (DEBUG) {
            Log.v(TAG, args == null || args.length == 0
                    ? message : String.format(message, args));
        }
    }

    public static void v(String tag, String message, Object... args) {
        if (DEBUG) {
            Log.v(replaceTag(tag), args == null || args.length == 0 ? message
                    : String.format(message, args));
        }
    }

    public static void d(String message, Object... args) {
        if (DEBUG) {
            Log.d(TAG, args == null || args.length == 0 ? message
                    : String.format(message, args));
        }
    }

    public static void d(String tag, String message, Object... args) {
        if (DEBUG) {
            Log.d(replaceTag(tag), args == null || args.length == 0 ? message
                    : String.format(message, args));
        }
    }

    public static void d(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

    public static void i(String message, Object... args) {
        if (DEBUG) {
            Log.i(TAG, args == null || args.length == 0 ? message
                    : String.format(message, args));
        }
    }

    public static void i(String tag, String message, Object... args) {
        if (DEBUG) {
            Log.i(replaceTag(tag), args == null || args.length == 0 ? message
                    : String.format(message, args));
        }
    }

    public static void w(String message, Object... args) {
        if (DEBUG) {
            Log.w(TAG, args == null || args.length == 0 ? message
                    : String.format(message, args));
        }
    }

    public static void w(String tag, String message, Object... args) {
        if (DEBUG) {
            Log.w(replaceTag(tag), args == null || args.length == 0 ? message
                    : String.format(message, args));
        }
    }

    public static void e(String message, Object... args) {
        if (DEBUG) {
            Log.e(TAG, args == null || args.length == 0 ? message
                    : String.format(message, args));
        }
    }

    public static void e(String tag, String message, Object... args) {
        if (DEBUG) {
            Log.e(replaceTag(tag), args == null || args.length == 0 ? message
                    : String.format(message, args));
        }
    }

    public static void e(String message, Exception e) {
        if (DEBUG) {
            Log.e(TAG, message, e);
        }
    }

    public static void e(String tag, String message, Exception e) {
        if (DEBUG) {
            Log.e(replaceTag(tag), message, e);
        }
    }

    public static void wtf(String message, Object... args) {
        if (DEBUG) {
            Log.wtf(TAG, args == null || args.length == 0 ? message
                    : String.format(message, args));
        }
    }

    public static void wtf(String tag, String message, Object... args) {
        if (DEBUG) {
            Log.wtf(replaceTag(tag), args == null || args.length == 0 ? message
                    : String.format(message, args));
        }
    }

    public static void trace(String tag, String message) {
        if (DEBUG) {
            Log.i(replaceTag(tag), message + "----------" + "\n" + Log.getStackTraceString(new Throwable()));
        }
    }

    private static String replaceTag(String src) {
        if (src == null) return null;
        String dest = TAG + "-" + src;
        return dest;
    }
}

