package com.java.lifelog_backend;

import android.app.AlertDialog;
import android.app.Dialog;
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

/**
 * Notice that this activity is similar to MoodRequest.
 * The only difference is that this activity only triggered by user to record emotion.
 * That's to say, NOT by the timer :D
 */
public class MoodSubmitActivity extends AppCompatActivity implements View.OnClickListener {

    private int state;

    private String emotion_event;
    private int[] emotion_time;
    private double[] emotion;

    private TextView question_text;
    private EditText ans;
    private TimePicker timePicker;
    private Button button;

    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_submit);

        init();
    }

    private void init() {

        state = 0;

        emotion = new double[]{0, 0};
        emotion_time = new int[]{0, 0, 0, 0, 0}; // yy:mm:dd:hh::mm

        question_text = findViewById(R.id.mood_submit_text1);
        ans = findViewById(R.id.mood_submit_textedit);
        timePicker = findViewById(R.id.mood_submit_time);
        button = findViewById(R.id.mood_submit_confirm);

        question_text.setVisibility(View.INVISIBLE);
        ans.setVisibility(View.INVISIBLE);
        timePicker.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);

        button.setOnClickListener(this);

        // set all components
        state = 1;

        button.setVisibility(View.VISIBLE);
        button.setText("Next");

        // clear all data collected
        emotion_event = "";

        ans.setText("");
        question_text.setVisibility(View.VISIBLE);
        ans.setVisibility(View.VISIBLE);
        question_text.setText("Please briefly describe the events that triggered your strong emotions: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            return;
        }
        switch (requestCode) {
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
                    intent.setClass(MoodSubmitActivity.this, ContextActivity.class);
                    this.startActivity(intent);
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mood_submit_confirm:
                switch (state) {
                    case 1:
                        emotion_event = ans.getText().toString();
//                            Toast.makeText(getApplicationContext(), emotion_event, Toast.LENGTH_SHORT).show();
                        question_text.setText("Please record the moment: ");
                        ans.setVisibility(View.INVISIBLE);
                        timePicker.setVisibility(View.VISIBLE);
                        timePicker.setIs24HourView(true);
                        timePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
                        break;
                    case 2:
                        // collect time
                        final Calendar calendar = Calendar.getInstance();
                        emotion_time[0] = calendar.get(Calendar.YEAR);
                        emotion_time[1] = calendar.get(Calendar.MONTH) + 1;
                        emotion_time[2] = calendar.get(Calendar.DAY_OF_MONTH);
                        emotion_time[3] = timePicker.getHour();
                        emotion_time[4] = timePicker.getMinute();
                        // jump to Mood to collect emotion
                        Intent intent = new Intent();
                        intent.setClass(MoodSubmitActivity.this, MoodActivity.class);
                        intent.putExtra("title", "How are you feeling then??");
                        this.startActivityForResult(intent, 200);
                        break;
                }
                state += 1;
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
            String timeString = emotion_time[0] + ":" + emotion_time[1] + ":" + emotion_time[2] + ":" + emotion_time[3] + ":" + emotion_time[4];
            sb.append(timeString).append("\t");
            sb.append(emotion_event.replace('\n', ' ')).append("\t");
            String emotionString = "(" + String.format("%.2f", emotion[0]) + "," + String.format("%.2f", emotion[1]) + ")";
            Log.e("Record", emotionString);
            sb.append(emotionString).append("\te");
            out.write(sb.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Record", e.toString());
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

}

