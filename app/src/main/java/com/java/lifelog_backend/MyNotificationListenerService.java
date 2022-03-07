package com.java.lifelog_backend;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MyNotificationListenerService extends NotificationListenerService {

    private long LastTime = 0;

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        getNotifyData(sbn);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        getNotifyData(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationRemoved(sbn, rankingMap);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason) {
        super.onNotificationRemoved(sbn, rankingMap, reason);
    }

    private void getNotifyData(StatusBarNotification sbn) {
        try {
            if (!"com.tencent.qqmusic".equals(sbn.getPackageName()) & !"com.netease.cloudmusic".equals(sbn.getPackageName())) {
                return;
            }
            String tickerText = sbn.getNotification().tickerText.toString();
            Log.d("Music_ticker", tickerText);
            long Time = sbn.getPostTime();
            Log.d("Music_time", Long.toString(Time));
            if (Time - LastTime < 1000) {
                LastTime = Time;
                return;
            }
            LastTime = Time;
            String musicRecordPath = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/music";
            File file = new File(musicRecordPath + "/musicHistory.txt");
            Log.d("Music_path", musicRecordPath);
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
                out.write(tickerText + "\t" + Long.toString(Time));
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
//            Bundle extras = sbn.getNotification().extras;
//            String title = extras.getString(Notification.EXTRA_TITLE, ""); //通知标题
//            String content = extras.getString(Notification.EXTRA_TEXT, "");//通知内容
//            // Log.d("Music",title+":"+content);
//            String uri = extras.getString(Notification.AUDIO_ATTRIBUTES_DEFAULT.toString(),"");
//            Log.d("Music_uri","uri is:"+uri);
        } catch (Exception e) {
            Log.e("Music_error", e.toString());
            e.printStackTrace();
        }
    }
}
