package com.example.android.memos.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


public class NoteProvider extends ContentProvider {

    // Tag for the Log Messages
    public static final String LOG_TAG = NoteProvider.class.getSimpleName();

    // Uri matcher code for the content URI for the notes table
    private static final int NOTES = 100;
    // Uri matcher code for the content URI for a single note
    private static final int NOTE_ID = 101;

    // UriMatcher object to match a content URI to a corresponding code.
    // The input passed into the constructor represent the code to return for the root URI.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer.
    static {
        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES, NOTES);
        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES + "/#", NOTE_ID);
    }

    // Database helper object
    private NoteDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        // Create and initialize NoteDbHelper to gain access to the notes database.
        mDbHelper = new NoteDbHelper(getContext());
        return true;
    }

    // Perform the query for the given URI by using projection, selection, selection arguments and sort order.
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor = null;
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                // Querying multiple rows directly with the given projection, selection, selectionArgs and sort order.
                cursor = database.query(NoteContract.NoteEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder);
                break;
            case NOTE_ID:
                // For a single note, we are extracting the id
                // Example: "content://com.example.android.memos/notes/5"
                selection = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(NoteContract.NoteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
        }
        // Setting notification URI on the cursor, so we know what content URI the cursor was created for.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        // Returning the cursor object
        return cursor;
    }

    // Returns the MIME type of data for the content URI
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return NoteContract.NoteEntry.CONTENT_LIST_TYPE;
            case NOTE_ID:
                return NoteContract.NoteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " With match " + match);
        }
    }

    // Inserting new data into the provider
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return insertNote(uri, values);
            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }
    }

    // Helper method for inserting note
    private Uri insertNote(Uri uri, ContentValues values) {
        // Open a database to write into
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert into the database and return the new register _ID
        long id = database.insert(NoteContract.NoteEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert new row for " + uri);
            return null;
        }
        // Setting notification URI so the cursor can be updated
        getContext().getContentResolver().notifyChange(uri, null);
        // Returning the new Uri with the ID appended
        return ContentUris.withAppendedId(uri, id);

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of row/s
        int rowDeleted;
        // Match the Uri to know which type of query is being done
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                // Detele all rows that match the selection and selectionArgs
                rowDeleted = database.delete(NoteContract.NoteEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case NOTE_ID:
                // Delete a single row given by the ID in the URI
                selection = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowDeleted = database.delete(NoteContract.NoteEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If one or more rows are deleted, then notify all listeners that the data at the given URI
        // has changed
        if (rowDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                // Setting notification URI so the cursor can be updated automatically
                getContext().getContentResolver().notifyChange(uri, null);
                return update(uri, values, selection, selectionArgs);
            case NOTE_ID:
                // Setting notification URI, so the cursor can be updated automatically
                getContext().getContentResolver().notifyChange(uri, null);
                selection = NoteContract.NoteEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateNote(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // Helper method for updating a note. Returning the number of rows that were successfully updated
    private int updateNote(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Open a writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // If there is no value to update - do not bother
        if (values.size() == 0) {
            return 0;
        }

        int rowUpdated = database.update(NoteContract.NoteEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        if (rowUpdated != 0) {
            // If one or more rows were updated, notify all listeners that the data at the given URI has been changed
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returning the number of row/s that were updated
        return rowUpdated;
    }
}
