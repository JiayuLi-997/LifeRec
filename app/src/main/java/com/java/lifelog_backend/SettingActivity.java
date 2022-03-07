package com.java.lifelog_backend;


import android.os.Bundle;
import android.os.Environment;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.view.View;
import android.content.Intent;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText user_editText, bluetooth_editText, interval_editText;
    private int[] tagging_time, breakfast_time, lunch_time, dinner_time;
    private TextView start_Textview, end_Textview, tagging_Textview, breakfast_Textview, lunch_Textview, dinner_Textview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setall);

        // findViewById(R.id.set_start_time).setOnClickListener(this);
        // findViewById(R.id.set_end_time).setOnClickListener(this);
        findViewById(R.id.set_tagging_time).setOnClickListener(this);
        findViewById(R.id.set_breakfast_time).setOnClickListener(this);
        findViewById(R.id.set_lunch_time).setOnClickListener(this);
        findViewById(R.id.set_dinner_time).setOnClickListener(this);

        findViewById(R.id.save_setting).setOnClickListener(this);

        init();
    }

    private void init() {
        user_editText = findViewById(R.id.user_id);
        bluetooth_editText = findViewById(R.id.bluetooth_id);
        // interval_editText = findViewById(R.id.interval_time);

        // start_Textview = findViewById(R.id.TV_start_time);
        // end_Textview = findViewById(R.id.TV_end_time);
        tagging_Textview = findViewById(R.id.TV_tagging_time);
        breakfast_Textview = findViewById(R.id.TV_breakfast_time);
        lunch_Textview = findViewById(R.id.TV_lunch_time);
        dinner_Textview = findViewById(R.id.TV_dinner_time);

        user_editText.setHint("Please set user index");
        bluetooth_editText.setHint("Please set bracelet index");

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

            String reminderClocks = object.get("reminder_clocks").getAsString();

            for (String clockId : reminderClocks.split(",")) {
                int resID = getResources().getIdentifier("moment_" + clockId, "id", getPackageName());
                CheckBox cb = (CheckBox) findViewById(resID);
                cb.setChecked(true);
            }

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
                startNotification(clocks);
                //返回Main界面
                Intent intent = new Intent();
                intent.setClass(SettingActivity.this, MainActivity.class);
                intent.putExtra("interval", interval_editText.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
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
        }
    }

    private String write() {
        String user_id = user_editText.getText().toString();
        Log.i("user_id", user_id);
        String bluetooth_id = bluetooth_editText.getText().toString();

        String tagging_time_setting = tagging_Textview.getText().toString();
        String breakfast_time_setting = breakfast_Textview.getText().toString();
        String lunch_time_setting = lunch_Textview.getText().toString();
        String dinner_time_setting = dinner_Textview.getText().toString();

        String reminderClocks = "";
        for (int i = 0; i < 14; i++) {
            int resID = getResources().getIdentifier("moment_" + Integer.toString(i), "id", getPackageName());
            CheckBox cb = (CheckBox) findViewById(resID);
            if (cb.isChecked()) {
                reminderClocks += "," + Integer.toString(i);
            }
        }
        if (reminderClocks.length() > 0) {
            reminderClocks = reminderClocks.substring(1);
        }

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
        return (reminderClocks);
    }

    private void startNotification(String clocks) {
        String tagging_time_setting = tagging_Textview.getText().toString();
        String breakfast_time_setting = breakfast_Textview.getText().toString();
        String lunch_time_setting = lunch_Textview.getText().toString();
        String dinner_time_setting = dinner_Textview.getText().toString();

        Intent i = new Intent(this, NotifyService.class);
        i.putExtra("trace_time", tagging_time_setting);
        i.putExtra("breakfast", breakfast_time_setting);
        i.putExtra("lunch", lunch_time_setting);
        i.putExtra("dinner", dinner_time_setting);
        i.putExtra("clocks", clocks);
        startService(i);

    }

}
