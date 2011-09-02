/*
 * Copyright (C) 2011 Tonchidot Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.openphoto.android.app.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class UploadsProvider extends ContentProvider {
    private static final String packageName = "me.openphoto.android.app";
    public static final Uri CONTENT_URI = Uri.parse("content://" + packageName + "/uploads");

    private static final String TAG = UploadsProvider.class.getSimpleName();
    private static final String DATABASE_NAME = "uploads.db";
    private static final int DATABASE_VERSION = 2;
    private static final String PHOTOS_TABLE = "uploads";

    // Column names
    public static final String KEY_ID = "_id";
    public static final String KEY_URI = "uri";
    public static final String KEY_METADATA_JSON = "metadata";
    public static final String KEY_UPLOADED = "timestamp";

    // Column indexes
    public static final int ID_COLUMN = 0;
    public static final int URI_COLUMN = 1;
    public static final int METADATA_JSON_COLUMN = 2;
    public static final int UPLOADED_COLUMN = 3;

    private static final String DATABASE_CREATE = "CREATE TABLE " + PHOTOS_TABLE + " (" + KEY_ID
            + " INTEGER primary key autoincrement, " + KEY_URI + " VARCHAR(255) not null, "
            + KEY_METADATA_JSON + " TEXT null, " + KEY_UPLOADED + " INTEGER not null);";

    private SQLiteDatabase mDb;

    public static class HistoryDbHelper extends SQLiteOpenHelper {
        public HistoryDbHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            _db.execSQL("DROP TABLE IF EXISTS " + PHOTOS_TABLE);
            onCreate(_db);
        }
    }

    // Constants to differntiate URI requests
    private static final int CONTACTS = 1;
    private static final int CONTACT_ID = 2;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(packageName, "uplodas", CONTACTS);
        uriMatcher.addURI(packageName, "uploads/#", CONTACT_ID);
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CONTACTS:
                return "vnd.android.cursor.dir/vnd.openphoto.photo";
            case CONTACT_ID:
                return "vnd.android.cursor.item/vnd.openphoto.photo";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        HistoryDbHelper dbHelper = new HistoryDbHelper(context, DATABASE_NAME, null,
                DATABASE_VERSION);
        mDb = dbHelper.getWritableDatabase();
        return mDb != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PHOTOS_TABLE);
        switch (uriMatcher.match(uri)) {
            case CONTACT_ID:
                qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                break;
        }

        Cursor c = qb.query(mDb, projection, selection, selectionArgs, null, null, null);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId = mDb.insert(PHOTOS_TABLE, "contact", values);
        if (rowId > 0) {
            Uri insertedUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(insertedUri, null);
            return insertedUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case CONTACTS:
                count = mDb.delete(PHOTOS_TABLE, where, whereArgs);
                break;
            case CONTACT_ID:
                String segment = uri.getPathSegments().get(1);
                count = mDb.delete(PHOTOS_TABLE,
                        KEY_ID + "=" + segment
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : ""),
                        whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case CONTACTS:
                count = mDb.update(PHOTOS_TABLE, values, where, whereArgs);
                break;

            case CONTACT_ID:
                String segment = uri.getPathSegments().get(1);
                count = mDb.update(PHOTOS_TABLE, values,
                        KEY_ID + "=" + segment
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : ""),
                        whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    // public long insertEntry(HistoryEntry toInsert) {
    // ContentValues values = new ContentValues();
    // values.put(KEY_NAME, toInsert.name);
    // values.put(KEY_DATE, toInsert.timestamp);
    // values.put(KEY_LATITUDE, toInsert.latitude);
    // values.put(KEY_LONGITUDE, toInsert.longitude);
    // values.put(KEY_VCARD, toInsert.vcard);
    // return db.insert(CONTACTS_TABLE, null, values);
    // }
    //
    // public boolean removeEntry(long id) {
    // return db.delete(CONTACTS_TABLE, KEY_ID + "=" + id, null) > 0;
    // }
    //
    // public Cursor getAllEntries() {
    // return getEntries(null);
    // }
    //
    // public Cursor getEntries(String limit) {
    // return db.query(CONTACTS_TABLE, new String[] {
    // KEY_ID, KEY_NAME, KEY_DATE, KEY_LATITUDE, KEY_LONGITUDE, KEY_VCARD
    // }, null, null, null, null, KEY_DATE + " DESC", limit);
    // }
    //
    // public Cursor getEntriesLast10Minutes() {
    // Calendar past = Calendar.getInstance();
    // past.add(Calendar.MINUTE, -10);
    //
    // return db.query(CONTACTS_TABLE, new String[] {
    // KEY_ID, KEY_NAME, KEY_DATE, KEY_LATITUDE, KEY_LONGITUDE, KEY_VCARD
    // }, KEY_DATE + ">" + past.getTimeInMillis(), null, null, null, KEY_DATE +
    // " DESC");
    // }
    //
    // public HistoryEntry getEntry(long id) {
    // Cursor cursor = db.query(CONTACTS_TABLE, new String[] {
    // KEY_ID, KEY_NAME, KEY_DATE, KEY_LATITUDE, KEY_LONGITUDE, KEY_VCARD
    // }, KEY_ID + "=" + id, null, null, null, null);
    // if (cursor.moveToNext()) {
    // HistoryEntry entry = HistoryEntry.createFromCursor(cursor);
    // cursor.close();
    // return entry;
    // }
    // cursor.close();
    // return null;
    // }
    //
    // public boolean updateEntry(long id, HistoryEntry newEntry) {
    // ContentValues values = new ContentValues();
    // values.put(KEY_ID, id);
    // values.put(KEY_NAME, newEntry.name);
    // values.put(KEY_DATE, newEntry.timestamp);
    // values.put(KEY_LATITUDE, newEntry.latitude);
    // values.put(KEY_LONGITUDE, newEntry.longitude);
    // values.put(KEY_VCARD, newEntry.vcard);
    // return db.update(CONTACTS_TABLE, values, KEY_ID + "=" + id, null) > 0;
    // }
}
