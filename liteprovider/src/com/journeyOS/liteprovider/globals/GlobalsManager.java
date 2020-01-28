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

import android.annotation.TargetApi;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import com.journeyOS.liteprovider.utils.LogUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalsManager {
    private static final String TAG = GlobalsManager.class.getSimpleName();

    private static Application mContext = null;

    private static List<OnGlobalsProviderChangeListener> mListeners =
            new CopyOnWriteArrayList<OnGlobalsProviderChangeListener>();

    private static SharedPreferences mPreferences = null;

    public static void initialize(Application context) {
        mContext = context;
        mPreferences = Globals.getInstance(mContext);
    }

    private static boolean isInitialize() {
        if (mContext == null || mPreferences == null) {
            //throw new IllegalArgumentException("you has not initialize!");
            LogUtils.w(TAG, "you has not initialize!");
            return false;
        }

        return true;
    }

    public static SharedPreferences getSharedPreferences() {
        if (isInitialize()) {
            return mPreferences;
        } else {
            return null;
        }
    }

    /**
     * Set a boolean value in the globals provider
     *
     * @param key   The name of the globals provider to modify.
     * @param value The new value for the globals provider.
     */
    public static void put(String key, boolean value) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return;
            }
            put(key, value, false);
        }
    }

    /**
     * Set a boolean value in the globals provider
     *
     * @param key      The name of the globals provider to modify.
     * @param value    The new value for the globals provider.
     * @param isCommit If you don't care about the return value and you're
     *                 using this from your application's main thread, consider
     *                 using apply instead.
     */
    public static void put(String key, boolean value, boolean isCommit) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return;
            }
            if (isCommit) {
                mPreferences.edit().putBoolean(key, value).commit();
            } else {
                mPreferences.edit().putBoolean(key, value).apply();
            }
        }
    }

    /**
     * Retrieve a boolean value from the globals provider.
     *
     * @param key The name of the globals provider to retrieve.
     * @return Returns the globals provider value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     */
    public static boolean getBoolean(String key) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return true;
            }
            return getBoolean(key, true);
        } else {
            return true;
        }
    }

    /**
     * Retrieve a boolean value from the globals provider.
     *
     * @param key          The name of the globals provider to retrieve.
     * @param defaultValue Value to return if this globals provider does not exist.
     * @return Returns the globals provider value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return defaultValue;
            }
            return mPreferences.getBoolean(key, defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Set a int value in the globals provider
     *
     * @param key   The name of the globals provider to modify.
     * @param value The new value for the globals provider.
     */
    public static void put(final String key, int value) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return;
            }
            put(key, value, false);
        }
    }

    /**
     * Set a int value in the globals provider
     *
     * @param key      The name of the globals provider to modify.
     * @param value    The new value for the globals provider.
     * @param isCommit If you don't care about the return value and you're
     *                 using this from your application's main thread, consider
     *                 using apply instead.
     */
    public static void put(final String key, int value, boolean isCommit) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return;
            }
            if (isCommit) {
                mPreferences.edit().putInt(key, value).commit();
            } else {
                mPreferences.edit().putInt(key, value).apply();
            }
        }
    }

    /**
     * Retrieve a int value from the globals provider.
     *
     * @param key The name of the globals provider to retrieve.
     * @return Returns the globals provider value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     */
    public static int getInt(String key) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return -1;
            }
            return getInt(key, -1);
        } else {
            return -1;
        }
    }

    /**
     * Retrieve a int value from the globals provider.
     *
     * @param key          The name of the globals provider to retrieve.
     * @param defaultValue Value to return if this globals provider does not exist.
     * @return Returns the globals provider value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     */
    public static int getInt(String key, int defaultValue) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return defaultValue;
            }
            return mPreferences.getInt(key, defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Set a String value in the globals provider
     *
     * @param key   The name of the globals provider to modify.
     * @param value The new value for the globals provider.
     */
    public static void put(final String key, String value) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return;
            }
            put(key, value, false);
        }
    }

    /**
     * Set a String value in the globals provider
     *
     * @param key      The name of the globals provider to modify.
     * @param value    The new value for the globals provider.
     * @param isCommit If you don't care about the return value and you're
     *                 using this from your application's main thread, consider
     *                 using apply instead.
     */
    public static void put(final String key, String value, boolean isCommit) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return;
            }
            if (isCommit) {
                mPreferences.edit().putString(key, value).commit();
            } else {
                mPreferences.edit().putString(key, value).apply();
            }
        }
    }

    /**
     * Retrieve a String value from the globals provider.
     *
     * @param key The name of the globals provider to retrieve.
     * @return Returns the globals provider value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     */
    public static String getString(String key) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return null;
            }
            return getString(key, null);
        } else {
            return null;
        }
    }

    /**
     * Retrieve a String value from the globals provider.
     *
     * @param key          The name of the globals provider to retrieve.
     * @param defaultValue Value to return if this globals provider does not exist.
     * @return Returns the globals provider value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     */
    public static String getString(String key, String defaultValue) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return defaultValue;
            }
            return mPreferences.getString(key, defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Set a set of String value in the globals provider
     *
     * @param key   The name of the globals provider to modify.
     * @param value The new value for the globals provider.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void put(String key, Set<String> value) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return;
            }
            put(key, value, false);
        }
    }

    /**
     * Set a set of String value in the globals provider
     *
     * @param key      The name of the globals provider to modify.
     * @param value    The new value for the globals provider.
     * @param isCommit If you don't care about the return value and you're
     *                 using this from your application's main thread, consider
     *                 using apply instead.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void put(String key, Set<String> value, boolean isCommit) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return;
            }
            if (isCommit) {
                mPreferences.edit().putStringSet(key, value).commit();
            } else {
                mPreferences.edit().putStringSet(key, value).apply();
            }
        }
    }

    /**
     * Retrieve a set of String value from the globals provider.
     *
     * @param key The name of the globals provider to retrieve.
     * @return Returns the globals provider value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Set<String> getStringSet(String key) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return null;
            }
            return getStringSet(key, null);
        } else {
            return null;
        }
    }

    /**
     * Retrieve a set of String value from the globals provider.
     *
     * @param key          The name of the globals provider to retrieve.
     * @param defaultValue Value to return if this globals provider does not exist.
     * @return Returns the globals provider value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Set<String> getStringSet(String key, Set<String> defaultValue) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return defaultValue;
            }
            return mPreferences.getStringSet(key, defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Remove globals provider for key
     * Only the owner(who ceate) can remove!
     *
     * @param key The name of the globals provider
     */
    public static void remove(String key) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return;
            }
            remove(key, false);
        }
    }

    /**
     * Remove globals provider for key
     * Only the owner(who ceate) can remove!
     *
     * @param key      The name of the globals provider
     * @param isCommit consider using apply instead
     */
    public static void remove(String key, boolean isCommit) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return;
            }
            if (isCommit) {
                mPreferences.edit().remove(key).commit();
            } else {
                mPreferences.edit().remove(key).apply();
            }
        }
    }

    protected static void dispatchGlobalsProviderChanged(String key) {
        if (isInitialize()) {
            for (OnGlobalsProviderChangeListener l : mListeners) {
                l.onGlobalsProviderChanged(key);
            }
        }
    }

    protected static void dispatchGlobalsProviderObserver(String key) {
        if (isInitialize()) {
            ContentResolver cr = mContext.getContentResolver();
            Uri uri = getUriFor(key);
            if (cr != null && uri != null) {
                cr.notifyChange(uri, null);
            }
        }
    }

    /**
     * Construct the content URI for a particular name/value pair,
     * useful for monitoring changes with a ContentObserver.
     *
     * @param key The key of the globals provider we car about
     * @return the corresponding content URI, or null if not present
     */
    public static Uri getUriFor(String key) {
        if (isInitialize()) {
            if (key == null) {
                LogUtils.w(TAG, "key was null");
                return null;
            }
            return Uri.withAppendedPath(GlobalsContract.CONTENT_URI, key);
        } else {
            return null;
        }
    }

    /**
     * Registers a callback to be invoked when a change happens to a globals provider.
     *
     * @param listener The callback that will run.
     */
    public static void registerOnGlobalsProviderChangeListener(OnGlobalsProviderChangeListener listener) {
        if (isInitialize()) {
            if (listener == null) {
//            throw new IllegalArgumentException("listener should not be null");
                LogUtils.w(TAG, "listener should not be null");
            }

            if (!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }
    }

    /**
     * Unregisters a previous callback.
     *
     * @param listener The callback that should be unregistered.
     * @see #registerOnGlobalsProviderChangeListener
     */
    public static void unregisterOnGlobalsProviderChangeListener(OnGlobalsProviderChangeListener listener) {
        if (isInitialize()) {
            if (listener == null) {
//            throw new IllegalArgumentException("listener should not be null");
                LogUtils.w(TAG, "listener should not be null");
            }

            if (mListeners.contains(listener)) {
                mListeners.remove(listener);
            }
        }
    }

    /**
     * Interface definition for a callback to be invoked when a globals
     * provider is changed.
     */
    public interface OnGlobalsProviderChangeListener {
        /**
         * Called when a globals provider is changed, added, or removed.
         *
         * @param key The key of the globals provider that was changed, added, or
         *            removed.
         */
        void onGlobalsProviderChanged(String key);
    }

}
