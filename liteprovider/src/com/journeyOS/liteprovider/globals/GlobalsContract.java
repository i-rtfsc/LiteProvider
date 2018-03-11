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
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The contract between the globals provider and applications.
 * A row in the globals table can store any kind of a key-value pair.
 */
public final class GlobalsContract implements BaseColumns {

    /**
     * Cannot be instantiated.
     */
    private GlobalsContract() {
    }

    /**
     * The authority for the globals provider.
     */
    public static final String AUTHORITY = "com.journeyOS.globalsprovider";

    /**
     * An URI to the authority for the globals provider.
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * The content:// style URI for this table.
     */
    public static final Uri CONTENT_URI =
            Uri.withAppendedPath(AUTHORITY_URI, "globals");

    /**
     * The MIME type of the results from {@link #CONTENT_URI}.
     */
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/vnd.journeyOS.globals";

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/vnd.journeyOS.globals";

    /**
     * @see BaseColumns#_ID
     */
    public static final String _ID = "_id";

    /**
     * The packageName of a gloabl.
     * <P>Type: TEXT</P>
     */
    public static final String PACKAGE_NAME = "packageName";

    /**
     * The key of a gloabl.
     * <P>Type: TEXT</P>
     */
    public static final String KEY = "key";

    /**
     * The type of the value. The type is a string representation of class names.
     * <P>Type: TEXT</P>
     */
    public static final String TYPE = "type";

    /**
     * The value of the mapping with the specified key.
     * <P>Type: TEXT</P>
     */
    public static final String VALUE = "value";
}
