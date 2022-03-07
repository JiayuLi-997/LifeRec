package com.java.lifelog_backend;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ResolveNotification extends AppCompatActivity {

    private int DELAY_ALARM = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String notifyText = intent.getStringExtra("notifyText");
        //setContentView(R.layout.activity_resolve_notification);
        tipClick(notifyText);

    }

    public void tipClick(final String notifyText) {
        final Context context = getApplicationContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setMessage(notifyText);
        builder.setIcon(R.mipmap.ic_launcher_round);
        //点击对话框以外的区域是否让对话框消失
        builder.setCancelable(true);
        //设置反面按钮
        builder.setNegativeButton("跳过本次提醒", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "你选择了跳过提醒", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        //设置中立按钮
        builder.setNeutralButton("稍后提醒我", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "五分钟后再次提醒！", Toast.LENGTH_SHORT).show();
                AlarmTimer timer = new AlarmTimer();
                long startTime = SystemClock.currentThreadTimeMillis();
                long cycTime = 5 * 60 * 1000;
                Intent myIntent = new Intent(context, NotifyAlarmReceiver.class);
                myIntent.setAction("NotifyAction");
                myIntent.putExtra("notifyText", notifyText);
                myIntent.putExtra("alarmId", DELAY_ALARM);
                timer.setAlarmTimer(context, startTime + cycTime, AlarmManager.RTC_WAKEUP, myIntent);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        //设置正面按钮
        builder.setPositiveButton("现在前往", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "你选择了现在前往", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        //对话框显示的监听事件
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Log.e("Dialog", "对话框显示了");
            }
        });
        //对话框消失的监听事件
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.e("Dialog", "对话框消失了");
            }
        });
        //显示对话框
        dialog.show();
    }
}
