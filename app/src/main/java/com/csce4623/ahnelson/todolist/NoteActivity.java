package com.csce4623.ahnelson.todolist;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import static com.csce4623.ahnelson.todolist.ToDoProvider.TODO_TABLE_COL_CONTENT;
import static com.csce4623.ahnelson.todolist.ToDoProvider.TODO_TABLE_COL_TITLE;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener{

    Button save;
    EditText noteContent, datePicker;
    TextView noteTitle;
    CheckBox taskDone;
    int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        initializeComponents();

        position = getIntent().getExtras().getInt("POSITION");
        save = (Button) findViewById(R.id.btnSave);
        noteContent = (EditText) findViewById(R.id.etNoteContent);
        datePicker = (EditText) findViewById(R.id.etDatePicker);
        noteTitle = (TextView) findViewById(R.id.tvNoteTitle);
        taskDone = (CheckBox) findViewById(R.id.checkBox);
        getCurrentNote();
    }

    //Set the OnClick Listener for buttons
    void initializeComponents(){
        findViewById(R.id.btnSave).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            //If new Note, call createNewNote()
            case R.id.btnSave:
                updateNote();
                finish();
                break;
            //If delete note, call deleteNewestNote()
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


        //Perform the query, with ID Descending
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
        ContentValues myCV = new ContentValues();
        //Put key_value pairs based on the column names, and the values
        myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, noteContent.getText().toString());
        myCV.put(ToDoProvider.TODO_TABLE_COL_DATE, datePicker.getText().toString());
        if (taskDone.isChecked())
            myCV.put(ToDoProvider.TODO_TABLE_COL_COMPLETED, 1);
        else
            myCV.put(ToDoProvider.TODO_TABLE_COL_COMPLETED, 0);
        //Perform the insert function using the ContentProvider
        getContentResolver().update(ToDoProvider.CONTENT_URI, myCV, ToDoProvider.TODO_TABLE_COL_ID+"=?", new String[] {String.valueOf(position + 1)});

    }


}