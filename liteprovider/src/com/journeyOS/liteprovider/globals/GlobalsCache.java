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

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The memory cache for globals to access quickly.
 * Note that the cache is not completely synchronized with
 * the original records in the database.
 * The cache does not usually have difference from original records
 * because the globals database will not be frequently updated.
 * However, the cache might not be synchronized if you immediately access
 * records after updating records.
 * <p>
 * The cache is constructed through the change notifications of
 * the {@link GlobalsProvider}.
 * That is, this cache depends on the implementation of
 * the {@link GlobalsProvider}.
 *
 * @see GlobalsProvider#notifyChange(Set)
 */
/* package */ class GlobalsCache {

    private Context mContext;
    private ContentResolver mContentResolver;

    private boolean mLoaded = false;

    /**
     * The memory cache for globals.
     */
    private Map<Uri, Global> mMap = new ConcurrentHashMap<Uri, Global>();

    /**
     * The memory cache for globals that are currently changing on the database.
     */
    private Map<String, Object> mTempMap = new ConcurrentHashMap<String, Object>();

    private GlobalsObserver mObserver;

    private List<CacheListener> mCacheListeners = new CopyOnWriteArrayList<CacheListener>();

    /**
     * Create a new memory cache for globals.
     * Note that the cache becomes available after loading.
     *
     * @param context The application context.
     */
    public GlobalsCache(Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mObserver = new GlobalsObserver(mContext, this);
        mContentResolver.registerContentObserver(GlobalsContract.CONTENT_URI, true, mObserver);
        startLoadingFromDatabase();
    }

    /**
     * Starts loading globals from the database.
     * Note that the operation will be executed asynchronously.
     */
    private void startLoadingFromDatabase() {
        synchronized (this) {
            mLoaded = false;
        }

        Thread loader = new Thread(new Runnable() {
            @Override
            public void run() {
                loadFromDatabase();
            }
        });
        loader.start();
    }

    /**
     * Loads globals from the database into the cache.
     */
    private synchronized void loadFromDatabase() {
        Map<Uri, Global> map = GlobalsLoader.loadAll(mContentResolver);
        if (map != null) {
            mMap.putAll(map);
        }
        mLoaded = true;
        notifyAll();
    }

    public void addCacheListener(CacheListener l) {
        if (l == null) {
            throw new IllegalArgumentException("listener should not be null");
        }

        if (!mCacheListeners.contains(l)) {
            mCacheListeners.add(l);
        }
    }

    public void removeCacheListener(CacheListener l) {
        if (l == null) {
            throw new IllegalArgumentException("listener should not be null");
        }

        if (mCacheListeners.contains(l)) {
            mCacheListeners.remove(l);
        }
    }

    /**
     * Destroys the internal state of this cache.
     * This method should be called after this cache has been unnecessary.
     * No other methods may be called after destroying.
     */
    public void destroy() {
        mCacheListeners.clear();
        mContentResolver.unregisterContentObserver(mObserver);
    }

    /**
     * Waits for a loading completion.
     */
    private void awaitLoading() {
        while (!mLoaded) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized Map<String, ?> getAllAsMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        Set<Entry<Uri, Global>> entries = mMap.entrySet();
        for (Entry<Uri, Global> entry : entries) {
            Global cache = entry.getValue();
            String key = cache.getKey();
            Object value = cache.getValue();
            map.put(key, value);
        }
        return map;
    }

    public synchronized boolean contains(String key) {
        if (mTempMap.containsKey(key)) {
            return true;
        }

        awaitLoading();
        Set<Entry<Uri, Global>> entries = mMap.entrySet();
        for (Entry<Uri, Global> entry : entries) {
            Global cache = entry.getValue();
            String cacheKey = cache.getKey();
            if (cacheKey.equals(key)) {
                return true;
            }
        }

        return false;
    }

    public synchronized void put(String key, Object value) {
        mTempMap.put(key, value);

        awaitLoading();
        Set<Entry<Uri, Global>> entries = mMap.entrySet();
        Uri uri = null;
        for (Entry<Uri, Global> entry : entries) {
            Global cache = entry.getValue();
            String cacheKey = cache.getKey();
            if (cacheKey.equals(key)) {
                uri = entry.getKey();
                break;
            }
        }

        if (uri != null) {
            mMap.remove(uri);
        }
    }

    public synchronized void remove(String key) {
        mTempMap.remove(key);

        awaitLoading();
        Set<Entry<Uri, Global>> entries = mMap.entrySet();
        Uri uri = null;
        for (Entry<Uri, Global> entry : entries) {
            Global cache = entry.getValue();
            String cacheKey = cache.getKey();
            if (cacheKey.equals(key)) {
                uri = entry.getKey();
                break;
            }
        }

        if (uri != null) {
            mMap.remove(uri);
        }
    }

    public synchronized Global get(String key) {
        if (mTempMap.containsKey(key)) {
            Object value = mTempMap.get(key);
            return new Global(key, value);
        }

        awaitLoading();
        Set<Entry<Uri, Global>> entries = mMap.entrySet();
        Uri uri = null;
        for (Entry<Uri, Global> entry : entries) {
            Global cache = entry.getValue();
            String cacheKey = cache.getKey();
            if (cacheKey.equals(key)) {
                uri = entry.getKey();
                break;
            }
        }

        if (uri != null) {
            return mMap.get(uri);
        } else {
            return null;
        }
    }

    public synchronized void clear() {
        mMap.clear();
        mTempMap.clear();
    }

    private synchronized void put(Uri uri, Global global) {
        String key = global.getKey();
        mTempMap.remove(key);

        awaitLoading();
        if (uri != null) {
            mMap.put(uri, global);
        }

        dispatchInsertedOrUpdated(global);
    }

    private synchronized void remove(Uri uri) {
        awaitLoading();
        if (uri != null) {
            Global removed = mMap.remove(uri);
            if (removed != null) {
                String removedKey = removed.getKey();
                if (removedKey != null && mTempMap != null) {
                    if (mTempMap.containsKey(removedKey)) {
                        mTempMap.remove(removedKey);
                    }
                }
            }
        }

        Global global = mMap.get(uri);
        dispatchRemoved(global);
    }


    private void dispatchRemoved(Global global) {
        for (CacheListener l : mCacheListeners) {
            l.onRemoved(global);
        }
    }

    private void dispatchInsertedOrUpdated(Global global) {
        for (CacheListener l : mCacheListeners) {
            l.onInsertedOrUpdated(global);
        }
    }

    /**
     * The observer for the globals database to update the cache status.
     */
    private static class GlobalsObserver extends ContentObserver {

        private GlobalsCache mCache;
        private ContentResolver mContentResolver;

        public GlobalsObserver(Context context, GlobalsCache cache) {
            // Callbacks will be called on the main thread.
            super(new Handler(context.getMainLooper()));
            mContentResolver = context.getContentResolver();
            mCache = cache;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            synchronized (mCache) {
                mCache.awaitLoading();
            }

            if (uri == null) {
                return;
            }

            String lastPath = uri.getLastPathSegment();
            try {
                Long.parseLong(lastPath);
            } catch (NumberFormatException e) {
                return;
            }

            Cursor cursor = null;
            try {
                cursor = GlobalsLoader.loadCursor(mContentResolver, uri);
                if (cursor == null) {
                    return;
                }

                int count = cursor.getCount();
                if (count == 0) {
                    onGlobalRemoved(uri);
                    return;
                }

                if (cursor.moveToNext()) {
                    Global global = Global.cursorRowToGlobal(cursor);
                    onGlobalInsertedOrUpdated(uri, global);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        /**
         * Called when a global was removed.
         *
         * @param uri The {@link Uri} of the removed record.
         */
        private void onGlobalRemoved(Uri uri) {
            mCache.remove(uri);
        }

        /**
         * Called when a global was newly inserted or updated.
         *
         * @param uri    The {@link Uri} of the inserted or updated record.
         * @param global The inserted or updated global.
         */
        private void onGlobalInsertedOrUpdated(Uri uri, Global global) {
            mCache.put(uri, global);
        }
    }

    /**
     * The interface to notify of changes for the globals cache.
     */
    public interface CacheListener {

        /**
         * Called when the global was removed.
         *
         * @param global The removed global.
         */
        public void onRemoved(Global global);

        /**
         * Called when the global was inserted or updated.
         *
         * @param global The changed global.
         */
        public void onInsertedOrUpdated(Global global);
    }
}
