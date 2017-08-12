package com.example.android.memos.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public class NoteContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.memos";

    public static final Uri BASE_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_NOTES = "notes";

    private NoteContract() {
    }

    public static final class NoteEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, PATH_NOTES);

        // Table name
        public static final String TABLE_NAME = "notes";

        // The _id field to index the table content
        public static final String _ID = BaseColumns._ID;

        // Note's title
        public static final String COLUMN_TITLE = "title";

        // Note's content
        public static final String COLUMN_CONTENT = "content";

        // Note's image
        public static final String COLUMN_IMAGE = "image";

        // The MIME type of the CONTENT_URI for a list of notes
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES;

        // The MIME type of the CONTENT_URI for a single note
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES;

    }


}
