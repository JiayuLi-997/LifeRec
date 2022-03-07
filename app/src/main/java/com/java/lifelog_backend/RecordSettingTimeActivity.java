package com.java.lifelog_backend;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.TimePicker;
import android.os.Bundle;

import androidx.annotation.Nullable;


import android.widget.TimePicker.OnTimeChangedListener;


public class RecordSettingTimeActivity extends Activity {
    private TimePicker my_time_picker;
    private Button done;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settime);

        init();
    }

    private void init() {
        my_time_picker = findViewById(R.id.pick_time);
        my_time_picker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
        my_time_picker.setIs24HourView(true);
        my_time_picker.setHour(0);
        my_time_picker.setMinute(0);

        my_time_picker.setOnTimeChangedListener(new OnTimePickerChange());

        done = findViewById(R.id.btn_save_time);
        ButtonListener buttonListener = new ButtonListener();
        done.setOnClickListener(buttonListener);
    }

    class OnTimePickerChange implements OnTimeChangedListener {
        @Override
        public void onTimeChanged(TimePicker view, int hour, int minute) {
            Log.i("onononon", "时=" + hour + "--分=" + minute);
        }
    }

    class ButtonListener implements android.view.View.OnClickListener {
        @Override
        public void onClick(View v) {
            int[] time = new int[2];
            time[0] = my_time_picker.getHour();
            time[1] = my_time_picker.getMinute();
            Intent intent = new Intent();
            intent.putExtra("time", time);
            setResult(RESULT_OK, intent);
            finish();
        }
    }


}
