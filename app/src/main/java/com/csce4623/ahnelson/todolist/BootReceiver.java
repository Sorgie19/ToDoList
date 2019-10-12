package com.csce4623.ahnelson.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.permission.RECEIVE_BOOT_COMPLETED"))
        {
            //Set alarm here
        }
    }
}
