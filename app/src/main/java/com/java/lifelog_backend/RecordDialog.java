package com.java.lifelog_backend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class RecordDialog extends PopupWindow {
    private Context mContext;

    private View view;

    private Button btn_save_pop;

    public TimePicker tp_time;

    public EditText text_event;

    public TextView text_mood;

    public Button btn_mood_set;


    public RecordDialog (Activity mContext, View.OnClickListener itemsOnClick) {

        this.mContext = mContext;
        this.view = LayoutInflater.from(mContext).inflate(R.layout.record_dialog, null);

        tp_time = view.findViewById(R.id.tp_time);
        tp_time.setIs24HourView(true);   //设置时间显示为24小时
        tp_time.setHour(0);
        tp_time.setMinute(0);
        tp_time.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);

        text_event = (EditText) view.findViewById(R.id.text_event);
        text_mood=(TextView) view.findViewById(R.id.text_mood);
        btn_mood_set = (Button) view.findViewById(R.id.btn_mood_set);

        btn_save_pop = (Button) view.findViewById(R.id.btn_save_pop);

        btn_mood_set.setOnClickListener(itemsOnClick);

        // 设置按钮监听
        btn_save_pop.setOnClickListener(itemsOnClick);

        // 设置外部可点击
        this.setOutsideTouchable(true);


        /* 设置弹出窗口特征 */
        // 设置视图
        this.setContentView(this.view);

        // 设置弹出窗体的宽和高
        /*
         * 获取圣诞框的窗口对象及参数对象以修改对话框的布局设置, 可以直接调用getWindow(),表示获得这个Activity的Window
         * 对象,这样这可以以同样的方式改变这个Activity的属性.
         */
        Window dialogWindow = mContext.getWindow();

        WindowManager m = mContext.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值

        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth((int) (d.getWidth() * 0.8));

        // 设置弹出窗体可点击
        this.setFocusable(true);

    }
}
