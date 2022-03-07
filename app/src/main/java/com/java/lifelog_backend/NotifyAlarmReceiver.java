package com.java.lifelog_backend;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotifyAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
//        Notification notification = new Notification(context);
        Notification notification = null;
        String id = "mychannel";//通道id
        String name = "channel1";//通道名称
        Log.e("alarm", intent.toString());
        //String notifyText=intent.getStringExtra("notifyText");
        //int alarmId=intent.getIntExtra("alarmId",-1);
        Bundle extras = intent.getExtras();
        String notifyText = (String) extras.get("notifyText");
        int alarmId = (Integer) extras.get("alarmId");
        if (alarmId == 0) {
            return;
        }
        Log.e("alarmText", notifyText + ":" + alarmId);
        long[] pattern = new long[]{1000, 1000, 1000, 1000, 1000};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setVibrationPattern(pattern);
            channel.enableLights(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            //创建通道
            manager.createNotificationChannel(channel);
            //创建通知
            Intent nextIntent = new Intent(context, ResolveNotification.class);
            nextIntent.putExtra("notifyText", notifyText);
            notification = new Notification.Builder(context.getApplicationContext(), id)
                    //setLargeIcon 设置大图标
                    //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.wolf))
                    //setSmallIcon 设置小图标
                    .setSmallIcon(R.drawable.small_icon)
                    //setContentText 设置内容
                    .setContentText(notifyText)
                    //setStyle 设置样式
                    //.setStyle(new Notification.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.wolf)))
                    //setContentTitle 设置标题
                    .setContentTitle("LifeRec reminder")
                    //setAutoCancel点击过后取消显示
                    .setAutoCancel(true)
                    //帮对应的Activity
                    .setContentIntent(PendingIntent.getActivity(context.getApplicationContext(), 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setWhen(System.currentTimeMillis())
                    .setTicker("LifeRec reminder")
                    .setOngoing(true)
                    .build();
            notification.flags = Notification.FLAG_NO_CLEAR;
        } else {
            //安卓4.0-8.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = new Notification.Builder(context.getApplicationContext()
                )//setSmallIcon 设置小图标
                        //.setSmallIcon(R.drawable.ic_event_note_black_24dp)
                        //setContentText 设置内容
                        .setContentText(notifyText)
                        //setContentTitle 设置标题
                        .setContentTitle("LifeRec reminder")
                        //setAutoCancel点击过后取消显示
                        .setAutoCancel(false)
//                        .setContentIntent(
//                                PendingIntent.getActivity(
//                                        context.getApplicationContext(),1,new
//                                                Intent(
//                                                MainActivity.this,
//                                                NotificationResult.class),
//                                        PendingIntent.FLAG_CANCEL_CURRENT
//                                ))
                        .build();
            }
        }
        manager.notify(0, notification);
        Log.e("alarm", "发送成功:" + notifyText + ":" + Integer.toString(alarmId));
        AlarmTimer timer = new AlarmTimer();
        long repeatTime = 24 * 60 * 60 * 1000;
        long systemTime = System.currentTimeMillis();
        Log.e("alarm_next", Long.toString(systemTime + repeatTime));
        timer.setRepeatingAlarmTimer(context, systemTime + repeatTime, AlarmManager.RTC_WAKEUP, intent);
    }


}
