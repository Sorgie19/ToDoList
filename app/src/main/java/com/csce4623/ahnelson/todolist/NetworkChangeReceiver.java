package com.csce4623.ahnelson.todolist;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.content.Context.MODE_PRIVATE;


public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Connected = "connectivityKey";
    String FILENAME = "save_file.txt";
    File file = new File("/data/data/com.csce4623.ahnelson.todolist/files/save_file.txt");
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if(!isOnline(context))
        {
            editor.putInt(Connected, 1);
            editor.commit();
            Toast.makeText(context,"Network is Offline",Toast.LENGTH_SHORT).show();
        }
        else //Has internet connectivity
        {
            editor.putInt(Connected, 0);
            editor.commit();
            if(file.exists())
                readFile(context);

            //Toast.makeText(context,"Broadcast Receiver Triggered | ONLINE",Toast.LENGTH_SHORT).show();
        }

    }

    public boolean isOnline(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void readFile(Context context)
    {
        FileInputStream input = null;
        try
        {
            input = context.openFileInput(FILENAME);
            InputStreamReader inputStreamReader = new InputStreamReader(input);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while((line = bufferedReader.readLine()) != null)
            {
                String[] linesplit = line.split("\\}");
                int position = Integer.parseInt(linesplit[0]) + 1;
                ContentValues myCV = new ContentValues();
                myCV.put(ToDoProvider.TODO_TABLE_COL_ID, position);
                myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, linesplit[1]);
                myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, linesplit[2]);
                myCV.put(ToDoProvider.TODO_TABLE_COL_DATE, linesplit[3]);
                myCV.put(ToDoProvider.TODO_TABLE_COL_COMPLETED, Integer.parseInt(linesplit[4]));
                context.getContentResolver().update(ToDoProvider.CONTENT_URI, myCV, ToDoProvider.TODO_TABLE_COL_ID + "=?", new String[]{String.valueOf(position)});
                Log.i(TAG, "updated successfully");
            }
            if(file.delete()){
                Log.i(TAG, ("file.txt File deleted from Project root directory"));
            }
            else
                Log.i(TAG, ("File file.txt doesn't exist in the project given directory"));
        }
        catch (FileNotFoundException e){e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}
    }
}
