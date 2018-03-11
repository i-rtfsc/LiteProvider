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

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A transaction for a database.
 */
/* package */ class Transaction {

    /**
     * Indicates whether this transaction is a batch operation.
     */
    private final boolean mBatch;
    private List<SQLiteDatabase> mDatabasesForTransaction;
    private Map<String, SQLiteDatabase> mDatabaseMap;

    /**
     * Indicates whether this transaction has changed the databases.
     */
    private boolean mDirty;

    /**
     * Indicates whether this transaction has not been yielded.
     */
    private boolean mYieldFailed;

    /**
     * {@link Uri}s that have changed in this transaction.
     * The hash code is calculated from a string representation of each {@link Uri}.
     *
     * @see Uri#hashCode()
     */
    private Set<Uri> mDirtyUris = new HashSet<Uri>();

    /**
     * Create a transaction.
     *
     * @param batch The flag that indicates whether this transaction is a batch operation.
     */
    public Transaction(boolean batch) {
        mBatch = batch;
        mDatabasesForTransaction = new ArrayList<SQLiteDatabase>();
        mDatabaseMap = new HashMap<String, SQLiteDatabase>();
        mDirty = false;
    }

    public boolean isBatch() {
        return mBatch;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void markDirty() {
        mDirty = true;
    }

    public void markDirty(Uri uri) {
        if (uri == null) {
            return;
        }

        mDirtyUris.add(uri);
        markDirty();
    }

    public Set<Uri> getDirtyUris() {
        return mDirtyUris;
    }

    public void markYieldFailed() {
        mYieldFailed = true;
    }

    public void startTransactionForDb(SQLiteDatabase db, String tag, SQLiteTransactionListener l) {
        if (!hasDbInTransaction(tag)) {
            mDatabasesForTransaction.add(db);
            mDatabaseMap.put(tag, db);
            if (l != null) {
                db.beginTransactionWithListener(l);
            } else {
                db.beginTransaction();
            }
        }

    }

    public boolean hasDbInTransaction(String tag) {
        return mDatabaseMap.containsKey(tag);
    }

    public SQLiteDatabase getDbForTag(String tag) {
        return mDatabaseMap.get(tag);
    }

    public SQLiteDatabase removeDbForTag(String tag) {
        SQLiteDatabase db = mDatabaseMap.get(tag);
        mDatabaseMap.remove(tag);
        mDatabasesForTransaction.remove(db);
        return db;
    }

    public void markSuccessful(boolean callerIsBatch) {
        if (!mBatch || callerIsBatch) {
            for (SQLiteDatabase db : mDatabasesForTransaction) {
                db.setTransactionSuccessful();
            }
        }
    }

    public void finish(boolean callerIsBatch) {
        if (!mBatch || callerIsBatch) {
            for (SQLiteDatabase db : mDatabasesForTransaction) {
                if (mYieldFailed && !db.isDbLockedByCurrentThread()) {
                    continue;
                }
                db.endTransaction();
            }
            mDatabasesForTransaction.clear();
            mDatabaseMap.clear();
            mDirty = false;
            if (mDirtyUris != null) {
                mDirtyUris.clear();
            }
        }
    }

}
