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
import android.database.Cursor;
import android.net.Uri;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* package */ class GlobalsLoader {

    private static final String[] PROJECTION = {
            GlobalsContract._ID,
            GlobalsContract.KEY,
            GlobalsContract.TYPE,
            GlobalsContract.VALUE,
            GlobalsContract.PACKAGE_NAME
    };

    private static final String SELECTION = GlobalsContract.KEY + "=?";

    /**
     * This clause is used to speed up queries.
     */
    private static final String SORT_ORDER = GlobalsContract._ID + " DESC LIMIT 1";

    private GlobalsLoader() {
    }

    /**
     * Loads the {@link Cursor} of the {@link Uri}.
     *
     * @param resolver The {@link ContentResolver}.
     * @param uri      The {@link Uri} to use for the query.
     * @return the {@link Cursor} of the {@link Uri}.
     */
    public static Cursor load(ContentResolver resolver, Uri uri) {
        return resolver.query(uri, PROJECTION, null, null, SORT_ORDER);
    }

    public static Cursor loadCursor(ContentResolver resolver, Uri uri) {
        return resolver.query(uri, PROJECTION, null, null, SORT_ORDER);
    }

    /**
     * Loads the {@link Global} for the key.
     *
     * @param resolver The {@link ContentResolver}.
     * @return the {@link Global} for the key.
     */
    public static Global load(ContentResolver resolver, String key) {
        Global global = null;
        Cursor cursor = null;
        try {
            cursor = resolver.query(GlobalsContract.CONTENT_URI, PROJECTION,
                    SELECTION, new String[]{key}, SORT_ORDER);

            if (cursor == null) {
                return null;
            }

            while (cursor.moveToNext()) {
                global = Global.cursorRowToGlobal(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return global;
    }

    public static Cursor loadCursor(ContentResolver resolver, String key) {
        return resolver.query(GlobalsContract.CONTENT_URI, PROJECTION,
                SELECTION, new String[]{key}, SORT_ORDER);
    }

    /**
     * Loads the {@link Global}s on the database.
     *
     * @param resolver The {@link ContentResolver}.
     * @return the {@link Global}s on the database.
     */
    public static Map<Uri, Global> loadAll(ContentResolver resolver) {
        Map<Uri, Global> map = new ConcurrentHashMap<Uri, Global>();
        Cursor cursor = null;
        try {
            cursor = resolver.query(GlobalsContract.CONTENT_URI, PROJECTION, null, null, null);
            if (cursor == null) {
                return null;
            }

            while (cursor.moveToNext()) {
                Global global = Global.cursorRowToGlobal(cursor);
                long id = global.getId();
                Uri uri = Uri.withAppendedPath(GlobalsContract.CONTENT_URI, String.valueOf(id));
                map.put(uri, global);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return map;
    }
}
