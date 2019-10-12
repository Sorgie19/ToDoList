package com.csce4623.ahnelson.todolist;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

//Create HomeActivity and implement the OnClick listener
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private BroadcastReceiver broadcastReceiver;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Connected = "connectivityKey";
    SharedPreferences sharedpreferences;
    ListView listView;
    int connectivityStatus;
    final Context c = this;
    int noteCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        broadcastReceiver = new NetworkChangeReceiver();
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        initializeComponents();
        listView = (ListView) findViewById(R.id.listView);
        updateListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //if(sharedpreferences.getInt(Connected, connectivityStatus) == 0) {
                    Intent intent = new Intent(view.getContext(), NoteActivity.class);
                    intent.putExtra("POSITION", i);
                    startActivity(intent);
                //}
                //else
                //{
                //    Toast.makeText(getApplicationContext(), "Not connected to the internet", Toast.LENGTH_LONG).show();
                //}
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    //Set the OnClick Listener for buttons
    void initializeComponents() {
        findViewById(R.id.btnNewNote).setOnClickListener(this);
        findViewById(R.id.btnDeleteNote).setOnClickListener(this);
        findViewById(R.id.btnRefresh).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //If new Note, call createNewNote()
            case R.id.btnNewNote:
                alertDialogInput();
                break;
            //If delete note, call deleteNewestNote()
            case R.id.btnDeleteNote:
                deleteNewestNote();
                break;
            case R.id.btnRefresh:
                updateListView();
            //This shouldn't happen
            default:
                break;
        }
    }

    //Create a new note with the title "New Note" and content "Note Content"
    void createNewNote(String noteTitle)
    {
        //Create a ContentValues object
        ContentValues myCV = new ContentValues();
        //Put key_value pairs based on the column names, and the values
        myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, noteTitle);
        myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, "");
        //Perform the insert function using the ContentProvider
        getContentResolver().insert(ToDoProvider.CONTENT_URI, myCV);
        //Set the projection for the columns to be returned
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT,
                ToDoProvider.TODO_TABLE_COL_DATE,
                ToDoProvider.TODO_TABLE_COL_COMPLETED};
        //Perform a query to get all rows in the DB
        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI, projection, null, null, null);
        noteCount = myCursor.getCount() - 1;
        //Create a toast message which states the number of rows currently in the database
        Toast.makeText(getApplicationContext(), Integer.toString(myCursor.getCount()), Toast.LENGTH_LONG).show();
        //updateListView();

    }

    //Delete the newest note placed into the database
    void deleteNewestNote() {
        //Create the projection for the query
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT,
                ToDoProvider.TODO_TABLE_COL_DATE,
                ToDoProvider.TODO_TABLE_COL_COMPLETED};

        //Perform the query, with ID Descending
        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI, projection, null, null, "_ID DESC");
        if (myCursor != null & myCursor.getCount() > 0) {
            //Move the cursor to the beginning
            myCursor.moveToFirst();
            //Get the ID (int) of the newest note (column 0)
            int newestId = myCursor.getInt(0);
            //Delete the note
            int didWork = getContentResolver().delete(Uri.parse(ToDoProvider.CONTENT_URI + "/" + newestId), null, null);
            //If deleted, didWork returns the number of rows deleted (should be 1)
            if (didWork == 1) {
                //If it didWork, then create a Toast Message saying that the note was deleted
                Toast.makeText(getApplicationContext(), "Deleted Note " + newestId, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Note to delete!", Toast.LENGTH_LONG).show();

        }
        updateListView();
    }

    public void updateListView() {
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT,
                ToDoProvider.TODO_TABLE_COL_DATE,
                ToDoProvider.TODO_TABLE_COL_COMPLETED};
        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI, projection, null, null, null);
        TodoCursorAdapter todoCursorAdapter = new TodoCursorAdapter(this, myCursor);
        listView.setAdapter(todoCursorAdapter);
    }

    void alertDialogInput() {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
        View mView = layoutInflaterAndroid.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
        alertDialogBuilderUserInput.setView(mView);

        final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.edittext);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        if (sharedpreferences.getInt(Connected, connectivityStatus) == 0) {
                            createNewNote(userInputDialogEditText.getText().toString());
                            Intent intent = new Intent(c, NoteActivity.class);
                            intent.putExtra("POSITION", noteCount);
                            startActivity(intent);
                        } else
                            Toast.makeText(getApplicationContext(), "Not Connected to Internet", Toast.LENGTH_LONG).show();
                    }
                })

                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
    }

}
