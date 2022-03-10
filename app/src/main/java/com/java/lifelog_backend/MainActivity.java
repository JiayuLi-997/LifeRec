package com.java.lifelog_backend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.preference.PreferenceActivity;

import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import android.view.View;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.Toast;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    LocationManager lm;
    Context mainContext;
    int photoNum = 0;
    Button photoButton;
    Uri imageUri;
    String intervalTime;
    String fileName;

    private Context mContext;
    private boolean[] checkItems;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mainContext = this.getBaseContext();
        photoNum = getPhotoNum();
        photoButton = (Button) findViewById(R.id.photo);
        photoButton.setOnClickListener(this);

        //获取GPS权限
        if (!isGpsAble()) {
            Toast.makeText(MainActivity.this, "Please enable GPS access", Toast.LENGTH_SHORT).show();
            openGPS();
        }

        // Permission 申请
        List<String> permissionList = new ArrayList<>();
        //从GPS获取最近的定位信息
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
        }

        // Add music tracing
        checkPermission();
        toggleNotificationListenerService(this);

        initView();
        //获取锁屏后运行权限
        initWakeLock();

        // 打开notification设置
        startNotification();
    }

    private void startNotification() {
        String Setting_Path = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/setting/";
        File openFile = new File(Setting_Path + "setting.json");
        String interval = "120";
        String start_time_setting = "8:00";
        String end_time_setting = "22:00";
        String tagging_time_setting = "23:00";
        String breakfast_time_setting = "8:30";
        String lunch_time_setting = "11:30";
        String dinner_time_setting = "17:30";
        String clocks="";
        if (openFile.exists()) {
            try{
                JsonParser parser = new JsonParser();
                String Filename = Setting_Path + "setting.json";
                JsonObject object = (JsonObject) parser.parse(new FileReader(Filename));
                tagging_time_setting = (object.get("tagging_time_setting").getAsString());
                breakfast_time_setting = (object.get("breakfast_time_setting").getAsString());
                lunch_time_setting = (object.get("lunch_time_setting").getAsString());
                dinner_time_setting = (object.get("dinner_time_setting").getAsString());
                clocks = object.get("reminder_clocks").getAsString();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("is_here", "is_error");

            }
        }
        Intent i = new Intent(this, NotifyService.class);
        i.putExtra("trace_time", tagging_time_setting);
        i.putExtra("breakfast", breakfast_time_setting);
        i.putExtra("lunch", lunch_time_setting);
        i.putExtra("dinner", dinner_time_setting);
        i.putExtra("clocks",clocks);

        startService(i);
    }


    private void checkPermission() {
        if (!isEnabled()) {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }
    }


    // 判断是否打开了通知监听权限
    private boolean isEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //先关闭再启动
    public static void toggleNotificationListenerService(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, MyNotificationListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(context, MyNotificationListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


    private void initView() {

        findViewById(R.id.btn_mood_request).setOnClickListener(this);
        findViewById(R.id.btn_mood_submit).setOnClickListener(this);
        findViewById(R.id.btn_trace).setOnClickListener(this);
        findViewById(R.id.btn_setting).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_mood_request:
                // jump to MoodRequest
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), MoodRequestActivity.class);
                intent.putExtra("interval",intervalTime);
                this.startActivity(intent);
                break;
            case R.id.btn_mood_submit:
                // jump to MoodSubmit
                Intent intent1 = new Intent();
                intent1.setClass(getApplicationContext(), MoodSubmitActivity.class);
                this.startActivity(intent1);
                break;
            case R.id.btn_trace:
                Intent intent2 = new Intent();
                intent2.setClass(getApplicationContext(), TraceActivity.class);
                this.startActivity(intent2);
                break;
            case R.id.btn_setting:
                Intent intent3 = new Intent();
                intent3.setClass(getApplicationContext(), SettingActivity.class);
                this.startActivityForResult(intent3, 100);
                break;
            case R.id.photo:
                final String[] options = new String[]{"拍照", "从相册选择"};
                alert = null;
                builder = new AlertDialog.Builder(MainActivity.this);
                alert = builder.setTitle("选择图片")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "你选择了" + options[which], Toast.LENGTH_SHORT).show();
                                switch (which){
                                    case 0:
                                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                                        }
                                        else openCamera();
                                        break;
                                    case 1:
                                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                                        }
                                        else openAlbum();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).create();
                alert.show();
        }
    }


    //获取用户权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //GPS权限
        if (requestCode == 1) {
            switch (grantResults[0]) {
                case 0:
                    Toast.makeText(MainActivity.this, "GPS access granted", Toast.LENGTH_LONG).show();
                    break;
                case -1:
                    Toast.makeText(MainActivity.this, "GPS access denied, please enable the permission", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 0);
                    break;
            }
            switch (grantResults[1]) {
                case 0:
                    Toast.makeText(MainActivity.this, "File read/write operation granted", Toast.LENGTH_LONG).show();
                    break;
                case -1:
                    Toast.makeText(MainActivity.this, "File read/write operation denied, please enable the permission", Toast.LENGTH_LONG).show();
                    //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    //startActivityForResult(intent, 0);
                    break;
            }
        }
        //相机权限
        else if (requestCode == 2) {
            switch ((grantResults[0])) {
                case 0:
                    openCamera();
                    break;
                case 1:
                    Toast.makeText(MainActivity.this, "Camera not available, please enable the permission", Toast.LENGTH_LONG).show();
                    break;
            }
        }
        //相册权限
        else if(requestCode == 3){
            switch ((grantResults[0])) {
                case 0:
                    openAlbum();
                    break;
                case 1:
                    Toast.makeText(MainActivity.this, "Photo album not available, please enable the permission", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    //检测GPS是否可用
    private boolean isGpsAble() {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //让用户自行打开GPS
    private void openGPS() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, 0);
    }

    private void openCamera() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("MM-dd-HH-mm-ss");// a为am/pm的标记
        fileName = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/image/" +
                "image_" + photoNum+ "_" + sdf.format(date) +".png";
        //System.out.println(fileName);
        File outputImage = new File(fileName);
        try {
            if (!outputImage.getParentFile().exists()) {
                outputImage.getParentFile().mkdirs();
            }
            if (outputImage.exists())
                outputImage.delete();
            outputImage.createNewFile();
            if (Build.VERSION.SDK_INT >= 24) {
                //Toast.makeText(this, ">=24", Toast.LENGTH_SHORT).show();
                imageUri = FileProvider.getUriForFile(this.getApplicationContext(), "com.java.lifelog_backend.provider", outputImage);
            } else {
                //Toast.makeText(this, "<24", Toast.LENGTH_SHORT).show();
                imageUri = Uri.fromFile(outputImage);
            }
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, 1);
        } catch (IOException e) {
            Toast toast= Toast.makeText(getApplicationContext(),"Failed to save the picture, please take the picture again!",Toast.LENGTH_LONG);
            toast.show();
            e.printStackTrace();
        }

    }

    private void openAlbum() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("MM-dd-HH-mm-ss");// a为am/pm的标记
        fileName = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/image/" +
                "image_" + photoNum+ "_" + sdf.format(date) +".png";
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //Intent.ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT"
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    private void galleryAddPic(Uri uri){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        sendBroadcast(mediaScanIntent);
    }

    private int getPhotoNum() {
        int tempNum = 0;
        String filePath = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/image/";
        File testPhoto = new File(filePath + "image_" + 0 + ".png");
        for (; ; tempNum++) {
            if (!testPhoto.exists())
                break;
            else
                testPhoto = new File(filePath + "image_" + (tempNum + 1) + ".png");
        }
        return tempNum;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //拍照请求成功
        switch (requestCode) {
            case 1: //拍照
                if (resultCode == RESULT_OK) {
                    Log.w("CAMERA","000111\n\n000111");
                    photoNum++;
                    galleryAddPic(imageUri);
                    Toast toast= Toast.makeText(getApplicationContext(),"Picture successfully saved to "+fileName,Toast.LENGTH_LONG);
                    toast.show();
                    //保存到系统相册
                    //Log.w("SAVE", "path:"+fileName);
                    try {
                        MediaStore.Images.Media.insertImage(getContentResolver(), fileName, fileName,"discription");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));
                }
                break;
            case 2: //从相册选照片
                String imagePath = null;
                if (Build.VERSION.SDK_INT >= 19) {
                    // 4.4及以上系统使用这个方法处理图片
                    //Toast.makeText(this, "album >=19", Toast.LENGTH_SHORT).show();
                    Uri uri = data.getData();
                    //imagePath = uritopath(uri);
                    if (DocumentsContract.isDocumentUri(this, uri)) {
                        // 如果是document类型的Uri，则通过document id处理
                        //Toast.makeText(this, "uri: document", Toast.LENGTH_SHORT).show();
                        Log.w("uritopath: ","document");
                        String docId = DocumentsContract.getDocumentId(uri);
                        if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                            String id = docId.split(":")[1];
                            // 解析出数字格式的id
                            String selection = MediaStore.Images.Media._ID + "=" + id;
                            imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                        } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content: //downloads/public_downloads"), Long.valueOf(docId));
                            //Toast.makeText(this, "album: document: ababa", Toast.LENGTH_SHORT).show();
                            imagePath = getImagePath(contentUri, null);
                        }
                    } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                        Log.w("uritopath: ","content");
                        //Toast.makeText(this, "uri: content", Toast.LENGTH_SHORT).show();
                        // 如果是content类型的Uri，则使用普通方式处理
                        imagePath = getImagePath(uri, null);
                    } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                        //Toast.makeText(this, "uri: file", Toast.LENGTH_SHORT).show();
                        // 如果是file类型的Uri，直接获取图片路径即可
                        imagePath = uri.getPath();
                    }

                } else {
                    // 4.4以下系统使用这个方法处理图片
                    //handleImageBeforeKitKat(data);
                    Uri uri = data.getData();
                    imagePath = getImagePath(uri, null);

                }
                Bitmap bits = getbit(imagePath);
                saveImgToDisk(bits);
                if (resultCode == RESULT_OK) {
                    photoNum++;
                    galleryAddPic(imageUri);
                    Toast toast= Toast.makeText(getApplicationContext(),"Picture successfully saved to "+fileName,Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            case 3:
                if (resultCode == RESULT_OK) {
                    System.out.println("hear");

                    //启动GPS服务
                    Intent gpsIntent = new Intent(this, GpsServer.class);
                    startService(gpsIntent);
                }
                break;
            case 100:
                if (resultCode == RESULT_OK) {
                    intervalTime = data.getExtras().getString("interval");
                }
                break;
            default:
                Toast.makeText(getApplicationContext(),"break",Toast.LENGTH_LONG).show();
        }
    }

    public void initWakeLock() {
        PowerManager pm = (PowerManager) mainContext.getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:lifelog_backend"));
            MainActivity.this.startActivityForResult(intent, 3);
        }
        //启动GPS服务
        Intent gpsIntent = new Intent(this, GpsServer.class);
        startService(gpsIntent);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
           if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private String uritopath(Uri uri){
        Log.w("uritopath: ","\n\n\n");
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            //Toast.makeText(this, "uri: document", Toast.LENGTH_SHORT).show();
            Log.w("uritopath: ","document");
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content: //downloads/public_downloads"), Long.valueOf(docId));
                //Toast.makeText(this, "album: document: ababa", Toast.LENGTH_SHORT).show();
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            Log.w("uritopath: ","content");
            //Toast.makeText(this, "uri: content", Toast.LENGTH_SHORT).show();
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //Toast.makeText(this, "uri: file", Toast.LENGTH_SHORT).show();
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    private Bitmap getbit(String path){
        Bitmap bitmap = null;
        if(TextUtils.isEmpty(path)) {
            return bitmap;
        }
        try {
            File file = new File(path);
            if (file.exists()) {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                opt.inPurgeable = true;
                opt.inInputShareable = true;
                bitmap = BitmapFactory.decodeFile(path, opt);
            }
        } catch (Exception e) {
        }

        return bitmap;

    }

    public void saveImgToDisk(Bitmap bitmap) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("MM-dd-HH-mm-ss");// a为am/pm的标记
        fileName = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/image/" +
                "image_" + photoNum + "_" + sdf.format(date) +".png";
        //System.out.println(fileName);
        File file = new File(fileName);
        if(file == null) {
            return;
        }

        //if(isFileExists(file.getPath())) {
        //    return;
        //}


        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            String path = file.getPath();
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
