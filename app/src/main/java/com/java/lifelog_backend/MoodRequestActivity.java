package com.java.lifelog_backend;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;

public class MoodRequestActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private int state;

    private boolean got_emotion;
    private String emotion_event;
    private int[] emotion_time;
    private String activities;
    private int[] cur_time;
    private double[] cur_emotion;
    private double[] emotion;

    private RadioButton yes;
    private RadioButton no;
    private TextView question_text;
    private EditText ans;
    private TimePicker timePicker;
    private Button button;

    private String interval;

    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_request);

        init();
    }

    private void init() {

        state = 0;

        emotion_time = new int[]{0, 0, 0, 0, 0};
        cur_time = new int[]{0, 0, 0, 0, 0};
        cur_emotion = new double[]{0, 0};
        emotion = new double[]{0, 0};

        yes = findViewById(R.id.mood_request_yes);
        no = findViewById(R.id.mood_request_no);
        question_text = findViewById(R.id.mood_request_text1);
        ans = findViewById(R.id.mood_request_textedit);
        timePicker = findViewById(R.id.mood_request_time);
        button = findViewById(R.id.mood_request_confirm);

        question_text.setVisibility(View.INVISIBLE);
        ans.setVisibility(View.INVISIBLE);
        timePicker.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);

        yes.setOnCheckedChangeListener(this);
        no.setOnCheckedChangeListener(this);
        button.setOnClickListener(this);

        // collect current time
        final Calendar calendar = Calendar.getInstance();
        cur_time[0] = calendar.get(Calendar.YEAR);
        cur_time[1] = calendar.get(Calendar.MONTH) + 1;
        cur_time[2] = calendar.get(Calendar.DAY_OF_MONTH);
        cur_time[3] = calendar.get(Calendar.HOUR_OF_DAY);
        cur_time[4] = calendar.get(Calendar.MINUTE);

        interval = getIntent().getStringExtra("interval");

        // jump to Mood to collect current mood
        Intent intent = new Intent();
        intent.setClass(MoodRequestActivity.this, MoodActivity.class);
        intent.putExtra("title", "How are you feeling now?");
        this.startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK)
                    //finish();
                    break;
            case 100:
                if (resultCode == RESULT_OK) {
                    cur_emotion = data.getExtras().getDoubleArray("mood");
                    showMusicDialog();
//                    String notice = "You've submitted successfully with mood (" + cur_emotion[0] + "," + cur_emotion[1] + ").";
//                    Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_SHORT).show();
                }
                break;
            case 200:
                if (resultCode == RESULT_OK) {
                    emotion = data.getExtras().getDoubleArray("mood");
//                    String notice = "You've submitted successfully with emotion (" + emotion[0] + "," + emotion[1] + ").";
//                    Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_SHORT).show();
                    // writeFile
                    if (writeFile()) {
                        Toast.makeText(getApplicationContext(), "Recorded successfully!", Toast.LENGTH_LONG).show();
                    }
                    // back to Main
                    // finish();
                    Intent intent = new Intent();
                    intent.setClass(MoodRequestActivity.this, ContextActivity.class);
                    this.startActivity(intent);
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mood_request_confirm:
                switch (state) {
                    case 1:
                        if (got_emotion) {

                            emotion_event = ans.getText().toString();
//                            Toast.makeText(getApplicationContext(), emotion_event, Toast.LENGTH_SHORT).show();
                            question_text.setText("Please record the moment: ");
                            ans.setVisibility(View.INVISIBLE);
                            timePicker.setVisibility(View.VISIBLE);
                            timePicker.setIs24HourView(true);
                            timePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
                        } else {
                            activities = ans.getText().toString();
                            // write file
                            if (writeFile()) {
                                Toast.makeText(getApplicationContext(), "Recorded successfully!", Toast.LENGTH_LONG).show();
                            }
                            // showMusicDialog();
                            Intent intent = new Intent();
                            intent.setClass(MoodRequestActivity.this, ContextActivity.class);
                            this.startActivity(intent);
                            finish();
                        }
                        break;
                    case 2:
                        if (got_emotion) {
                            final Calendar calendar = Calendar.getInstance();
                            emotion_time[0] = calendar.get(Calendar.YEAR);
                            emotion_time[1] = calendar.get(Calendar.MONTH) + 1;
                            emotion_time[2] = calendar.get(Calendar.DAY_OF_MONTH);
                            emotion_time[3] = timePicker.getHour();
                            emotion_time[4] = timePicker.getMinute();
//                            Toast.makeText(getApplicationContext(), emotion_time, Toast.LENGTH_SHORT).show();
                            // jump to Mood to collect emotion
                            Intent intent = new Intent();
                            intent.setClass(MoodRequestActivity.this, MoodActivity.class);
                            intent.putExtra("title", "How are you feeling then?");
                            String curEmotionString = String.format("%.3f", cur_emotion[0]) + "," + String.format("%.3f", cur_emotion[1]);
                            intent.putExtra("pmood", curEmotionString);
                            this.startActivityForResult(intent, 200);
                        }
                        break;
                }
                state += 1;
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        state = 1;
        button.setVisibility(View.VISIBLE);
        button.setText("Next");

        // clear all data collected
        emotion_event = "";
        activities = "";

        ans.setText("");
        question_text.setVisibility(View.VISIBLE);
        ans.setVisibility(View.VISIBLE);
        switch (buttonView.getId()) {
            case R.id.mood_request_yes:
                if (yes.isChecked()) {
//                    Toast.makeText(getApplicationContext(), "请简要描述激发您强烈情绪的事件：", Toast.LENGTH_SHORT).show();
                    got_emotion = true;
                    question_text.setText("Please briefly describe the events that triggered your strong emotions: ");
                }
                break;
            case R.id.mood_request_no:
                if (no.isChecked()) {
//                    Toast.makeText(getApplicationContext(), "请简要描述您过去3小时内进行的活动：", Toast.LENGTH_SHORT).show();
                    got_emotion = false;
                    String text;
                    if (interval != null) {
                        text = "Please briefly describe your activities in last " + interval + " minutes:\n(e.g. study/work, meet/chat, entertainment, sports, rest, meal, sleep, others)";
                    } else {
                        text = "Please briefly describe your activities:\n(e.g. study/work, meet/chat, entertainment, sports, rest, meal, sleep, others))";
                    }
                    question_text.setText(text);
                    button.setText("Finish");
                }
                break;
        }
    }

    private boolean writeFile() {
        String path = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/emotion";
        File file = new File(path + "/user_submitted_emotions.txt");
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
            StringBuilder sb = new StringBuilder();
            // write routine emotion first
            Log.e("Record", cur_time.toString());
            String curTimeString = cur_time[0] + ":" + cur_time[1] + ":" + cur_time[2] + ":" + cur_time[3] + ":" + cur_time[4];
            sb.append(curTimeString).append("\t");
            if (!got_emotion) {
                sb.append("act" + activities).append("\t");
            }
            String curEmotionString = "(" + String.format("%.2f", cur_emotion[0]) + "," + String.format("%.2f", cur_emotion[1]) + ")";
            sb.append(curEmotionString);
            out.write(sb.toString());
            // write emotion
            if (got_emotion) {
                out.newLine();
                StringBuilder sb1 = new StringBuilder();
                Log.e("Record", emotion_time.toString() + "--" + emotion.toString() + "--" + emotion_event);
                String timeString = emotion_time[0] + ":" + emotion_time[1] + ":" + emotion_time[2] + ":" + emotion_time[3] + ":" + emotion_time[4];
                sb1.append(timeString).append("\t");
                sb1.append(emotion_event.replace('\n', ' ')).append("\t");
                String emotionString = "(" + String.format("%.2f", emotion[0]) + "," + String.format("%.2f", emotion[1]) + ")";
                Log.e("Record", emotionString);
                sb1.append(emotionString).append("\te");
                out.write(sb1.toString());
            }
            return true;
        } catch (Exception e) {
            Log.e("Record", e.toString());
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Recorded error!", Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void showMusicDialog() {
        builder = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("Music")
                .setMessage("Would you like to listen to some music").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(MoodRequestActivity.this, "听音乐", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.setClass(MoodRequestActivity.this, MusicActivity.class);
                        intent.putExtra("title", "notMood");
                        String curEmotionString = String.format("%.3f", cur_emotion[0]) + "," + String.format("%.3f", cur_emotion[1]);
                        Log.d("music_mood_a", curEmotionString);
                        Log.d("music_mood_b", String.format("%.3f", emotion[0]));
                        intent.putExtra("pmood", curEmotionString);
                        startActivityForResult(intent, 10);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(MoodRequestActivity.this, "不听音乐", Toast.LENGTH_LONG).show();
                        //finish();
                    }
                });
        builder.create().show();
    }
}

