package com.java.lifelog_backend;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import okhttp3.MultipartBody;

public class DataServer extends Service {
    String dataRecordPath;
    String userName;
    String serverURL = "https://ir.cs.tsinghua.edu.cn/lifelogger/autoupload";
    public final int NOTICE_ID = 200;
    public final String CHANNEL_ID_STRING = "nyd002";
    public String app_name = "data_app";
    public String TAG = "dataServerActivity";
    Notification notification = new Notification();

    public DataServer() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        System.out.println("Create Data Service");
        Log.i(TAG, "Create Data Service");
        dataRecordPath = Environment.getExternalStorageDirectory()+"/com.java.lifelog_backend/";
//        setStartForeGround();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setStartForeGround();
        WakeLocker.acquire(this.getBaseContext());
        int interval = 1 * 61 * 1000;
        String uploadStatus = uploadData();
        if(!uploadStatus.equals("success"))
        {
            interval = 5 * 61 * 1000;
        }

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        long triggerTime = SystemClock.elapsedRealtime() + interval;
        Intent i = new Intent(this, DataAlarmReceiver.class);
        i.setAction("DataAction");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
        WakeLocker.release();

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
//        stopForeground(NOTICE_ID,);
        Log.i(TAG,"Destroy dataServer");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String uploadData(){
        String result;
        Log.i(TAG, "upload start");
        getUserInfo();
        try{
            String requestURL = "";
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.add("userName",userName);
            File locFile = new File(dataRecordPath+"gps/gpsInfo.txt");
            if (locFile.exists()&&locFile.length()>0) {
                params.put("locFile", locFile);
            }
            else{
                Log.i(TAG, dataRecordPath+"gps/gpsInfo.txt");
                Log.i(TAG,"location file not exist!");
            }
            File weatherFile = new File(dataRecordPath+"weather/weatherInfo.txt");
            if (weatherFile.exists()&&weatherFile.length()>0) {
                params.put("weatherFile", weatherFile);
            }
            else{
                Log.i(TAG, dataRecordPath+"weather/weatherInfo.txt");
                Log.i(TAG,"weather file not exist!");
            }
            File wristFile = new File(dataRecordPath+"wrist/gadgetbridge");
            if (wristFile.exists()&&wristFile.length()>0) {
                params.put("wristFile", wristFile);
            }
            else{
                Log.i(TAG, dataRecordPath+"wrist/gadgetbridge");
                Log.i(TAG,"wrist file not exist!");
            }
            client.post(serverURL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.i(TAG,"upload with AHC!");
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.i(TAG,"Upload fail with AHC!");
                    Log.i(TAG, String.valueOf(error));
                }
            });

            Log.i(TAG, "upload success");
            result="success";
        } catch (Exception e) {
            Log.i(TAG, "upload error");
            e.printStackTrace();
            result="fail";
        }
        Log.i(TAG, "upload end");
        return result;
    }

    void setStartForeGround()
    {
        //安卓8.0系统的特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = null;
            mChannel = new NotificationChannel(CHANNEL_ID_STRING, app_name, NotificationManager.IMPORTANCE_HIGH);
            //使通知静音
            mChannel.setSound(null,null);
            notificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID_STRING).build();
            startForeground(NOTICE_ID, notification);
            //8.0以前的bug，可创建相同id使该通知消失
        } else {
            startForeground(NOTICE_ID, notification);
        }

    }

    private void getUserInfo() {
        String fileName = dataRecordPath+"setting/setting.json";
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            if (file.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String jsonContent = reader.readLine().toString();
                System.out.println(jsonContent);
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(jsonContent);
                if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    userName = object.get("user_id").getAsString();
                    System.out.println("User id:" + userName);
                    Log.i(TAG, userName);
                }
            }
            else{
                Log.i(TAG,"setting file not exist!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}