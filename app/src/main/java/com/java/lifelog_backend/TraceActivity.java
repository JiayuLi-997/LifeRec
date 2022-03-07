package com.java.lifelog_backend;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java.lifelog_backend.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;

public class TraceActivity extends AppCompatActivity {

    private ListView lvTrace;
    private List<Trace> traceList = new ArrayList<>(10);
    private TraceListAdapter adapter;
    public RecordDialog recordDialog;
    private Button add_item_button;
    private Button save_button;
    private double[] mood = {0, 0};
    private int pos;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_save_pop:

                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    //String time = String.format("%d年%d月%d日 %d时%d分", year, month, day, recordDialog.tp_time.getCurrentHour(), recordDialog.tp_time.getCurrentMinute());
                    String time = String.format("%d:%d", recordDialog.tp_time.getCurrentHour(), recordDialog.tp_time.getCurrentMinute());
                    adapter.getItem(pos).setTime(time);
                    try {
                        adapter.getItem(pos).setEvent(recordDialog.text_event.getText().toString().trim());
                        adapter.getItem(pos).setMood(mood);
                    } catch (Exception e) {
                        Log.e("Music_error", e.toString());
                        e.printStackTrace();
                    }
                    adapter.updatelist();
                    recordDialog.tp_time.setHour(0);
                    recordDialog.tp_time.setMinute(0);
                    recordDialog.text_event.setText("");
                    recordDialog.text_mood.setText("");

                    recordDialog.dismiss();
                    break;

                case R.id.btn_mood_set:

                    Intent intent = new Intent();
                    intent.setClass(TraceActivity.this, MoodActivity.class);
                    startActivityForResult(intent, 300);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case 300:
                if (resultCode == RESULT_OK) {
                    mood = data.getExtras().getDoubleArray("mood");
                    StringBuilder sb = new StringBuilder();
                    sb.append("Your mood: \n");
                    sb.append(String.format("%.2f", mood[0]));
                    sb.append(" , ");
                    sb.append(String.format("%.2f", mood[1]));
                    recordDialog.text_mood.setText(sb.toString());
//                    String notice = "You've submitted successfully with emotion (" + mood[0] + "," + mood[1] + ").";
//                    Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(getApplicationContext(), "Recorded successfully!", Toast.LENGTH_LONG).show();
//                     back to Main
//                    Intent intent = new Intent();
//                    intent.setClass(TraceActivity.this, MainActivity.class);
//                    this.startActivity(intent);
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace);
        findView();
        initData();
    }

    private void findView() {
        lvTrace = (ListView) findViewById(R.id.lvTrace);
        add_item_button = (Button) findViewById(R.id.btn_add_item_pop);
        save_button = (Button) findViewById(R.id.btn_save);
    }

    private void initData() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // 如果当天已经填过，保留当天的数据
        String tracePath = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/tracer";
        File file = new File(tracePath + "/traceInfo.txt");
        int existFile = 0;
        if (file.exists()) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Scanner sc = new Scanner(fileReader);
            String line = null;
            while ((sc.hasNextLine() && (line = sc.nextLine()) != null)) {
                if (!sc.hasNextLine()) {
                    break;
                }
            }
            sc.close();
            if (line != null) {
                Log.d("load_act", line);
                try {
                    JsonParser parser = new JsonParser();
                    JsonObject actTrace = (JsonObject) parser.parse(line);

//                    JSONObject actTrace = new JSONObject(line);
                    String today = String.format("%d,%d,%d", year, month, day);
                    String date = actTrace.get("timeInfo").toString();
                    if (date.indexOf(today) != -1) {
                        Log.d("load_act", actTrace.get("traceList").toString());
                        String actString = actTrace.get("traceList").toString();
                        Gson gson = new Gson();
                        JsonArray actArray = new JsonParser().parse(actString).getAsJsonArray();
                        for (JsonElement jsonElement : actArray) {
                            JsonObject obj = jsonElement.getAsJsonObject();
                            String time = obj.get("time").getAsString();
                            String event = obj.get("event").getAsString();
                            double[] mood = new double[2];
                            mood[0] = obj.get("moodx").getAsDouble();
                            mood[1] = obj.get("moody").getAsDouble();
                            traceList.add(new Trace(time, event, mood));
                        }
//                        Log.d("load_act",actList.toString());
//                        for(Trace t : (ArrayList<Trace>)actTrace.get("traceList")){
//                            Log.d("load_act",t.toString());
//                        }
                        existFile = 1;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }


        if (existFile == 0) {
            String segmentTime = String.format("%04d-%02d-%02d", year, month, day);
            //ArrayList<String> timeList = segmentData("2021-04-18");
            ArrayList<String> timeList = segmentData(segmentTime);
            // 模拟一些假的数据
            for (int i = 0; i < timeList.size(); i++) {
                double[] mood = new double[2];
                mood[0] = 0;
                mood[1] = 0;
                traceList.add(new Trace(timeList.get(i), "", mood));
            }
//        traceList.add(new Trace("2021年4月20日 8时48分", "吃了很棒的早餐","happy"));
//        traceList.add(new Trace("2021年4月20日 12时13分", "午饭排队的人很多","sad"));
//        traceList.add(new Trace("2021年4月20日 17时40分", "一天的课程结束了","happy"));
//        traceList.add(new Trace("2021年4月20日 20时19分", "组会结束了","happy"));
//        traceList.add(new Trace("2021年4月20日 23时12分", "看了最新的《窥探》","happy"));
        }

        adapter = new TraceListAdapter(this, traceList);
        lvTrace.setAdapter(adapter);
        recordDialog = new RecordDialog(this, onClickListener);
        lvTrace.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //弹窗
                pos = position;
                String[] arr;
                if (!traceList.get(pos).getTime().equals("time")) {
                    arr = traceList.get(pos).getTime().split(":");
                } else {
                    arr = new String[2];
                    arr[0] = "0";
                    arr[1] = "0";
                }
                recordDialog.tp_time.setHour(Integer.parseInt(arr[0]));
                recordDialog.tp_time.setMinute(Integer.parseInt(arr[1]));
                recordDialog.text_event.setText(traceList.get(pos).getEvent());
                StringBuilder sb = new StringBuilder();
                sb.append("Your mood: \n");
                sb.append(String.format("%.2f", traceList.get(pos).getMood()[0]));
                sb.append(" , ");
                sb.append(String.format("%.2f", traceList.get(pos).getMood()[1]));
                recordDialog.text_mood.setText(sb);
                recordDialog.showAtLocation(findViewById(R.id.activity_trace), Gravity.CENTER, 0, 0);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

            }
        });
        add_item_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("add a item", "???");
                double[] mood = new double[2];
                mood[0] = 0;
                mood[1] = 0;
                adapter.addItemFirst("time", "activity", mood);
            }
        });
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                traceList=adapter.traceList;
                Log.v("haha", "save complete");
                JSONObject root = new JSONObject();
                Calendar calendars = Calendar.getInstance();
                calendars.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                String timeInfo = String.valueOf(calendars.get(Calendar.YEAR) + "," +
                        (calendars.get(Calendar.MONTH) + 1) + "," +
                        calendars.get(Calendar.DATE) + "," + calendars.get(Calendar.HOUR_OF_DAY));

                try {
                    root.put("timeInfo", timeInfo);
                    JSONArray tracelist = new JSONArray();
                    for (int i = 0; i < traceList.size(); i++) {
                        JSONObject trace1 = new JSONObject();
                        trace1.put("time", traceList.get(i).getTime());
                        trace1.put("event", traceList.get(i).getEvent());
                        trace1.put("moodx", traceList.get(i).getMood()[0]);
                        trace1.put("moody", traceList.get(i).getMood()[1]);
                        tracelist.put(i, trace1);
                    }
                    root.put("traceList", tracelist);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String tracePath = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/tracer";
                File file = new File(tracePath + "/traceInfo.txt");
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
                    Log.v("haha", root.toString());
                    out.write(root.toString());
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
                Intent intent = new Intent();
                intent.setClass(TraceActivity.this, MainActivity.class);
                TraceActivity.this.startActivity(intent);

            }
        });
    }

    private ArrayList<String> segmentData(String date) {
        ArrayList<String> timeList = new ArrayList<String>();

        String dbFile = "/storage/emulated/0/gadgetbridge/gadgetbridge";///data/nodomain.freeyourgadget.gadgetbridge/files/Gadgetbridge";
        File file = new File(dbFile);
        if (file.exists() && file.length() > 0) {
            try {
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
                Cursor cursor = db.rawQuery("SELECT STEPS,TIMESTAMP from MI_BAND_ACTIVITY_SAMPLE", null);
                if (cursor.getCount() > 0) {
                    ArrayList<Integer> steps = new ArrayList<>();
                    ArrayList<Integer> timeStamps = new ArrayList<>();
                    int compare = 0;
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        int step = cursor.getInt(cursor.getColumnIndex("STEPS"));
                        int timeStamp = cursor.getInt(cursor.getColumnIndex("TIMESTAMP"));
                        compare = ts2Date(timeStamp).compareTo(date);
                        if (compare > 0) break;
                        else if (compare == 0) {
                            steps.add(step);
                            timeStamps.add(timeStamp);
                        }
                    }
                    if (timeStamps.size() > 0) {
                        timeList = segmentWithStep(steps, timeStamps, 20, 3);
                    }
                }
                cursor.close();
                db.close();
            } catch (Exception e) {
                Log.e("segment_error", e.toString());
                e.printStackTrace();
            }
        }
        if (timeList.size() == 0) {
            timeList.add("00:00");
            timeList.add("08:00");
            timeList.add("10:00");
            timeList.add("12:00");
            timeList.add("14:00");
            timeList.add("16:00");
            timeList.add("18:00");
            timeList.add("20:00");
            timeList.add("22:00");
        }
        return timeList;
    }

    private ArrayList<String> segmentWithStep(ArrayList<Integer> steps, ArrayList<Integer> timeStamps, int minInterval, int maxLimit) {
        ArrayList<String> timeList = new ArrayList<String>();
        ArrayList<Integer> startTime = new ArrayList<Integer>();
        timeList.add(ts2Time(timeStamps.get(0)));
        startTime.add(timeStamps.get(0));
        float sumStep = 0;
        int length = 0;
        for (int i = 0; i < steps.size(); i++) {
            float avgStep = sumStep / length;
            int thisStep = steps.get(i);
            if (followStep(thisStep, avgStep, maxLimit)) {
                length += 1;
                sumStep += thisStep;
            } else {
                boolean newStart = true;
                for (int j = i + 1; j < i + minInterval; j++) {
                    if (j + minInterval >= steps.size())
                        break;
                    float intervalStep = 0;
                    for (int k = 0; k < minInterval; k++)
                        intervalStep += steps.get(j + k);
                    if (followStep(intervalStep / minInterval, avgStep, minInterval)) {
                        newStart = false;
                        break;
                    }
                }
                if (newStart && (timeStamps.get(i) - startTime.get(startTime.size() - 1)) > minInterval * 60) {
                    timeList.add(ts2Time(timeStamps.get(i)));
                    startTime.add(timeStamps.get(i));
                    sumStep = thisStep;
                    length = 1;
                } else {
                    sumStep += thisStep;
                    length += 1;
                }
            }
        }
        timeList.add(ts2Time(timeStamps.get(timeStamps.size() - 1)));
        return timeList;
    }

    private boolean followStep(float step, float avgStep, int maxLimit) {
        if (Math.abs(avgStep - 0) < 1) {
            return step == 0;
        }
        return Math.abs(step - avgStep) < maxLimit;
    }

    private String ts2Time(int timeStamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(new Date(Long.parseLong(Integer.toString(timeStamp) + "000")));
    }

    private String ts2Date(int timeStamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(new Date(Long.parseLong(Integer.toString(timeStamp) + "000")));
    }
}
