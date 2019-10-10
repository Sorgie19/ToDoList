package com.csce4623.ahnelson.todolist;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Connected = "connectivityKey";
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if(!isOnline(context))
        {
            editor.putInt(Connected, 1);
            editor.commit();
            Toast.makeText(context,"Broadcast Receiver Triggered | OFFLINE",Toast.LENGTH_SHORT).show();
        }
        else //Has internet connectivity
        {
            editor.putInt(Connected, 0);
            editor.commit();
            //Toast.makeText(context,"Broadcast Receiver Triggered | ONLINE",Toast.LENGTH_SHORT).show();
        }


    }

    public boolean isOnline(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (networkInfo != null && networkInfo.isConnected());
    }
}
