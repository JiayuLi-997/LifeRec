package com.java.lifelog_backend;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class NotifyService extends Service {

    private int maxAlarmId = 0;

    public NotifyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AlarmTimer Ttimer = new AlarmTimer();
        for (int alarmId = 0; alarmId <= 30; alarmId++) {
            try {
                Ttimer.cancelAlarmTimer(this, alarmId);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Alarm", "Cancel Alarm not exist: " + alarmId);
            }
        }

        AlarmTimer timer = new AlarmTimer();

        Bundle extras = intent.getExtras();

        String traceStr = (String) extras.get("trace_time");
        if (traceStr != null) {
            int[] traceTime = time2Int(traceStr);
            String notifyText = "Please record today's activities!";
            setDailyTimer(traceTime[0], traceTime[1], 1, notifyText);
        }
        String breakfastStr = (String) extras.get("breakfast");
        if (breakfastStr != null) {
            int[] breakfastTime = time2Int(breakfastStr);
            String notifyText = "Please remember to take a photo for breakfast!";
            setDailyTimer(breakfastTime[0], breakfastTime[1], 2, notifyText);
        }

        String lunchStr = (String) extras.get("lunch");
        if (lunchStr != null) {
            int[] lunchTime = time2Int(lunchStr);
            String notifyText = "Please remember to take a photo for lunch!";
            setDailyTimer(lunchTime[0], lunchTime[1], 3, notifyText);
        }

        String dinnerStr = (String) extras.get("dinner");
        if (dinnerStr != null) {
            int[] dinnerTime = time2Int(dinnerStr);
            String notifyText = "Please remember to take a photo for supper!";
            setDailyTimer(dinnerTime[0], dinnerTime[1], 4, notifyText);
        }

        try {
            String clocksID = (String) extras.get("clocks");
            String notifyText = "Please mark your current feeling!";
            setCycTimer(clocksID, 5, notifyText);
            Toast.makeText(getApplicationContext(), "Set all notification!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Set notifications except routine records!", Toast.LENGTH_LONG).show();
        }


        return super.onStartCommand(intent, flags, startId);
    }

    public void setDailyTimer(int hour, int minute, int alarmId, String notifyText) {
        Log.e("alarm_daily", hour + ":" + minute + "_" + notifyText);
        long systemTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        long selectTime = calendar.getTimeInMillis();
        if (systemTime > selectTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }
        // 24小时一次
//        long repeatTime = 24*60*60*1000;
        AlarmTimer timer = new AlarmTimer();
        Intent myIntent = new Intent(this, NotifyAlarmReceiver.class);
        myIntent.setAction("NotifyAction");
        myIntent.putExtra("notifyText", notifyText);
        myIntent.putExtra("alarmId", alarmId);
        Log.e("alarm_time", Long.toString(selectTime));
        timer.cancelAlarmTimer(this, alarmId);
        timer.setRepeatingAlarmTimer(this, selectTime, AlarmManager.RTC_WAKEUP, myIntent);
    }

    public void setCycTimer(String reminderClocks, int alarmStartId, String notifyText) {
        int alarmId = alarmStartId;

        int[] hours = {7, 9, 11, 12, 13, 15, 16, 17, 18, 19, 21, 22, 23, 23};
        int[] minutes = {50, 40, 30, 20, 20, 10, 10, 0, 0, 10, 0, 0, 0, 50};

        for (String clockId : reminderClocks.split(",")) {
            int i = Integer.parseInt(clockId);
            setDailyTimer(hours[i], minutes[i], alarmId, notifyText);
            alarmId++;
        }
        maxAlarmId = alarmId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AlarmTimer timer = new AlarmTimer();
        for (int alarmId = 0; alarmId <= maxAlarmId; alarmId++) {
            timer.cancelAlarmTimer(this, alarmId);
        }
        // 关闭alarm
//        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        Intent i = new Intent(this, AlarmReceiver.class);
//        manager.cancel(pi);
    }

    private int[] time2Int(String str) {
        String[] timeStr = str.split(":");
        int[] time = new int[]{Integer.parseInt(timeStr[0]), Integer.parseInt(timeStr[1])};
        return time;
    }
}
