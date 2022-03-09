package com.java.lifelog_backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DataAlarmReceiver extends BroadcastReceiver {

    String TAG="DataAlarmActivity";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Receive!");
        WakeLocker.acquire(context);

    String autoStatus = getAutoSetting();
    if(autoStatus.contains("open")){
        Intent i = new Intent(context, DataServer.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
        WakeLocker.release();
    }
    }

    private String getAutoSetting(){
        String path = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend";
        File file = new File(path + "/auto_upload.txt");
        try{
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                Log.i(TAG,"Auto file not exist");
                return "close";
            }
            else{
                InputStream instream = new FileInputStream(file);
                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line= buffreader.readLine();
                    instream.close();
                    Log.i(TAG,"Auto file: "+line);
                    return line;
                }
                else{
                    Log.i(TAG,"Auto file read error");
                    return "close";
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, e.toString());
            return "close";
        }

    }
}