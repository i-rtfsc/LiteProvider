package com.journeyOS.demo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.ArrayMap;
import android.util.Log;

import java.util.Map;

public class DBOperate {
    private static final String TAG = DBOperate.class.getSimpleName();

    private static final DBOperate sDefault = new DBOperate();

    private static final Object mLock = new Object();

    public static DBOperate getDefault() {
        return sDefault;
    }

    private Boolean isExists(Context context, Uri uri, String key) {
        String localKey = null;
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(uri, null, DBConfig.KEY + "=?", new String[]{key}, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    localKey = cursor.getString(cursor.getColumnIndex(DBConfig.KEY));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return key.equals(localKey);
    }

    public void saveOrUpdate(Context context, String key, String value) {
        saveOrUpdate(context, DBConfig.URL, key, value);
    }

    public void saveOrUpdate(Context context, Uri uri, String key, String value) {
        synchronized (mLock) {
            Log.d(TAG, "save or update key = [" + key + "],  value = [" + value + "]");
            if (key == null || value == null) {
                return;
            }
            ContentValues values = new ContentValues();
            values.put(DBConfig.KEY, key);
            values.put(DBConfig.VALUE, value);
            if (isExists(context, uri, key)) {
                context.getContentResolver().update(uri, values, DBConfig.KEY + "=?", new String[]{key});
            } else {
                context.getContentResolver().insert(uri, values);
            }
        }
    }

    public Map<String, String> query(Context context, String key) {
        return query(context, DBConfig.URL, key);
    }

    public Map<String, String> query(Context context, Uri uri, String key) {
        synchronized (mLock) {
            if (key == null) {
                return null;
            }

            Map<String, String> result = new ArrayMap<>();
            String vaule = null;
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, DBConfig.KEY + " = ?", new String[]{key}, null);
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    do {
                        vaule = cursor.getString(cursor.getColumnIndex(DBConfig.VALUE));
                    } while (cursor.moveToNext());
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            result.put(key, vaule);

            return result;
        }
    }
}
