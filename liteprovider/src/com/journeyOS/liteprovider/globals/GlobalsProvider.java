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

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The globals provider.
 * The contract between this provider and applications is defined in {@link GlobalsContract}.
 *
 * @see GlobalsContract
 */
public class GlobalsProvider extends ContentProvider implements SQLiteTransactionListener {
    private static final String TAG = GlobalsProvider.class.getSimpleName();

    private static final String GLOBALS_DATABASE_TAG = "globals";

    /**
     * Indicates an invalid row ID.
     */
    private static final int INVALID_ID = -1;

    /**
     * The maximum number of batch operations between yield points.
     */
    private static final int MAX_OPERATIONS_PER_YIELD_POINT = 500;

    /**
     * The maximum number of bulk insertions between yield points.
     */
    private static final int BULK_INSERTS_PER_YIELD_POINT = 50;

    /**
     * The time before starting a new transaction if the lock was actually yielded.
     */
    protected static final int SLEEP_AFTER_YIELD_DELAY = 4000;

    private Context mContext;
    private DatabaseHelper mDatabaseHelper;
    private ThreadLocal<DatabaseHelper> mGlobalsHelper;

    /**
     * Holds the current transaction for a thread.
     */
    private ThreadLocal<Transaction> mTransactionHolder;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * The temporary {@link ContentValues} to for a database operation.
     */
    private final ContentValues mValues = new ContentValues();

    private static final int GLOBALS = 1000;
    private static final int GLOBALS_ID = 1001;

    static {
        final UriMatcher matcher = sUriMatcher;
        matcher.addURI(GlobalsContract.AUTHORITY, "provider", GLOBALS);
        matcher.addURI(GlobalsContract.AUTHORITY, "provider/#", GLOBALS_ID);
    }

    private interface GlobalsDeleteQuery {

        /**
         * The query columns to delete records from the globals table.
         */
        public static final String[] COLUMNS = new String[]{
                GlobalsContract._ID,
                GlobalsContract.PACKAGE_NAME
        };

        public static final int _ID = 0;
    }

    private interface GlobalsUpdateQuery {

        /**
         * The query columns to update records for the globals table.
         */
        public static final String[] COLUMNS = new String[]{
                GlobalsContract._ID
        };

        public static int _ID = 0;
    }

    private static final ProjectionMap sGlobalsProjectionMap = ProjectionMap.builder()
            .add(GlobalsContract._ID)
            .add(GlobalsContract.KEY)
            .add(GlobalsContract.TYPE)
            .add(GlobalsContract.VALUE)
            .add(GlobalsContract.PACKAGE_NAME)
            .build();

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDatabaseHelper = DatabaseHelper.getInstance(mContext);
        mGlobalsHelper = new ThreadLocal<DatabaseHelper>();
        mGlobalsHelper.set(mDatabaseHelper);
        mTransactionHolder = new ThreadLocal<Transaction>();
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        mGlobalsHelper.remove();
        mTransactionHolder.remove();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        mGlobalsHelper.set(mDatabaseHelper);
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        final int match = sUriMatcher.match(uri);
        setTablesProjectionMap(match, builder);
        Cursor cursor = null;
        switch (match) {
            case GLOBALS:
                cursor = queryGlobal(builder, projection, selection, selectionArgs, sortOrder);
                break;
            case GLOBALS_ID:
                String globalId = uri.getLastPathSegment();
                selectionArgs = insertSelectionArg(selectionArgs, globalId);
                builder.appendWhere(GlobalsContract._ID + "=?");
                cursor = queryGlobal(builder, projection, selection, selectionArgs,
                        sortOrder);
                break;
            default:
                break;
        }

        if (cursor != null) {
            ContentResolver resolver = mContext.getContentResolver();
            cursor.setNotificationUri(resolver, uri);
        }

        return cursor;
    }

    private Cursor queryGlobal(SQLiteQueryBuilder builder, String[] projection, String selection,
                               String[] selectionArgs, String sortOrder) {
        DatabaseHelper helper = mGlobalsHelper.get();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null,
                sortOrder, null);

        if (cursor == null) {
            return null;
        }

        return cursor;
    }

    /**
     * Starts a transaction for the caller thread.
     *
     * @param callerIsBatch The flag indicating whether the method is called for a batch operation.
     * @return The started {@link Transaction}.
     */
    private Transaction startTransaction(boolean callerIsBatch) {
        Transaction transaction = mTransactionHolder.get();
        if (transaction == null) {
            DatabaseHelper helper = mGlobalsHelper.get();
            SQLiteDatabase db = helper.getWritableDatabase();
            transaction = new Transaction(callerIsBatch);
            transaction.startTransactionForDb(db, GLOBALS_DATABASE_TAG, this);
            // Set the transaction for the caller thread.
            mTransactionHolder.set(transaction);
        }
        return transaction;
    }

    /**
     * Ends a transaction for the caller thread.
     *
     * @param callerIsBatch The flag indicating whether the method is called for a batch operation.
     */
    private void endTransaction(boolean callerIsBatch) {
        Transaction transaction = mTransactionHolder.get();
        if (transaction != null && (!transaction.isBatch() || callerIsBatch)) {
            try {
                if (transaction.isDirty()) {
                    Set<Uri> dirtyUris = transaction.getDirtyUris();
                    notifyChange(dirtyUris);
                }
                transaction.finish(callerIsBatch);
            } finally {
                // Clear the transaction for the caller thread.
                mTransactionHolder.set(null);
            }
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        mGlobalsHelper.set(mDatabaseHelper);
        Transaction transaction = startTransaction(false);
        try {
            Uri result = insertInTransaction(uri, values);
            if (result != null) {
                transaction.markDirty(result);
            }
            transaction.markSuccessful(false);
            return result;
        } finally {
            endTransaction(false);
        }
    }

    protected Uri insertInTransaction(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        long id = INVALID_ID;
        switch (match) {
            case GLOBALS:
                id = insertGlobal(match, values);
                break;
            default:
                break;
        }

        if (id < 0) {
            return null;
        }

        return ContentUris.withAppendedId(uri, id);
    }

    private long insertGlobal(int match, ContentValues values) {
//        String newKey = null;
//        Cursor cursor = null;
//        try {
//            String key = values.getAsString(GlobalsContract.KEY);
//            cursor = query(GlobalsContract.CONTENT_URI, new String[]{GlobalsContract.KEY},
//                    GlobalsContract.KEY + "=?", new String[]{key}, null);
//            if (DEBUG) Log.d(TAG, "insertGlobal() called with: cursor = [" + cursor + "], key = [" + key + "]");
//            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
//                do {
//                    newKey = cursor.getString(cursor.getColumnIndex(GlobalsContract.KEY));
//                } while (cursor.moveToNext());
//            }
//        } catch (Exception e) {
//
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        if (newKey != null) {
//            values.put(GlobalsContract.PACKAGE_NAME, getCallingPackage());
//        }
        values.put(GlobalsContract.PACKAGE_NAME, getCallingPackage());
        long globalId = INVALID_ID;
        mValues.clear();
        mValues.putAll(values);

        DatabaseHelper helper = mGlobalsHelper.get();
        SQLiteDatabase db = helper.getWritableDatabase();
        switch (match) {
            case GLOBALS:
                globalId = db.insert(DatabaseHelper.Tables.GLOBALS, null, mValues);
                break;
            default:
                break;
        }
        return globalId;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        mGlobalsHelper.set(mDatabaseHelper);
        Transaction transaction = startTransaction(false);
        try {
            List<Uri> deletedUris = deleteInTransaction(uri, selection, selectionArgs);
            int size = 0;
            if (deletedUris != null) {
                size = deletedUris.size();
                for (Uri deletedUri : deletedUris) {
                    transaction.markDirty(deletedUri);
                }
            }
            transaction.markSuccessful(false);
            return size;
        } finally {
            endTransaction(false);
        }
    }

    protected List<Uri> deleteInTransaction(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GLOBALS:
                return deleteGlobal(selection, selectionArgs);
            case GLOBALS_ID:
                long globalId = ContentUris.parseId(uri);
                return deleteGlobal(GlobalsContract._ID + "=?",
                        new String[]{String.valueOf(globalId)});
            default:
                break;
        }

        return null;
    }

    private List<Uri> deleteGlobal(String selection, String[] selectionArgs) {
        Uri uri = GlobalsContract.CONTENT_URI;
        List<Uri> uris = new ArrayList<Uri>();
        Cursor cursor = query(uri, GlobalsDeleteQuery.COLUMNS,
                selection, selectionArgs, null);

        if (cursor == null) {
            return uris;
        }

        DatabaseHelper helper = mGlobalsHelper.get();
        try {
            int count = 0;
            int lastCount = 0;
            while (cursor.moveToNext()) {
                SQLiteDatabase db = helper.getWritableDatabase();
                long globalId = cursor.getLong(GlobalsDeleteQuery._ID);
                count += db.delete(DatabaseHelper.Tables.GLOBALS,
                        GlobalsContract._ID + "=?" + " AND " + GlobalsContract.PACKAGE_NAME + "=?",
                        new String[]{String.valueOf(globalId), getCallingPackage()});
                if (count > lastCount) {
                    uris.add(ContentUris.withAppendedId(GlobalsContract.CONTENT_URI, globalId));
                }
                lastCount = count;
            }
        } finally {
            cursor.close();
        }

        return uris;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        mGlobalsHelper.set(mDatabaseHelper);
        Transaction transaction = startTransaction(false);
        try {
            List<Uri> updatedUris = updateInTransaction(uri, values, selection, selectionArgs);
            int size = 0;
            if (updatedUris != null) {
                size = updatedUris.size();
                for (Uri updatedUri : updatedUris) {
                    transaction.markDirty(updatedUri);
                }
            }
            transaction.markSuccessful(false);
            return size;
        } finally {
            endTransaction(false);
        }
    }

    protected List<Uri> updateInTransaction(Uri uri, ContentValues values, String selection,
                                            String[] selectionArgs) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        final int match = sUriMatcher.match(uri);
        setTablesProjectionMap(match, builder);
        switch (match) {
            case GLOBALS:
                return updateGlobal(builder, values, selection, selectionArgs);
            case GLOBALS_ID:
                String globalId = uri.getLastPathSegment();
                selectionArgs = insertSelectionArg(selectionArgs, globalId);
                builder.appendWhere(GlobalsContract._ID + "=?");
                return updateGlobal(builder, values, selection, selectionArgs);
            default:
                break;
        }

        return null;
    }

    private List<Uri> updateGlobal(SQLiteQueryBuilder builder, ContentValues values,
                                   String selection, String[] selectionArgs) {
        List<Uri> uris = new ArrayList<Uri>();
        mValues.clear();
        mValues.putAll(values);
        // Cannot update the ID field.
        mValues.remove(GlobalsContract._ID);

        DatabaseHelper helper = mGlobalsHelper.get();
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = builder.query(db, GlobalsUpdateQuery.COLUMNS, selection, selectionArgs,
                null, null, null, null);

        if (cursor == null) {
            return uris;
        }

        int count = 0;
        try {
            int lastCount = 0;
            while (cursor.moveToNext()) {
                long globalId = cursor.getLong(GlobalsUpdateQuery._ID);
                if (mValues.size() > 0) {
                    count += db.update(DatabaseHelper.Tables.GLOBALS, mValues,
                            GlobalsContract._ID + " =?", new String[]{String.valueOf(globalId)});
                    if (count > lastCount) {
                        uris.add(ContentUris.withAppendedId(
                                GlobalsContract.CONTENT_URI, globalId));
                    }
                    lastCount = count;
                }
            }
        } finally {
            cursor.close();
        }
        return uris;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        mGlobalsHelper.set(mDatabaseHelper);
        Transaction transaction = startTransaction(true);
        int numValues = values.length;
        int opCount = 0;
        try {
            for (int i = 0; i < numValues; i++) {
                Uri inserted = insertInTransaction(uri, values[i]);
                if (inserted != null) {
                    // Add the URI of the table as a dirty URI.
                    transaction.markDirty(inserted);
                }

                if (++opCount >= BULK_INSERTS_PER_YIELD_POINT) {
                    opCount = 0;
                    try {
                        yield(transaction);
                    } catch (RuntimeException re) {
                        transaction.markYieldFailed();
                        throw re;
                    }
                }
            }
            transaction.markSuccessful(true);
        } finally {
            endTransaction(true);
        }
        return numValues;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        mGlobalsHelper.set(mDatabaseHelper);
        int ypCount = 0;
        int opCount = 0;
        Transaction transaction = startTransaction(true);
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                if (++opCount >= MAX_OPERATIONS_PER_YIELD_POINT) {
                    throw new OperationApplicationException(
                            "Too many operations between yield points.", ypCount);
                }
                final ContentProviderOperation operation = operations.get(i);
                if (i > 0 && operation.isYieldAllowed()) {
                    opCount = 0;
                    try {
                        if (yield(transaction)) {
                            ypCount++;
                        }
                    } catch (RuntimeException e) {
                        transaction.markYieldFailed();
                        throw e;
                    }
                }

                // Note that actual operations are applied through insert(), update() or delete().
                results[i] = operation.apply(this, results, i);
            }
            transaction.markSuccessful(true);
            return results;
        } finally {
            endTransaction(true);
        }
    }

    /**
     * Yields the transaction to let other threads run.
     *
     * @param transaction The transaction to yield.
     * @return true if the transaction was yielded.
     * @see SQLiteDatabase#yieldIfContendedSafely(long)
     */
    protected boolean yield(Transaction transaction) {
        SQLiteDatabase db = transaction.getDbForTag(GLOBALS_DATABASE_TAG);
        return db != null && db.yieldIfContendedSafely(SLEEP_AFTER_YIELD_DELAY);
    }

    /**
     * Notifies the registered observer that rows were changed.
     *
     * @param dirtyUris The {@link Uri}s that were changed.
     */
    protected void notifyChange(Set<Uri> dirtyUris) {
        if (dirtyUris == null || dirtyUris.isEmpty()) {
            return;
        }

        for (Uri uri : dirtyUris) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
    }

    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case GLOBALS:
                return GlobalsContract.CONTENT_TYPE;
            case GLOBALS_ID:
                return GlobalsContract.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Sets a proper projection map to the {@link SQLiteQueryBuilder}.
     *
     * @param match   The code for matched node of a {@link Uri}.
     * @param builder The {@link SQLiteQueryBuilder} to set a projection map.
     */
    private void setTablesProjectionMap(int match, SQLiteQueryBuilder builder) {
        switch (match) {
            case GLOBALS:
            case GLOBALS_ID:
                final ProjectionMap projectionMap = sGlobalsProjectionMap;
                builder.setTables(DatabaseHelper.Tables.GLOBALS);
                builder.setProjectionMap(projectionMap);
                break;
            default:
                throw new IllegalStateException("projection map does not exist");
        }
        builder.setStrict(true);
    }

    private String[] insertSelectionArg(String[] selectionArgs, String arg) {
        if (selectionArgs == null) {
            return new String[]{arg};
        } else {
            int newLength = selectionArgs.length + 1;
            String[] newSelectionArgs = new String[newLength];
            newSelectionArgs[0] = arg;
            System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, selectionArgs.length);
            return newSelectionArgs;
        }
    }

    /**
     * The helper class for the globals database.
     */
    private static final class DatabaseHelper extends SQLiteOpenHelper {

        /**
         * The database file name.
         */
        private static final String DATABASE_NAME = "globals.db";
        /* package */ static final int DATABASE_VERSION = 1;

        private static DatabaseHelper sInstance = null;

        public interface Tables {
            public static final String GLOBALS = "globals";
        }

        /**
         * Returns an instance of the database helper.
         *
         * @param context The application context.
         * @return an instance of the database helper.
         */
        public static synchronized DatabaseHelper getInstance(Context context) {
            if (sInstance == null) {
                sInstance = new DatabaseHelper(context, DATABASE_NAME);
            }
            return sInstance;
        }

        protected DatabaseHelper(final Context context, String databaseName) {
            super(context, databaseName, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createGlobalsTable(db);
        }

        /**
         * Creates a new globals table in the database.
         * Note that the globals table will be dropped if exists.
         *
         * @param db The {@link SQLiteDatabase} in which a new globals table is created.
         */
        private void createGlobalsTable(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + Tables.GLOBALS);
            db.execSQL("CREATE TABLE " + Tables.GLOBALS +
                    " (" +
                    GlobalsContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    GlobalsContract.KEY + " TEXT NOT NULL," +
                    GlobalsContract.TYPE + " TEXT NOT NULL," +
                    GlobalsContract.VALUE + " TEXT," +
                    GlobalsContract.PACKAGE_NAME + " TEXT NOT NULL," +
                    "UNIQUE (" + GlobalsContract.KEY + ")" +
                    ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Nothing to do here yet.
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }

    @Override
    public void onBegin() {
    }

    @Override
    public void onCommit() {
    }

    @Override
    public void onRollback() {
    }

}
