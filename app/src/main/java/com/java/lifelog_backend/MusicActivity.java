package com.java.lifelog_backend;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MusicActivity extends AppCompatActivity {
    Button backButton;
    boolean fromMoodActivity;
    String musicURL = "https://y.qq.com/n/yqq/song/001g6fPZ0pCL5K.html";
    String serverURL = "https://ir.cs.tsinghua.edu.cn/lifelogger/recommendation&name=";
    String pmood = "";
    String userName;
    String urlResponseText;
    List<MusicInfo> musicInfos;
    MusicAdapter musicAdapter;
    ListView mListView;
    int selectIndex = -1;
    int userPreference = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        getUserName();
        musicInfos = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.music_list);
        musicAdapter = new MusicAdapter(getBaseContext(), musicInfos);
        mListView.setAdapter(musicAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            boolean selected = false;

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0 || selected) return;
                MusicInfo musicInfo = (MusicInfo) adapterView.getItemAtPosition(position - 1);
                selectIndex = position - 1;
                selected = true;
                Uri uri = Uri.parse(musicInfo.musicURL);
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(uri);
                startActivity(intent);
                SystemClock.sleep(2000);
                showRatingDialog();
            }
        });
        //userName = null;
        selectIndex = -1;
        backButton = (Button) findViewById(R.id.music_back_button);
        fromMoodActivity = false;
        init();
        sendUserInfo();
    }

    private void init() {
        String from = getIntent().getStringExtra("title");
        pmood = getIntent().getStringExtra("pmood");
        Log.d("music_mood", pmood);
        if (from.equals("mood")) fromMoodActivity = true;
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromMoodActivity) {
                    Intent intent = new Intent();
                    intent.putExtra("selectedIndex", selectIndex);
                    intent.putExtra("userName", userName);
                    intent.putExtra("responseText", urlResponseText);
                    intent.putExtra("userPreference", userPreference);
                    intent.putExtra("musicNum", musicInfos.size());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.setClass(MusicActivity.this, MoodActivity.class);
                    intent.putExtra("title", "music");
                    startActivityForResult(intent, 10);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null)
            return;
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {
                    double[] new_mood = new double[]{0, 0};
                    new_mood = data.getExtras().getDoubleArray("mood");
                    double[] old_mood = new double[]{0, 0};
                    old_mood = getLatestMood();
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    JsonParser parser = new JsonParser();
                    JsonObject object = parser.parse(urlResponseText).getAsJsonObject();
                    List<JsonObject> objectList = new ArrayList<>();
                    objectList.add(object.getAsJsonObject("0"));
                    objectList.add(object.getAsJsonObject("1"));
                    objectList.add(object.getAsJsonObject("2"));
                    JsonObject allObject = new JsonObject();
                    for (int i = 0; i < musicInfos.size(); i++) {
                        JsonObject musicObject = new JsonObject();
                        if (i == selectIndex) {
                            musicObject.addProperty("click", 1);
                            musicObject.addProperty("preference", userPreference);
                        } else {
                            musicObject.addProperty("click", 0);
                            musicObject.addProperty("preference", 0);
                        }
                        musicObject.addProperty("mid", objectList.get(i).get("mid").getAsString());
//                        musicObject.addProperty("mood_before", old_mood[0]+","+old_mood[1]);
                        Log.d("music_mood_b", pmood);
                        musicObject.addProperty("mood_before", pmood);
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
                    Log.d("music_mood_s", allObject.toString());
                    body = new FormBody.Builder().add("music", allObject.toString()).build();
                    if (body != null) {
                        Request request = new Request.Builder().url(serverURL + userName).post(body).build();
                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.i("app", "提交失败");
                                Looper.prepare();
                                Toast.makeText(getBaseContext(), "Failed", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                                finish();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Log.i("app", "提交成功");
                                Looper.prepare();
                                Toast.makeText(getBaseContext(), "Succeed", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                                finish();
                            }
                        });
                    } else finish();
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void sendUserInfo() {
        //userName = getUserName();
        if (userName == null)
            userName = "anonymous user";
        System.out.println("send Info:" + userName);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(serverURL + userName + "__" + pmood).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("on Failure: Fail");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                        // Intent i = new Intent(MusicActivity.this, MainActivity.class);
                        // startActivity(i);
                        finish();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseText = response.body().string();
                    Log.d("mymusic", responseText);
                    urlResponseText = responseText;
                    runOnUiThread((new Runnable() {
                        @Override
                        public void run() {
                            System.out.println(responseText);
                            JsonParser parser = new JsonParser();
                            JsonElement element = parser.parse(responseText);
                            if (element.isJsonObject()) {
                                JsonObject allMusic = element.getAsJsonObject();
                                if (allMusic.getAsJsonObject("0") != null) {
                                    JsonObject musicObject = allMusic.getAsJsonObject("0");
                                    musicInfos.add(new MusicInfo(musicObject.get("mid").getAsString(),
                                            musicObject.get("music").getAsString(), musicObject.get("singer").getAsString(),
                                            musicObject.get("valence").getAsString(), musicObject.get("url").getAsString()));
                                }
                                if (allMusic.getAsJsonObject("1") != null) {
                                    JsonObject musicObject = allMusic.getAsJsonObject("1");
                                    musicInfos.add(new MusicInfo(musicObject.get("mid").getAsString(),
                                            musicObject.get("music").getAsString(), musicObject.get("singer").getAsString(),
                                            musicObject.get("valence").getAsString(), musicObject.get("url").getAsString()));
                                }
                                if (allMusic.getAsJsonObject("2") != null) {
                                    JsonObject musicObject = allMusic.getAsJsonObject("2");
                                    musicInfos.add(new MusicInfo(musicObject.get("mid").getAsString(),
                                            musicObject.get("music").getAsString(), musicObject.get("singer").getAsString(),
                                            musicObject.get("valence").getAsString(), musicObject.get("url").getAsString()));
                                }
                                musicAdapter.notifyDataSetChanged();
                            }
                        }
                    }));
                } else {
                    System.out.println("response Fail");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), "Fail to acquire music information", Toast.LENGTH_SHORT).show();
                            // Intent i = new Intent(MusicActivity.this, MainActivity.class);
                            // startActivity(i);
                            finish();
                        }
                    });
                }
            }
        });
    }

    private void getUserName() {
        String fileName = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/setting/setting.json";
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            if (file.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String jsonContent = reader.readLine().toString();
                System.out.println(jsonContent);
                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(jsonContent);
                if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    userName = object.get("user_id").getAsString();
                    System.out.println("User id:" + userName);
                }
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

    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MusicActivity.this);
        builder.setTitle("Please score this song:");
        final String[] tempKey = {"1 (strongly dislike)", "2 (dislike)", "3 (average)", "4 (like)", "5 (strongly like)"};
        builder.setItems(tempKey, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                userPreference = i + 1;
                System.out.println("userPrefernce:" + userPreference);
            }
        });

        builder.show();
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
