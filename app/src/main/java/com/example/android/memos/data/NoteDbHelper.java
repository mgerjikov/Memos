package com.example.android.memos.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class NoteDbHelper extends SQLiteOpenHelper {

    // Creating a String that contains the SQL statement for creating the notes table
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + NoteContract.NoteEntry.TABLE_NAME + " ( " +
            NoteContract.NoteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NoteContract.NoteEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
            NoteContract.NoteEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
            NoteContract.NoteEntry.COLUMN_IMAGE + " BLOB ) " +
            ";";


    // Database version
    private static final int DATABASE_VERSION = 1;

    // Name of the database file
    private static final String DATABASE_NAME = "notes.db";

    public NoteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.v("SQLite Entries Init: ", SQL_CREATE_ENTRIES);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table or read from existing one
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(db);
    }
}
