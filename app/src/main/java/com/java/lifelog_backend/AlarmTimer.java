package com.java.lifelog_backend;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

//import com.prolificinteractive.materialcalendarview.CalendarDay;

/**
 * 闹钟定时工具类
 *
 * @author xulei
 * @time 2016/12/13 10:03
 */

public class AlarmTimer {

    /**
     * 设置周期性闹钟
     *
     * @param context
     * @param firstTime
     * @param cycTime
     * @param action
     * @param AlarmManagerType 闹钟的类型，常用的有5个值：AlarmManager.ELAPSED_REALTIME、
     *                         AlarmManager.ELAPSED_REALTIME_WAKEUP、AlarmManager.RTC、
     *                         AlarmManager.RTC_WAKEUP、AlarmManager.POWER_OFF_WAKEUP
     */
    public static void setRepeatingAlarmTimer(Context context, long alarmTime,
                                             int AlarmManagerType,Intent myIntent) {
        //myIntent.putExtra();
        //myIntent.setAction(action);
        int alarmId = myIntent.getIntExtra("alarmId",-1);
        alarmId += 1000;
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long trigerTime = alarmTime;
        alarm.setExact(AlarmManagerType, trigerTime, sender);
        Log.e("alarmTimer",Long.toString(trigerTime));
    }

    /**
     * 设置定时闹钟
     *
     * @param context
     * @param cycTime
     * @param action
     * @param AlarmManagerType 闹钟的类型，常用的有5个值：AlarmManager.ELAPSED_REALTIME、
     *                         AlarmManager.ELAPSED_REALTIME_WAKEUP、AlarmManager.RTC、
     *                         AlarmManager.RTC_WAKEUP、AlarmManager.POWER_OFF_WAKEUP
     */
    public static void setAlarmTimer(Context context, long cycTime, int AlarmManagerType, Intent myIntent){//CalendarDay date) {
        //传递定时日期
//        myIntent.putExtra("date", date);
//        myIntent.setAction(action);
        //给每个闹钟设置不同ID防止覆盖
        //int alarmId = SharedPreUtils.getInteger(context, "alarm_id", 0);
        //SharedPreUtils.setInteger(context, "alarm_id", ++alarmId);
        int alarmId = myIntent.getIntExtra("alramId",-1);
        alarmId += 1000;
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, myIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManagerType, cycTime, sender);
    }

    /**
     * 取消闹钟
     *
     */
    public static void cancelAlarmTimer(Context context, int alarmId) {
        alarmId += 1000;
        Intent myIntent = new Intent(context,NotifyAlarmReceiver.class);
        myIntent.setAction("NotifyAction");
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, myIntent,0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(sender);
    }
}