package com.example.android.memos;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.memos.data.NoteContract;


public class NoteCursorAdapter extends CursorAdapter {


    public NoteCursorAdapter(Context context, Cursor cursor, boolean autoReQuery) {
        super(context, cursor, autoReQuery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate and return a new view without binding any data
        return LayoutInflater.from(context).inflate(R.layout.note_item, parent, false);
    }

    // This method bind the note data (in the current row pointed by the cursor) to the given list item layout
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find note name view, so it can be populated
        TextView noteTitle = (TextView) view.findViewById(R.id.text_view_title);
        // Find note content, so it can be populated
        TextView noteContent = (TextView) view.findViewById(R.id.text_view_content);
        ImageView noteImage = (ImageView) view.findViewById(R.id.note_image_preview);

        // Extract values from the Cursor object
        String title = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_TITLE));
        String content = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_CONTENT));
        byte[] note_image = cursor.getBlob(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_IMAGE));
        if (note_image != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(note_image, 0, note_image.length);
            // Populate the image view
            noteImage.setImageBitmap(bitmap);
            noteImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            // Hide the image view if there is no image selected
            noteImage.setVisibility(View.GONE);
        }
        final Uri uri = ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI,
                cursor.getInt(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry._ID)));

        // Populate the text views with values extracted from the Cursor object
        noteTitle.setText(title);
        noteContent.setText(content);
    }
}
