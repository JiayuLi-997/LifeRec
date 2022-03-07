package com.java.lifelog_backend;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class GpsServer extends Service {
    //GPS
    LocationManager lm;
    //定位信息
    Location lc;
    WeatherAPI weatherAPI;
    Context mainContext;
    String gpsRecodePath;
    String weatherRecodePath;
    Calendar calendars;

    public final int NOTICE_ID = 100;
    public final String CHANNEL_ID_STRING = "nyd001";
    public String app_name = "APP";

    public Handler weatherHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String weatherInfo = (String) msg.obj;
            if (weatherInfo != null) {
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(weatherInfo);
                calendars = Calendar.getInstance();
                calendars.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                String timeInfo = String.valueOf(calendars.get(Calendar.YEAR) + "," +
                        (calendars.get(Calendar.MONTH) + 1) + "," +
                        calendars.get(Calendar.DATE) + ", " +
                        calendars.get(Calendar.HOUR_OF_DAY) + ":" +
                        calendars.get(Calendar.MINUTE) + ":" +
                        calendars.get(Calendar.SECOND));
                if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    object.addProperty("timeInfo", timeInfo);
                    File file = new File(weatherRecodePath + "/weatherInfo.txt");
                    BufferedWriter out = null;
                    try {
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
                        out.newLine();//换行
                        out.write(object.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Create GPS Serve");
        gpsRecodePath = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/gps";
        weatherRecodePath = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/weather";
        weatherAPI = new WeatherAPI(weatherHandler);
        mainContext = getApplicationContext();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        calendars = Calendar.getInstance();
        calendars.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        setStartForeGroud();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WakeLocker.acquire(this.getBaseContext());
        int interval = 0;

        lc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lc != null)
        {
            String locationStr = lc.getLongitude() + "," + lc.getLatitude();
            System.out.println(lc.toString());
            gpsUpdate(lc);
            weatherAPI.getWeather(mainContext, locationStr);
            //若成功则每15分钟请求一次
            interval = 15 * 60 * 1000;
        }
        else{
            interval = 3 * 60 * 1000;
        }
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        long trigerTime = SystemClock.elapsedRealtime() + interval;
        Intent i = new Intent(this, GpsAlarmReceiver.class);
        i.setAction("GpsAction");
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigerTime, pi);
        WakeLocker.release();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void gpsUpdate(Location location)
    {
        if (location != null)
        {
            calendars = Calendar.getInstance();
            calendars.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            String timeInfo = String.valueOf(calendars.get(Calendar.YEAR)+","+
                    (calendars.get(Calendar.MONTH)+1) + ","+
                    calendars.get(Calendar.DATE) + ", " +
                    calendars.get(Calendar.HOUR_OF_DAY) + ":" +
                    calendars.get(Calendar.MINUTE) + ":" +
                    calendars.get(Calendar.SECOND));
            JsonObject object = new JsonObject();
            object.addProperty("longitude", location.getLongitude());
            object.addProperty("latitude", location.getLatitude());
            object.addProperty("altitude", location.getAltitude());
            object.addProperty("speed", location.getSpeed());
            object.addProperty("direction", location.getBearing());
            object.addProperty("accuracy", location.getAccuracy());
            object.addProperty("timeInfo", timeInfo);
            File file = new File(gpsRecodePath + "/gpsInfo.txt");
            BufferedWriter out = null;
            try{
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if(!file.exists())
                {
                    file.createNewFile();
                }
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
                out.newLine();//换行
                out.write(object.toString());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally {
                try {
                    if(out != null){
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    void setStartForeGroud()
    {
        //安卓8.0系统的特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = null;
            mChannel = new NotificationChannel(CHANNEL_ID_STRING, app_name, NotificationManager.IMPORTANCE_HIGH);
            //使通知静音
            mChannel.setSound(null,null);
            notificationManager.createNotificationChannel(mChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID_STRING).build();
            startForeground(NOTICE_ID, notification);
            //8.0以前的bug，可创建相同id使该通知消失
        } else {
            startForeground(NOTICE_ID, new Notification());
        }

    }
}
