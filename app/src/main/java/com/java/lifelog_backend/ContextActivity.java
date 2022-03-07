package com.java.lifelog_backend;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

public class ContextActivity extends AppCompatActivity  implements View.OnClickListener{

    private RadioGroup radiodoor;
    private RadioGroup radiocompany;
    private RadioGroup radiolocation;
    private EditText ans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context);

        findViewById(R.id.submit_context).setOnClickListener(this);
        radiodoor = findViewById(R.id.indoor_radiobtn);
        radiocompany = findViewById(R.id.company_radiobtn);
        radiolocation = findViewById(R.id.location_radiobtn);
        ans = findViewById(R.id.location_text);

        init();

    }

    private void init(){

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.submit_context:
                BufferedWriter out=null;

                String results = new String();
                long currentTime = System.currentTimeMillis();
                String emotion_event = ans.getText().toString();
                results += new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss\t").format(currentTime);
                RadioButton door = (RadioButton)ContextActivity.this.findViewById(radiodoor.getCheckedRadioButtonId());
                results += "室内/室外:"+(door.getText())+"\t";
                RadioButton company = (RadioButton)ContextActivity.this.findViewById(radiocompany.getCheckedRadioButtonId());
                results += "陪伴:"+(company.getText())+"\t";
                RadioButton location = (RadioButton)ContextActivity.this.findViewById(radiolocation.getCheckedRadioButtonId());
                results += "地点:"+(location.getText())+"\t"+emotion_event+"\n";

                String path = Environment.getExternalStorageDirectory() + "/com.java.lifelog_backend/context";
                File file = new File(path+"/user_submittec_context.txt");
                try {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
                    out.newLine();//换行
                    out.write(results);
                }catch (Exception e) {
                    Log.e("Record", e.toString());
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Recorded error!", Toast.LENGTH_LONG).show();
                }finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i("context_results",results);
                    Toast.makeText(getApplicationContext(),"Context Successful",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.setClass(ContextActivity.this, MainActivity.class);
                    this.startActivity(intent);
                }

        }
    }



}
