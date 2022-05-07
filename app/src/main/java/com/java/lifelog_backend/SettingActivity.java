package com.java.lifelog_backend;


import android.os.Bundle;
import android.os.Environment;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.view.View;
import android.content.Intent;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText user_editText, bluetooth_editText, interval_editText;
    private int[] tagging_time, breakfast_time, lunch_time, dinner_time;
    private int[][] time = new int[10][2];
    private int alarm_cnt = 0;
    private TextView start_Textview, end_Textview, tagging_Textview, breakfast_Textview, lunch_Textview, dinner_Textview, interval_Textview;
    private TextView[] time_Textview = new TextView[10];
    String TAG="SettingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setall);
        // findViewById(R.id.set_start_time).setOnClickListener(this);
        // findViewById(R.id.set_end_time).setOnClickListener(this);
        findViewById(R.id.set_tagging_time).setOnClickListener(this);
        findViewById(R.id.set_breakfast_time).setOnClickListener(this);
        findViewById(R.id.add_time).setOnClickListener(this);
        findViewById(R.id.remove_time).setOnClickListener(this);
        findViewById(R.id.set_time1).setOnClickListener(this);
        findViewById(R.id.set_time2).setOnClickListener(this);
        findViewById(R.id.set_time3).setOnClickListener(this);
        findViewById(R.id.set_time4).setOnClickListener(this);
        findViewById(R.id.set_time5).setOnClickListener(this);
        findViewById(R.id.set_time6).setOnClickListener(this);
        findViewById(R.id.set_time7).setOnClickListener(this);
        findViewById(R.id.set_time8).setOnClickListener(this);
        findViewById(R.id.set_time9).setOnClickListener(this);
        findViewById(R.id.set_time10).setOnClickListener(this);
        findViewById(R.id.set_lunch_time).setOnClickListener(this);
        findViewById(R.id.set_dinner_time).setOnClickListener(this);
        findViewById(R.id.upload_interval).setOnClickListener(this);
        findViewById(R.id.save_setting).setOnClickListener(this);
        init();
        for(int i = alarm_cnt; i < 10; i++){
            int id = getResources().getIdentifier("TIME"+(i+1),"id",getPackageName());
            findViewById(id).setVisibility(View.GONE);
        }
    }

    private void init() {
        user_editText = findViewById(R.id.user_id);
        bluetooth_editText = findViewById(R.id.bluetooth_id);
        // interval_editText = findViewById(R.id.interval_time);

        // start_Textview = findViewById(R.id.TV_start_time);
        // end_Textview = findViewById(R.id.TV_end_time);
        tagging_Textview = findViewById(R.id.TV_tagging_time);
        breakfast_Textview = findViewById(R.id.TV_breakfast_time);
        time_Textview[0] = findViewById(R.id.TV_time1);
        time_Textview[1] = findViewById(R.id.TV_time2);
        time_Textview[2] = findViewById(R.id.TV_time3);
        time_Textview[3] = findViewById(R.id.TV_time4);
        time_Textview[4] = findViewById(R.id.TV_time5);
        time_Textview[5] = findViewById(R.id.TV_time6);
        time_Textview[6] = findViewById(R.id.TV_time7);
        time_Textview[7] = findViewById(R.id.TV_time8);
        time_Textview[8] = findViewById(R.id.TV_time9);
        time_Textview[9] = findViewById(R.id.TV_time10);
        lunch_Textview = findViewById(R.id.TV_lunch_time);
        dinner_Textview = findViewById(R.id.TV_dinner_time);
        interval_Textview = findViewById(R.id.upload_interval);

        user_editText.setHint("Please set user index");
        bluetooth_editText.setHint("Please set bracelet index");
        interval_Textview.setHint("10");

        String Setting_Path = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/setting/";
        File openFile = new File(Setting_Path + "setting.json");

        if (openFile.exists()) {
            Log.i("is_here", "is_here_exists");
            getJson(Setting_Path + "setting.json");
        }
        //getJson(Setting_Path + "setting2.txt");

    }

    private void getJson(String fileName) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(new FileReader(fileName));
            user_editText.setText(object.get("user_id").getAsString());
            bluetooth_editText.setText(object.get("bluetooth_id").getAsString());
            // start_Textview.setText(object.get("start_time_setting").getAsString());
            // end_Textview.setText(object.get("end_time_setting").getAsString());
            tagging_Textview.setText(object.get("tagging_time_setting").getAsString());
            // interval_editText.setText(object.get("interval").getAsString());
            breakfast_Textview.setText(object.get("breakfast_time_setting").getAsString());
            lunch_Textview.setText(object.get("lunch_time_setting").getAsString());
            dinner_Textview.setText(object.get("dinner_time_setting").getAsString());
            for(int i = 0; i < 10; i++)
            {
                String s = "alarm_time"+(i);
                time_Textview[i].setText(object.get(s).getAsString());
            }
            alarm_cnt = object.get("alarm_cnt").getAsInt();
            String reminderClocks = object.get("reminder_clocks").getAsString();
            interval_Textview.setText(getAutoInterval());
            /*
            for (String clockId : reminderClocks.split(",")) {
                int resID = getResources().getIdentifier("moment_" + clockId, "id", getPackageName());
                CheckBox cb = (CheckBox) findViewById(resID);
                cb.setChecked(true);
            }*/

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("is_here", "is_error");

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_setting:
                String clocks = write();
                //启动notification的service
                //startNotification(clocks);
                //返回Main界面
                Intent intent = new Intent();
                intent.setClass(SettingActivity.this, MainActivity.class);
                this.startActivity(intent);
//                Intent intent = new Intent();
//                intent.setClass(SettingActivity.this, MainActivity.class);
//                intent.putExtra("interval", interval_editText.getText().toString());
//                setResult(RESULT_OK, intent);
//                finish();
                break;
            case R.id.add_time:
                if(alarm_cnt <= 9)
                {
                    alarm_cnt++;
                    int id = getResources().getIdentifier("TIME"+(alarm_cnt),"id",getPackageName());
                    findViewById(id).setVisibility(View.VISIBLE);
                }
                break;
            case R.id.remove_time:
                if(alarm_cnt > 0)
                {
                    int id = getResources().getIdentifier("TIME"+(alarm_cnt),"id",getPackageName());
                    findViewById(id).setVisibility(View.GONE);
                    alarm_cnt--;
                }
                break;
            case R.id.set_tagging_time:
                Intent intent3 = new Intent();
                intent3.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent3, 103);
                break;
            case R.id.set_breakfast_time:
                Intent intent4 = new Intent();
                intent4.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent4, 104);
                break;
            case R.id.set_lunch_time:
                Intent intent5 = new Intent();
                intent5.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent5, 105);
                break;
            case R.id.set_dinner_time:
                Intent intent6 = new Intent();
                intent6.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent6, 106);
                break;
            case R.id.set_time1:
                Intent intent11 = new Intent();
                intent11.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent11, 111);
                break;
            case R.id.set_time2:
                Intent intent12 = new Intent();
                intent12.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent12, 112);
                break;
            case R.id.set_time3:
                Intent intent13 = new Intent();
                intent13.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent13, 113);
                break;
            case R.id.set_time4:
                Intent intent14 = new Intent();
                intent14.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent14, 114);
                break;
            case R.id.set_time5:
                Intent intent15 = new Intent();
                intent15.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent15, 115);
                break;
            case R.id.set_time6:
                Intent intent16 = new Intent();
                intent16.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent16, 116);
                break;
            case R.id.set_time7:
                Intent intent17 = new Intent();
                intent17.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent17, 117);
                break;
            case R.id.set_time8:
                Intent intent18 = new Intent();
                intent18.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent18, 118);
                break;
            case R.id.set_time9:
                Intent intent19 = new Intent();
                intent19.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent19, 119);
                break;
            case R.id.set_time10:
                Intent intent110 = new Intent();
                intent110.setClass(SettingActivity.this, RecordSettingTimeActivity.class);
                startActivityForResult(intent110, 120);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case 103:
                if (resultCode == RESULT_OK) {
                    tagging_time = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(tagging_time[0]);
                    sb.append(":");
                    sb.append(tagging_time[1]);
                    tagging_Textview.setText(sb.toString());
                }
                break;
            case 104:
                if (resultCode == RESULT_OK) {
                    breakfast_time = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(breakfast_time[0]);
                    sb.append(":");
                    sb.append(breakfast_time[1]);
                    breakfast_Textview.setText(sb.toString());
                }
                break;
            case 105:
                if (resultCode == RESULT_OK) {
                    lunch_time = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(lunch_time[0]);
                    sb.append(":");
                    sb.append(lunch_time[1]);
                    lunch_Textview.setText(sb.toString());
                }
                break;
            case 106:
                if (resultCode == RESULT_OK) {
                    dinner_time = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(dinner_time[0]);
                    sb.append(":");
                    sb.append(dinner_time[1]);
                    dinner_Textview.setText(sb.toString());
                }
                break;
            case 111:
                if (resultCode == RESULT_OK) {
                    time[0] = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%02d", time[0][0]));
                    sb.append(":");
                    sb.append(String.format("%02d", time[0][1]));
                    time_Textview[0].setText(sb.toString());
                }
                break;
            case 112:
                if (resultCode == RESULT_OK) {
                    time[1] = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%02d", time[1][0]));
                    sb.append(":");
                    sb.append(String.format("%02d", time[1][1]));
                    time_Textview[1].setText(sb.toString());
                }
                break;
            case 113:
                if (resultCode == RESULT_OK) {
                    time[2] = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%02d", time[2][0]));
                    sb.append(":");
                    sb.append(String.format("%02d", time[2][1]));
                    time_Textview[2].setText(sb.toString());
                }
                break;
            case 114:
                if (resultCode == RESULT_OK) {
                    time[3] = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%02d", time[3][0]));
                    sb.append(":");
                    sb.append(String.format("%02d", time[3][1]));
                    time_Textview[3].setText(sb.toString());
                }
                break;
            case 115:
                if (resultCode == RESULT_OK) {
                    time[4] = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%02d", time[4][0]));
                    sb.append(":");
                    sb.append(String.format("%02d", time[4][1]));
                    time_Textview[4].setText(sb.toString());
                }
                break;
            case 116:
                if (resultCode == RESULT_OK) {
                    time[5] = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%02d", time[5][0]));
                    sb.append(":");
                    sb.append(String.format("%02d", time[5][1]));
                    time_Textview[5].setText(sb.toString());
                }
                break;
            case 117:
                if (resultCode == RESULT_OK) {
                    time[6] = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%02d", time[6][0]));
                    sb.append(":");
                    sb.append(String.format("%02d", time[6][1]));
                    time_Textview[6].setText(sb.toString());
                }
                break;
            case 118:
                if (resultCode == RESULT_OK) {
                    time[7] = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%02d", time[7][0]));
                    sb.append(":");
                    sb.append(String.format("%02d", time[7][1]));
                    time_Textview[7].setText(sb.toString());
                }
                break;
            case 119:
                if (resultCode == RESULT_OK) {
                    time[8] = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%02d", time[8][0]));
                    sb.append(":");
                    sb.append(String.format("%02d", time[8][1]));
                    time_Textview[8].setText(sb.toString());
                }
                break;
            case 120:
                if (resultCode == RESULT_OK) {
                    time[9] = data.getExtras().getIntArray("time");
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%02d", time[9][0]));
                    sb.append(":");
                    sb.append(String.format("%02d", time[9][1]));
                    time_Textview[9].setText(sb.toString());
                }
                break;
        }
    }

    private String write() {
        String user_id = user_editText.getText().toString();
        String bluetooth_id = bluetooth_editText.getText().toString();
        String tagging_time_setting = tagging_Textview.getText().toString();
        String breakfast_time_setting = breakfast_Textview.getText().toString();
        String lunch_time_setting = lunch_Textview.getText().toString();
        String dinner_time_setting = dinner_Textview.getText().toString();
        String reminderClocks = "";
        /*
        for (int i = 0; i < 14; i++) {
            int resID = getResources().getIdentifier("moment_" + Integer.toString(i), "id", getPackageName());
            CheckBox cb = (CheckBox) findViewById(resID);
            if (cb.isChecked()) {
                reminderClocks += "," + Integer.toString(i);
            }
        }*/
        //System.out.println("time[0]_1:"+time[0].length);
        for(int i = 0; i < 10; i++)
        {
            if (time[i].length > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%02d", time[i][0]));
                sb.append(":");
                sb.append(String.format("%02d", time[i][1]));
                reminderClocks += "," + sb;
            }
        }
        if (reminderClocks.length() > 0) { //去除掉最开始的逗号
            reminderClocks = reminderClocks.substring(1);
        }
        System.out.println("clock:"+reminderClocks);
        //save
        String Setting_Path = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/setting/";
        //File setting_file = new File(Setting_Path + "setting.txt");

/*
        BufferedWriter  out = null;
        try {
            if (!setting_file.getParentFile().exists()) {
                setting_file.getParentFile().mkdirs();
            }
            if (!setting_file.exists()) {
                setting_file.createNewFile();
            }
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(setting_file, true)));
            out.newLine();
            out.write("user_id "+user_id);
            out.newLine();
            out.write("bluetooth_id "+bluetooth_id);
            out.newLine();
            out.write("start_time "+start_time_setting);
            out.newLine();
            out.write("end_time "+end_time_setting);
            out.newLine();
            out.write("interval "+interval);
            out.newLine();
            out.write("trace "+tagging_time_setting);
            out.newLine();
            out.write("breakfast_time"+breakfast_time_setting);
            out.newLine();
            out.write("lunch_time"+lunch_time_setting);
            out.newLine();
            out.write("dinner_time"+dinner_time_setting);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
*/
        //JSON
        JsonObject object = new JsonObject();
        object.addProperty("user_id", user_id);
        object.addProperty("bluetooth_id", bluetooth_id);
        object.addProperty("tagging_time_setting", tagging_time_setting);
        object.addProperty("breakfast_time_setting", breakfast_time_setting);
        object.addProperty("lunch_time_setting", lunch_time_setting);
        object.addProperty("dinner_time_setting", dinner_time_setting);
        object.addProperty("reminder_clocks", reminderClocks);
        for(int i = 0; i < 10; i++)
        {
            String s = Integer.toString(i);
            object.addProperty("reminder_clocks"+s,
                    time_Textview[i].getText().toString());
        }
        object.addProperty("alarm_cnt", alarm_cnt);
        File setting_file2 = new File(Setting_Path + "setting.json");

        BufferedWriter out2 = null;
        try {
            if (!setting_file2.getParentFile().exists()) {
                setting_file2.getParentFile().mkdirs();
            }
            if (setting_file2.exists()) {
                setting_file2.delete();
            }
            setting_file2.createNewFile();
            out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(setting_file2, true)));
            out2.write(object.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out2 != null) {
                    out2.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // 单独存储upload interval
        writeAutoInterval(interval_Textview.getText().toString());
        return (reminderClocks);
    }

    private void writeAutoInterval(String interval){
        String path = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend";
        File file = new File(path + "/auto_upload_interval.txt");
        BufferedWriter out = null;
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
            Log.e("Record auto setting interval",interval );
            out.write(interval);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Record", e.toString());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void startNotification(String clocks) {
        String tagging_time_setting = tagging_Textview.getText().toString();
        String breakfast_time_setting = breakfast_Textview.getText().toString();
        String lunch_time_setting = lunch_Textview.getText().toString();
        String dinner_time_setting = dinner_Textview.getText().toString();

        Intent i = new Intent(this, NotifyService.class);
        i.putExtra("trace_time", tagging_time_setting);
        i.putExtra("breakfa" +
                "st", breakfast_time_setting);
        i.putExtra("lunch", lunch_time_setting);
        i.putExtra("dinner", dinner_time_setting);
        i.putExtra("clocks", clocks);
        startService(i);
    }

    private String getAutoInterval(){
        String path = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend";
        File file = new File(path + "/auto_upload_interval.txt");
        try{
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                Log.i(TAG,"Auto interval file not exist");
                return "10";
            }
            else{
                InputStream instream = new FileInputStream(file);
                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line= buffreader.readLine();
                    instream.close();
                    Log.i(TAG,"Auto interval: "+line);
                    return line;
                }
                else{
                    Log.i(TAG,"Auto interval file read error");
                    return "10";
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, e.toString());
            return "10";
        }
    }

}
