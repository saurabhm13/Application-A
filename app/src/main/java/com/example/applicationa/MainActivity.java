package com.example.applicationa;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private Button save, loadData;
    private TextInputLayout title, artist;
    static MyAdapter adapter;
    String tTitle, tArtist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        save = findViewById(R.id.save);
        loadData = findViewById(R.id.loadData);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artist);

        // Register broadcast receiver for data modification events
        IntentFilter filter = new IntentFilter("com.example.DATA_MODIFIED");
        registerReceiver(dataModificationReceiver, filter);

        recyclerView = findViewById(R.id.rv_album);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tTitle = title.getEditText().getText().toString().trim();
                tArtist = artist.getEditText().getText().toString().trim();

                if (!tTitle.equals("") && !tArtist.equals("")) {
                    saveData(tTitle, tArtist);
                    showData();
                    removeFields();
                }else {
                    Toast.makeText(MainActivity.this, "Add All Fields", Toast.LENGTH_SHORT).show();
                }

            }
        });

        loadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showData();
            }
        });

        adapter.setItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String userName, String artist) {
                showEditDialog(userName, artist);
                showData();
            }
        });

        // Set delete click listener
        adapter.setDeleteClickListener(new MyAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(String userName) {
                performDeleteOperation(userName);
                showData();
            }
        });
    }

    // Method to display data from the content provider
    @SuppressLint({"Range", "SetTextI18n", "NotifyDataSetChanged"})
    public void showData() {

        Cursor cursor = getContentResolver().query(Uri.parse("content://com.example.albumprovider/users"), null, null, null, null);

        List<Album> userNames = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Album album = new Album(cursor.getString(cursor.getColumnIndex("title")), cursor.getString(cursor.getColumnIndex("artist")));
                userNames.add(album);
                cursor.moveToNext();
            }
        }

        // Update the data in the adapter
        adapter.setData(userNames);
        adapter.notifyDataSetChanged();
    }

    // Method to save data to the content provider
    public void saveData(String title, String artist) {

        ContentValues values = new ContentValues();
        values.put(AlbumProvider.title, (title));
        values.put(AlbumProvider.artist, (artist));

        getContentResolver().insert(AlbumProvider.CONTENT_URI, values);

        Toast.makeText(getBaseContext(), "New Record Inserted", Toast.LENGTH_LONG).show();
    }

    // Method to show a dialog for editing an entry
    private void showEditDialog(final String title, final String artist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Entry");

        // Set up the input
        final EditText editName = new EditText(this);
        editName.setInputType(InputType.TYPE_CLASS_TEXT);
        editName.setText(title);
        editName.setHint("New Title");

        final EditText editArtist = new EditText(this);
        editArtist.setInputType(InputType.TYPE_CLASS_TEXT);
        editArtist.setText(artist);
        editArtist.setHint("New Artist");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(editName);
        layout.addView(editArtist);
        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newTitle = editName.getText().toString();
                String newArtist = editArtist.getText().toString();

                if (!newTitle.equals("") && !newArtist.equals("")) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.POST_NOTIFICATIONS}, 0);

                    }
                    updateEntry(title, newTitle, newArtist);
                    showData();

                }else {
                    Toast.makeText(MainActivity.this, "Add All Fields", Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Method to update an entry in the content provider
    @SuppressLint("NotifyDataSetChanged")
    private void updateEntry(String userName, String newTitle, String newArtist) {

        ContentValues values = new ContentValues();
        values.put("title", newTitle);
        values.put("artist", newArtist);
        Uri uri = Uri.parse("content://com.example.albumprovider/users");
        getContentResolver().update(uri, values, "title=?", new String[]{userName});

        adapter.notifyDataSetChanged();
    }

    // Method to perform delete operation on an entry in the content provider
    @SuppressLint("NotifyDataSetChanged")
    private void performDeleteOperation(String userName) {

        Uri uri = Uri.parse("content://com.example.albumprovider/users");
        getContentResolver().delete(uri, "title=?", new String[]{userName});
        adapter.notifyDataSetChanged();
    }

    // Method to clear input fields
    private void removeFields() {
        title.getEditText().setText("");
        artist.getEditText().setText("");
    }

    // BroadcastReceiver for handling data modification events
    private final BroadcastReceiver dataModificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Notification notification = new Notification();
            notification.sendNotification(MainActivity.this);
        }
    };
}