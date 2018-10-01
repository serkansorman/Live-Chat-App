package com.example.serkansorman.chat;

/*
 * Created by Serkan Sorman on 02.07.2018.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


/**
 * Cihaz yeniden başlatıldığında arka planda çalışan mesaj servisini başlatır.
 */
public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("DeviceBootReceiver","DeviceBootReceiver started");
        Intent resultIntent = new Intent(context,FireBaseBackgroundService.class);
        context.startService(resultIntent);

    }
}