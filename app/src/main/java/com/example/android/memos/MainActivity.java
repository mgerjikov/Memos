package com.example.android.memos;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.memos.data.NoteContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int URI_LOADER = 0;
    private NoteCursorAdapter mNoteCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find reference to the ListView
        initializeListView();

        // Start LoaderManager
        getLoaderManager().initLoader(URI_LOADER, null, this);
    }

    private void initializeListView() {
        // Find the list view
        ListView listView = (ListView) findViewById(R.id.list_view);
        // Defining empty state
        View emptyView = findViewById(R.id.empty_state_view);
        listView.setEmptyView(emptyView);

        mNoteCursorAdapter = new NoteCursorAdapter(this, null, false);
        // Attaching adapter
        listView.setAdapter(mNoteCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.setData(ContentUris.withAppendedId(NoteContract.NoteEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URI_LOADER:
                // Defining projection for the cursor so that it contains all rows from the table
                String projection[] = {
                        NoteContract.NoteEntry._ID,
                        NoteContract.NoteEntry.COLUMN_TITLE,
                        NoteContract.NoteEntry.COLUMN_CONTENT,
                        NoteContract.NoteEntry.COLUMN_IMAGE
                };
                // Defining sort order
                String sortOrder = NoteContract.NoteEntry._ID + " DESC ";

                // Returning cursor loader
                return new CursorLoader(
                        this,
                        NoteContract.NoteEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        sortOrder
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        try {
            mNoteCursorAdapter.swapCursor(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mNoteCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
