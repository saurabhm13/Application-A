package com.example.applicationa;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import java.util.HashMap;

// ContentProvider for handling album data
public class AlbumProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.albumprovider";
    public static final String URL = "content://" + AUTHORITY + "/users";

    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String id = "id";
    static final String title = "title";
    static final String artist = "artist";

    static final int uriCode = 1;
    static final UriMatcher uriMatcher;
    private static HashMap<String, String> values;

    static {

        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, "users", uriCode);

        // to access a particular row
        // of the table
        uriMatcher.addURI(AUTHORITY, "users/*", uriCode);
    }


    public AlbumProvider() {
    }

    // Returns the MIME type of the data for the content URI
    @Override
    public String getType(Uri uri) {
        if (uriMatcher.match(uri) == uriCode) {
            return "vnd.android.cursor.dir/users";
        }
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }

    // Initializes the content provider
    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    // Queries the content provider for data
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        if (uriMatcher.match(uri) == uriCode) {
            qb.setProjectionMap(values);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == "") {
            sortOrder = id;
        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    // Queries the content provider for data
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert(TABLE_NAME, "", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLiteException("Failed to add a record into " + uri);
    }

    // Updates data in the content provider
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        if (uriMatcher.match(uri) == uriCode) {
            count = db.update(TABLE_NAME, values, selection, selectionArgs);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        // Send a notification
        Notification notification = new Notification();
        notification.sendNotification(getContext());
        return count;
    }

    // Deletes data from the content provider
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        if (uriMatcher.match(uri) == uriCode) {
            count = db.delete(TABLE_NAME, selection, selectionArgs);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    // creating object of database
    // to perform query
    private SQLiteDatabase db;

    // declaring name of the database
    static final String DATABASE_NAME = "UserDB";

    // declaring table name of the database
    static final String TABLE_NAME = "Users";

    // declaring version of the database
    static final int DATABASE_VERSION = 1;

    // sql query to create the table
    static final String CREATE_DB_TABLE = "CREATE TABLE " + TABLE_NAME
            + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + " title TEXT, "
            + " artist TEXT);";

    // creating a database
    private static class DatabaseHelper extends SQLiteOpenHelper {

        // defining a constructor
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // creating a table in the database
        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            // sql query to drop a table
            // having similar name
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}