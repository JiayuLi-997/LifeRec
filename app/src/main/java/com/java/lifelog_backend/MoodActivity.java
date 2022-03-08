package com.java.lifelog_backend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MoodActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private int[] location;
    private TextView location_text;
    private double[] mood;
    private double[] new_mood;
    boolean showMusicDialog;
    boolean fromMusicDialog;
    boolean backFromMusicDialog;
    boolean fileReadMood;
    private AlertDialog.Builder builder;
    String pmood = "";
    String TAG="moodActivity";

    //以下五个变量全部是音乐相关
    String responseText;
    int userPreference;
    int musicNum;
    int selectedIndex;
    String serverURL = "https://ir.cs.tsinghua.edu.cn/lifelogger/recommendation&name=";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);
        showMusicDialog = false;
        fromMusicDialog = false;
        backFromMusicDialog = false;
        fileReadMood = true;
        init();
        update();
    }

    private void init() {
        location = new int[]{0, 0};
        location_text = findViewById(R.id.moodTextResult);
        mood = new double[]{0, 0};
        new_mood = new double[]{0, 0};
        Configuration configuration = getResources().getConfiguration();
        Log.i(TAG,configuration.getLocales().toString());
        Log.i(TAG,configuration.getLocales().toLanguageTags());
        Log.i(TAG,configuration.locale.getLanguage());
        Log.i(TAG,configuration.locale.getCountry());
        if(!(configuration.locale.getLanguage().contains("zh"))){
            Log.i(TAG,"English version!");
            ImageView imageview=(ImageView)super.findViewById(R.id.mood_pic);
            imageview.setImageResource(R.drawable.thayer_mood_eng);
//            imageview.setBackgroundDrawable(getResources().getDrawable(R.drawable.thayer_mood_eng));
        }
        else{
            Log.i(TAG,"Chinese version!");
            ImageView imageview=(ImageView)super.findViewById(R.id.mood_pic);
            imageview.setImageResource(R.drawable.thayer_mood);
//            imageview.setBackgroundDrawable(getResources().getDrawable(R.drawable.thayer_mood));
        }
        findViewById(R.id.btn_mood_submit).setOnClickListener(this);
        findViewById(R.id.mood_pic).setOnTouchListener(this);
        TextView tx = findViewById(R.id.mood_text);
        String intentTitle = "";
        intentTitle = getIntent().getStringExtra("title");
        pmood = getIntent().getStringExtra("pmood");
        if (intentTitle != null)
            if (intentTitle.equals("music")) {
                if(configuration.locale.getLanguage().contains("zh")){
                    tx.setText("你现在的心情怎么样？");
                }
                else{
                    tx.setText("How are you feeling now?");
                }
                showMusicDialog = false;
                fromMusicDialog = true;
            }
            else if (intentTitle.equals("How were you feeling then??")) {
                if(configuration.locale.getLanguage().contains("zh")){
                    tx.setText("你当时的心情怎么样？");
                }
                else{
                    tx.setText("How were you feeling then?");
                }
                fileReadMood = false;
                showMusicDialog = true;
            }
            else if (intentTitle.equals("How were you feeling then?")) {
//                System.out.println("but hear");
                if(configuration.locale.getLanguage().contains("zh")){
                    tx.setText("你当时的心情如何？");
                }
                else{
                    tx.setText("What was your feeling then?");
                }
                //fileReadMood = false;
                // showMusicDialog = true;
                showMusicDialog = false;
            }
            else {
                String title = getIntent().getStringExtra("title");
                if (title.equals("How are you feeling now?")){
                    if(configuration.locale.getLanguage().contains("zh")){
                        tx.setText("你现在的心情怎么样？");
                    }
                    else{
                        tx.setText("How are you feeling now?");
                    }
                }
                else if(title.equals("How were you feeling then?")){
                    if(configuration.locale.getLanguage().contains("zh")){
                        tx.setText("你当时的心情怎么样？");
                    }
                    else{
                        tx.setText("How were you feeling then?");
                    }
                }
                showMusicDialog = false;
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case 10: //from music activity
                if (resultCode == RESULT_OK) {
                    System.out.println("return musiv activity hear");
                    selectedIndex = data.getExtras().getInt("selectedIndex");
                    userPreference = data.getExtras().getInt("userPreference");
                    responseText = data.getExtras().getString("responseText");
                    musicNum = data.getExtras().getInt("musicNum");
                    serverURL = serverURL + data.getExtras().getString("userName");
                    backFromMusicDialog = true;
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_mood_submit:
                if (fromMusicDialog) {
                    //todo
                    Intent intent = new Intent();
                    intent.putExtra("mood", mood);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    if (showMusicDialog) {
                        showMusicDialog = false;
                        showMusicDialog();
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra("mood", mood);
                        setResult(RESULT_OK, intent);
                        if (backFromMusicDialog) {
                            double[] latestMood = new double[]{0, 0};
                            if (fileReadMood) latestMood = getLatestMood();
                            else latestMood = mood;
                            JsonParser parser = new JsonParser();
                            JsonObject object = parser.parse(responseText).getAsJsonObject();
                            List<JsonObject> objectList = new ArrayList<>();
                            objectList.add(object.getAsJsonObject("0"));
                            objectList.add(object.getAsJsonObject("1"));
                            objectList.add(object.getAsJsonObject("2"));
                            JsonObject allObject = new JsonObject();
                            for (int i = 0; i < musicNum; i++) {
                                JsonObject musicObject = new JsonObject();
                                if (i == selectedIndex) {
                                    musicObject.addProperty("click", 1);
                                    musicObject.addProperty("preference", userPreference);
                                } else {
                                    musicObject.addProperty("click", 0);
                                    musicObject.addProperty("preference", 0);
                                }
                                musicObject.addProperty("mid", objectList.get(i).get("mid").getAsString());
                                if (pmood != null && pmood.length() > 0) {
                                    musicObject.addProperty("mood_before", pmood);
                                } else {
                                    musicObject.addProperty("mood_before", latestMood[0] + "," + latestMood[1]);
                                }
                                musicObject.addProperty("mood_after", new_mood[0] + "," + new_mood[1]);
                                musicObject.addProperty("valence", objectList.get(i).get("valence").getAsString());
                                if (i == 0)
                                    allObject.addProperty("0", musicObject.toString());
                                else if (i == 1)
                                    allObject.addProperty("1", musicObject.toString());
                                else if (i == 2)
                                    allObject.addProperty("2", musicObject.toString());
                            }
                            OkHttpClient client = new OkHttpClient();
                            RequestBody body = null;
                            body = new FormBody.Builder().add("music", allObject.toString()).build();
                            if (body != null) {
                                Request request = new Request.Builder().url(serverURL).post(body).build();
                                Call call = client.newCall(request);
                                call.enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Log.i("app", "提交失败");
                                        finish();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        Log.i("app", "提交成功");
                                        finish();
                                    }
                                });
                            } else finish();

                        } else finish();
                        //finish();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        if (view.getId() != R.id.mood_pic) {
            return true;
        }
        int eventaction = event.getAction();
        switch (eventaction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:

                WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                double cursor_size = findViewById(R.id.mood_cursor).getHeight();
                double width = wm.getDefaultDisplay().getWidth();
                double height = findViewById(R.id.mood_pic).getHeight();
                double mid_x = width / 2;
                double mid_y = height / 2;
                double square_len = mid_x * 0.665;

                location[0] = (int) event.getX();
                location[1] = (int) event.getY();

                // set cursor location
                if ((location[0] - mid_x) > square_len) {
                    location[0] = (int) (mid_x + square_len);
                } else if ((mid_x - location[0]) > square_len) {
                    location[0] = (int) (mid_x - square_len);
                }
                if ((location[1] - mid_y) > square_len) {
                    location[1] = (int) (mid_y + square_len);
                } else if ((mid_y - location[1]) > square_len) {
                    location[1] = (int) (mid_y - square_len);
                }
                findViewById(R.id.mood_cursor).setX(location[0] - (int) (cursor_size / 2));
                findViewById(R.id.mood_cursor).setY(location[1] - (int) (cursor_size / 2));

                if (!backFromMusicDialog) {
                    // set mood
                    mood[0] = 4 * (location[0] - mid_x) / square_len;
                    mood[1] = -4 * (location[1] - mid_y) / square_len;
                    if (mood[0] > 4.0) {
                        mood[0] = 4.0;
                    } else if (mood[0] < -4.0) {
                        mood[0] = -4.0;
                    }
                    if (mood[1] > 4.0) {
                        mood[1] = 4.0;
                    } else if (mood[1] < -4.0) {
                        mood[1] = -4.0;
                    }
                } else {
                    // set mood
                    new_mood[0] = 4 * (location[0] - mid_x) / square_len;
                    new_mood[1] = -4 * (location[1] - mid_y) / square_len;
                    if (new_mood[0] > 4.0) {
                        new_mood[0] = 4.0;
                    } else if (new_mood[0] < -4.0) {
                        new_mood[0] = -4.0;
                    }
                    if (new_mood[1] > 4.0) {
                        new_mood[1] = 4.0;
                    } else if (new_mood[1] < -4.0) {
                        new_mood[1] = -4.0;
                    }
                }
                update();
                break;
        }
        return true;
    }

    private void update() {
        // update location
        StringBuilder sb = new StringBuilder();
        sb.append("Your mood: \n");
        if (!backFromMusicDialog)
            sb.append(String.format("%.2f", mood[0]));
        else sb.append(String.format("%.2f", new_mood[0]));
        sb.append(" , ");
        if (!backFromMusicDialog)
            sb.append(String.format("%.2f", mood[1]));
        else sb.append(String.format("%.2f", new_mood[1]));
        location_text.setText(sb.toString());
    }

    private void showMusicDialog() {
        final Configuration configuration = getResources().getConfiguration();
        String msg = "Would you like to listen to some music";
        String yes = "Yes";
        String no = "No";
        if(configuration.locale.getLanguage().contains("zh")){
            msg = "你现在想听音乐吗？";
            yes = "是";
            no = "否";
        }
        builder = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("Music")
                .setMessage(msg).setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(MoodActivity.this, "听音乐", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.setClass(MoodActivity.this, MusicActivity.class);
                        intent.putExtra("title", "mood");
                        if (pmood != null) {
                            intent.putExtra("pmood", pmood);
                        } else {
                            String curEmotionString = String.format("%.3f", mood[0]) + "," + String.format("%.3f", mood[1]);
                            intent.putExtra("pmood", curEmotionString);
                        }
                        startActivityForResult(intent, 10);
                        TextView tx = findViewById(R.id.mood_text);
                        if(configuration.locale.getLanguage().contains("zh")){
                            tx.setText("你现在的心情如何？");
                        }
                        else{
                            tx.setText("How are you feeling now?");
                        }

                    }
                }).setNegativeButton(no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(MoodActivity.this, "不听音乐", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.putExtra("mood", mood);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
        builder.create().show();
    }

    private double[] getLatestMood() {
        String fileName = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/emotion/user_submitted_emotions.txt";
        File file = new File(fileName);
        BufferedReader reader = null;
        double[] latestMood = new double[]{0, 0};
        try {
            if (file.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = "";
                String temp = "";
                //读取最后一行的内容
                while ((temp = reader.readLine()) != null) {
                    line = temp;
                }
                int begin = 0, end = 0;
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == "(".toCharArray()[0])
                        begin = i + 1;
                    if (line.charAt(i) == ")".toCharArray()[0])
                        end = i;
                }
                String array = line.substring(begin, end);
                String[] temp2 = array.split(",");
                latestMood[0] = Float.parseFloat(temp2[0]);
                latestMood[1] = Float.parseFloat(temp2[1]);
                //System.out.println(latestMood[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return latestMood;
    }
}
