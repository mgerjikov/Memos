package com.example.android.memos;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.memos.data.NoteContract;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = NoteActivity.class.getSimpleName();

    // Final for the image intent request code
    private final static int SELECT_IMAGE = 200;

    // Uri Loader
    private static final int URI_LOADER = 0;

    private EditText mTitleEditText;
    private EditText mContentEditText;
    private String mTitleName;
    private String mContentName;

    private Button mBrowseImage;
    private ImageView mNoteImageView;
    private Bitmap mNoteBitmap;

    // Uri received with the intent from MainActivity
    private Uri mNoteUri;

    // Boolean to check whether or not the register has changed
    private boolean mNoteHasChanged = false;

    // OnTouchListener that listens for any user touches on a View
    View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mNoteHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // Receive Uri data from Intent
        Intent intent = getIntent();
        mNoteUri = intent.getData();

        // Check if Uri is null or not
        if (mNoteUri == null) {
            // If it's null that means a new note
            setTitle(getString(R.string.add_new_memo_label));
            // Invalidate options menu (delete button), since there is nothing to delete
            invalidateOptionsMenu();
        } else {
            // If it's not null that means a note will be edited
            setTitle(getString(R.string.edit_memo_label));
            // Kick off LoaderManager
            getLoaderManager().initLoader(URI_LOADER, null, this);
        }

        // Find all relevant views that we will need to read or show user input onto
        initializeViews();

        // Set OnTouchListener to all relevant views
        setOnTouchListener();
    }

    private void setOnTouchListener() {
        mTitleEditText.setOnTouchListener(mTouchListener);
        mContentEditText.setOnTouchListener(mTouchListener);
        mBrowseImage.setOnTouchListener(mTouchListener);

    }

    private void initializeViews() {
        // Initialize EditText's
        mTitleEditText = (EditText) findViewById(R.id.edit_text_title);
        mContentEditText = (EditText) findViewById(R.id.edit_text_content);

        // Initialize image view to show preview of the image
        mNoteImageView = (ImageView) findViewById(R.id.note_image);

        // Initialize button to browse image
        mBrowseImage = (Button) findViewById(R.id.button_browse_image);
        mBrowseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    // Handle the result of the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if request code, result and intent match the image
        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            // Get image Uri
            Uri selectedImage = data.getData();
            Log.v(LOG_TAG, "Uri: " + selectedImage.toString());
            if (selectedImage != null) {
                // Get image file path
                String[] filePath = {MediaStore.Images.Media.DATA};
                // Create cursor object and query image
                Cursor cursor = getContentResolver().query(selectedImage, filePath, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePath[0]);
                // Get image path from cursor
                String imagePath = cursor.getString(columnIndex);
                // Close cursor to avoid memory leaks
                cursor.close();
                // Set the image to a Bitmap object
                mNoteBitmap = BitmapFactory.decodeFile(imagePath);
                mNoteBitmap = getBitmapFromUri(selectedImage);
                // Set Bitmap to the image view
                mNoteImageView = (ImageView) findViewById(R.id.note_image);
                mNoteImageView.setImageBitmap(mNoteBitmap);
            } else {
                mNoteImageView.setVisibility(View.GONE);
            }

        }
    }

    // Helper method
    private Bitmap getBitmapFromUri(Uri selectedImage) {
        if (selectedImage == null || selectedImage.toString().isEmpty()) {
            return null;
        }

        // Get the dimensions of the view
        mNoteImageView = (ImageView) findViewById(R.id.note_image);
        int targetW = mNoteImageView.getWidth();
        int targetH = mNoteImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(selectedImage);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int imageW = bmOptions.outWidth;
            int imageH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(imageW / targetW, imageH / targetH);

            // Decode the image file into a Bitmap size to fill the view
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            input = this.getContentResolver().openInputStream(selectedImage);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            return bitmap;
        } catch (FileNotFoundException fileNotFound) {
            Log.e(LOG_TAG, "Failed to load image.", fileNotFound);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {
                Log.e(LOG_TAG, "Exception! " + ioe);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from res/menu/menu_note_activity.xml
        getMenuInflater().inflate(R.menu.menu_note_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // I this is a new note, hide
        if (mNoteUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click
            case R.id.action_add:
                if (mNoteHasChanged) {
                    saveNote();
                } else {
                    Toast.makeText(this, getString(R.string.toast_not_saved_or_updated), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_delete:
                showDeletedNotes();
                return true;
            case android.R.id.home:
                if (!mNoteHasChanged) {
                    NavUtils.navigateUpFromSameTask(NoteActivity.this);
                    return true;
                } else {
                    // If there are unsaved changes, setup a dialog to warn
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // User clicked "Cancel"
                                    NavUtils.navigateUpFromSameTask(NoteActivity.this);
                                }
                            };
                    // Dialog that notifies user if there are unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    // Handle the back button
    @Override
    public void onBackPressed() {
        // If nothing has changed, continue with closing and going back to parent activity
        if (!mNoteHasChanged) {
            super.onBackPressed();
        } else {
            // Otherwise setup a dialog to warn user
            DialogInterface.OnClickListener discard =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    };
            showUnsavedChangesDialog(discard);
        }

    }

    private void saveNote() {
        // Define whether or not EditText fields are empty
        boolean titleIsEmpty = checkIfEmpty(mTitleEditText.getText().toString().trim());
        boolean contentIsEmpty = checkIfEmpty(mContentEditText.getText().toString().trim());

        // Checking if name, content or image are null and notifying user to change it/them if necessary
        if (titleIsEmpty) {
            Toast.makeText(this, getString(R.string.toast_empty_title), Toast.LENGTH_SHORT).show();
        } else if (contentIsEmpty) {
            Toast.makeText(this, getString(R.string.toast_empty_content), Toast.LENGTH_SHORT).show();
        } else {
            // Assuming all fields are valid ( image should be optional )
            String title = mTitleEditText.getText().toString().trim();
            String content = mContentEditText.getText().toString().trim();

            // Creating new Content Values
            ContentValues values = new ContentValues();
            values.put(NoteContract.NoteEntry.COLUMN_TITLE, title);
            values.put(NoteContract.NoteEntry.COLUMN_CONTENT, content);

            if (mNoteBitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                boolean x = mNoteBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                values.put(NoteContract.NoteEntry.COLUMN_IMAGE, byteArray);
            }

            // Determining whether it's a new note or existing one
            if (mNoteUri == null) {
                Uri newUri = getContentResolver().insert(NoteContract.NoteEntry.CONTENT_URI, values);

                if (newUri == null) {
                    // Notify for unsuccessful insertion
                    Toast.makeText(this, getString(R.string.toast_error_saving), Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise notify for successful insertion
                    Toast.makeText(this, getString(R.string.toast_successful_save), Toast.LENGTH_SHORT).show();
                }
            } else {
                // This is an existing note
                int rowsAffected = getContentResolver().update(mNoteUri, values, null, null);

                if (rowsAffected == 0) {
                    Toast.makeText(this, getString(R.string.toast_error_update), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.toast_successful_update), Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        }

    }

    private boolean checkIfEmpty(String trim) {
        return TextUtils.isEmpty(trim) || trim.equals(".");
    }

    // Helper method used for confirmation before deleting note/s
    private void showDeletedNotes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.alert_delete));
        // Setting onClickListener for positive action (Yes, delete!)
        builder.setPositiveButton(getString(R.string.alert_delete_confirmation), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNote();
                finish();
            }
        });
        // Setting onClickListener for negative action (Canceling)
        builder.setNegativeButton(getString(R.string.alert_delete_cancellation), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog and continue editing
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteNote() {
        if (mNoteUri != null) {
            int rowsDeleted = getContentResolver().delete(mNoteUri, null, null);
            // Notifying user whether or not the deletion was successful
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.toast_error_deletion), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_successful_deletion), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        // Creating alert dialog with confirmation message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.alert_update));
        builder.setPositiveButton(getString(R.string.alert_update_confirmation), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.alert_update_cancellation), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                NoteContract.NoteEntry._ID,
                NoteContract.NoteEntry.COLUMN_TITLE,
                NoteContract.NoteEntry.COLUMN_CONTENT,
                NoteContract.NoteEntry.COLUMN_IMAGE
        };

        switch (id) {
            case URI_LOADER:
                return new CursorLoader(
                        this,
                        mNoteUri,
                        projection,
                        null,
                        null,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            mTitleName = data.getString(data.getColumnIndex(NoteContract.NoteEntry.COLUMN_TITLE));
            mTitleEditText.setText(mTitleName);
            mContentName = data.getString(data.getColumnIndex(NoteContract.NoteEntry.COLUMN_CONTENT));
            mContentEditText.setText(mContentName);

            byte[] bytes = data.getBlob(data.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_IMAGE));
            if (bytes != null) {
                mNoteBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mNoteImageView.setImageBitmap(mNoteBitmap);
            }

        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mTitleEditText.getText().clear();
        mContentEditText.getText().clear();
    }
}
