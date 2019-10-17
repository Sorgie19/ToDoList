package com.csce4623.ahnelson.todolist;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import static com.csce4623.ahnelson.todolist.ToDoProvider.TODO_TABLE_COL_CONTENT;
import static com.csce4623.ahnelson.todolist.ToDoProvider.TODO_TABLE_COL_TITLE;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener{

    Button save, editTitle;
    EditText noteContent, datePicker;
    TextView noteTitle;
    CheckBox taskDone;
    int position;
    final Context c = this;
    private BroadcastReceiver broadcastReceiver;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Connected = "connectivityKey";
    SharedPreferences sharedpreferences;
    int connectivityStatus;
    String FILENAME = "save_file.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        initializeComponents();

        position = getIntent().getExtras().getInt("POSITION");

        String test = String.valueOf(position);
        Toast.makeText(getApplicationContext(), test, Toast.LENGTH_SHORT).show();
        save = (Button) findViewById(R.id.btnSave);
        noteContent = (EditText) findViewById(R.id.etNoteContent);
        datePicker = (EditText) findViewById(R.id.etDatePicker);
        noteTitle = (TextView) findViewById(R.id.tvNoteTitle);
        taskDone = (CheckBox) findViewById(R.id.checkBox);
        editTitle = (Button) findViewById(R.id.editTitle);
        broadcastReceiver = new NetworkChangeReceiver();
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        getCurrentNote();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    //Set the OnClick Listener for buttons
    void initializeComponents(){
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.editTitle).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnSave:
                updateNote();
                finish();
                break;
            case R.id.editTitle:
                editTitle();
            //This shouldn't happen
            default:
                break;
        }
    }

    public void getCurrentNote()
    {
        //Create the projection for the query
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT,
                ToDoProvider.TODO_TABLE_COL_DATE,
                ToDoProvider.TODO_TABLE_COL_COMPLETED};


        //Perform the query, gets all data
        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,null);
        if(myCursor != null & myCursor.getCount() > 0) {
            //Move the cursor to the beginning
            myCursor.moveToPosition(position);
            noteTitle.setText(myCursor.getString(1));
            noteContent.setText(myCursor.getString(2));
            datePicker.setText(myCursor.getString(3));

            if(myCursor.getInt(4) == 1)
                taskDone.setChecked(true);
            else
                taskDone.setChecked(false);
        }

    }

    public void updateNote()
    {
        if(sharedpreferences.getInt(Connected, connectivityStatus) == 0) {
            ContentValues myCV = new ContentValues();
            //Put key_value pairs based on the column names, and the values
            myCV.put(TODO_TABLE_COL_TITLE, noteTitle.getText().toString());
            myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, noteContent.getText().toString());
            myCV.put(ToDoProvider.TODO_TABLE_COL_DATE, datePicker.getText().toString());
            if (taskDone.isChecked())
                myCV.put(ToDoProvider.TODO_TABLE_COL_COMPLETED, 1);
            else
                myCV.put(ToDoProvider.TODO_TABLE_COL_COMPLETED, 0);
            //Perform the insert function using the ContentProvider
            getContentResolver().update(ToDoProvider.CONTENT_URI, myCV, ToDoProvider.TODO_TABLE_COL_ID + "=?", new String[]{String.valueOf(position + 1)});
        }
        else
        {
            String titleOfNote = noteTitle.getText().toString();
            String contentOfNote = noteContent.getText().toString();
            String dueDate = datePicker.getText().toString();
            int checkedBox;
            if(taskDone.isChecked())
                checkedBox = 1;
            else
                checkedBox = 0;

            String lineToWrite = position +"}" + titleOfNote + "}" + contentOfNote + "}" + dueDate + "}" + checkedBox + "}\n";
            writeFile(lineToWrite);
        }

    }

    public void editTitle()
    {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
        View mView = layoutInflaterAndroid.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
        alertDialogBuilderUserInput.setView(mView);

        final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.edittext);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        noteTitle.setText(userInputDialogEditText.getText().toString());
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

    public void writeFile(String lineToWrite)
    {
        FileOutputStream output = null;
        try{
            output = openFileOutput(FILENAME, MODE_APPEND);
            output.write(lineToWrite.getBytes());
            //Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILENAME, Toast.LENGTH_LONG).show();
        }
        catch (FileNotFoundException e){e.printStackTrace();}
        catch (IOException e){e.printStackTrace();}
        finally {
            if(output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}