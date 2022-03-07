package com.java.lifelog_backend;

import android.content.Context;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.QWeather;
import com.qweather.sdk.view.HeConfig;


import static android.content.ContentValues.TAG;

public class WeatherAPI {
    String APIkey = "5c21462280a44743bc8737c8212deab9";
    Handler handler;

    WeatherAPI(Handler weatherHandler) {
        HeConfig.init("HE2104182321331976", APIkey);
        HeConfig.switchToDevService();
        handler = weatherHandler;
    }

    public void getWeather(final Context context, String location) {
        QWeather.getWeatherNow(context, location, new QWeather.OnResultWeatherNowListener() {
            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "getWeather onError: " + e);
                System.out.println("weatherFail");
                Toast.makeText(context.getApplicationContext(), "Failed to acquire weather information", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(WeatherNowBean weatherNowBean) {
                Message msg = new Message();
                msg.obj = (Object) new Gson().toJson(weatherNowBean);
                handler.sendMessage(msg);
            }
        });
    }
}
