package com.java.lifelog_backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;

import androidx.legacy.content.WakefulBroadcastReceiver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

public class GpsAlarmReceiver extends BroadcastReceiver {
    String gpsRecodePath = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/gps";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        WakeLocker.acquire(context);
        
        Intent i = new Intent(context, GpsServer.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
        WakeLocker.release();
    }
}
