/*
 * Copyright (c) 2018 anqi.huang@outlook.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.journeyOS.liteprovider.globals;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * This class holds a key-value pair.
 */
/* package */ class Global {

    /**
     * Indicates this global was not created from a record of the database.
     */
    public static final long NO_ID = -1;

    /**
     * The integer representation of <code>true</code> to store boolean type values
     * into a database.
     */
    private static final int TRUE = 1;

    /**
     * The integer representation of <code>false</code> to store boolean type values
     * into a database.
     */
    private static final int FALSE = 0;

    /**
     * The unique ID for this global.
     */
    private long mId = NO_ID;

    /**
     * The key of this global.
     */
    private String mKey;

    /**
     * The value type of this global.
     */
    private String mType;

    /**
     * The value of this global.
     */
    private Object mValue;

    /**
     * Returns the {@link Global} created from the current row of the {@link Cursor}.
     * Note that null fields are not read out.
     *
     * @param cursor The {@link Cursor} indicating the row to convert a {@link Global}.
     * @return the {@link Global} created from the current row of the {@link Cursor}.
     */
    public static Global cursorRowToGlobal(Cursor cursor) {
        ContentValues values = new ContentValues();
        String[] columns = cursor.getColumnNames();
        int length = columns.length;
        for (int i = 0; i < length; i++) {
            if (!cursor.isNull(i)) {
                int type = cursor.getType(i);
                switch (type) {
                    case Cursor.FIELD_TYPE_FLOAT:
                        values.put(columns[i], cursor.getFloat(i));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        values.put(columns[i], cursor.getInt(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        values.put(columns[i], cursor.getString(i));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        values.put(columns[i], cursor.getBlob(i));
                        break;
                    case Cursor.FIELD_TYPE_NULL:
                    default:
                        break;
                }
            }
        }

        return new Global(values);
    }

    /**
     * Creates a new global from the {@link ContentValues}.
     *
     * @param values The {@link ContentValues}.
     */
    public Global(ContentValues values) {
        mId = values.getAsLong(GlobalsContract._ID);
        mKey = values.getAsString(GlobalsContract.KEY);
        mType = values.getAsString(GlobalsContract.TYPE);
        if (mType.equals(Boolean.class.getName())) {
            mValue = getValueAsBoolean(values);
        } else if (mType.equals(Float.class.getName())) {
            mValue = getValueAsFloat(values);
        } else if (mType.equals(Integer.class.getName())) {
            mValue = getValueAsInteger(values);
        } else if (mType.equals(Long.class.getName())) {
            mValue = getValueAsLong(values);
        } else if (mType.equals(String.class.getName())) {
            mValue = getValueAsString(values);
        } else {
            mValue = getValueAsObject(values);
        }
    }

    private Object getValueAsBoolean(ContentValues values) {
        Integer value = values.getAsInteger(GlobalsContract.VALUE);
        if (value.equals(TRUE)) {
            return Boolean.TRUE;
        } else if (value.equals(FALSE)) {
            return Boolean.FALSE;
        } else {
            throw new IllegalStateException("invalid value");
        }
    }

    private Object getValueAsFloat(ContentValues values) {
        Float value = values.getAsFloat(GlobalsContract.VALUE);
        return value;
    }

    private Object getValueAsInteger(ContentValues values) {
        Integer value = values.getAsInteger(GlobalsContract.VALUE);
        return value;
    }

    private Object getValueAsLong(ContentValues values) {
        Long value = values.getAsLong(GlobalsContract.VALUE);
        return value;
    }

    private Object getValueAsString(ContentValues values) {
        String value = values.getAsString(GlobalsContract.VALUE);
        return value;
    }

    private Object getValueAsObject(ContentValues values) {
        byte[] bytes = values.getAsByteArray(GlobalsContract.VALUE);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        ObjectInput input = null;
        Object object = null;
        try {
            input = new ObjectInputStream(stream);
            object = input.readObject();
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        } finally {
            try {
                stream.close();
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
            }
        }
        return object;
    }

    /**
     * Creates a new global for the key-value pair.
     *
     * @param key   The key of this global.
     * @param value The value of this global.
     */
    public Global(String key, Object value) {
        mKey = key;
        Class<?> clazz = value.getClass();
        mType = clazz.getName();
        mValue = value;
    }

    /* package */ long getId() {
        return mId;
    }

    public String getKey() {
        return mKey;
    }

    public Object getValue() {
        return mValue;
    }

    /**
     * Returns a {@link ContentValues} for this global.
     *
     * @return a {@link ContentValues} for this global.
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(GlobalsContract.KEY, mKey);
        values.put(GlobalsContract.TYPE, mType);
        if (mType.equals(Boolean.class.getName())) {
            putBoolean(values, (Boolean) mValue);
        } else if (mType.equals(Float.class.getName())) {
            putFloat(values, (Float) mValue);
        } else if (mType.equals(Integer.class.getName())) {
            putInteger(values, (Integer) mValue);
        } else if (mType.equals(Long.class.getName())) {
            putLong(values, (Long) mValue);
        } else if (mType.equals(String.class.getName())) {
            putString(values, (String) mValue);
        } else {
            putObject(values, mValue);
        }
        return values;
    }

    private void putBoolean(ContentValues values, Boolean value) {
        if (value) {
            values.put(GlobalsContract.VALUE, TRUE);
        } else {
            values.put(GlobalsContract.VALUE, FALSE);
        }
    }

    private void putFloat(ContentValues values, Float value) {
        values.put(GlobalsContract.VALUE, value);
    }

    private void putInteger(ContentValues values, Integer value) {
        values.put(GlobalsContract.VALUE, value);
    }

    private void putLong(ContentValues values, Long value) {
        values.put(GlobalsContract.VALUE, value);
    }

    private void putString(ContentValues values, String value) {
        values.put(GlobalsContract.VALUE, value);
    }

    private void putObject(ContentValues values, Object value) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutput output = null;
        try {
            output = new ObjectOutputStream(stream);
            output.writeObject(value);
            byte[] bytes = stream.toByteArray();
            values.put(GlobalsContract.VALUE, bytes);
        } catch (IOException e) {
        } finally {
            try {
                stream.close();
                if (output != null) {
                    output.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    @Override
    public int hashCode() {
        if (TextUtils.isEmpty(mKey)) {
            return 0;
        } else {
            return mKey.hashCode();
        }
    }

    /**
     * Compares the given key to the key of this global.
     *
     * @param key The key to compare the key of this global with.
     * @return true if the given key is equal to the key of this global.
     */
    public boolean keyEquals(String key) {
        if (TextUtils.isEmpty(mKey)) {
            return false;
        } else {
            return mKey.equals(key);
        }
    }

    @Override
    public String toString() {
        String str = new StringBuilder()
                .append("[")
                .append("KEY=").append(mKey)
                .append(", TYPE=").append(mType)
                .append(", VALUE=").append(mValue)
                .toString();
        return str;
    }
}
