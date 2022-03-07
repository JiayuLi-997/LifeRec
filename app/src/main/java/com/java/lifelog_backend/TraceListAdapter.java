package com.java.lifelog_backend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class TraceListAdapter extends BaseAdapter {
    private Context context;
    public List<Trace> traceList = new ArrayList<>(1);
    private static final int TYPE_TOP = 0x0000;
    private static final int TYPE_NORMAL = 0x0001;

    public TraceListAdapter(Context context, List<Trace> traceList) {
        this.context = context;
        this.traceList = traceList;
    }

    public void addItem(String time, String event, double[] mood) {
        traceList.add(new Trace(time, event, mood));
        notifyDataSetChanged();
    }

    public void addItemFirst(String time, String event, double[] mood) {
        traceList.add(0, new Trace(time, event, mood));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return traceList.size();
    }

    @Override
    public Trace getItem(int position) {
        return traceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updatelist() {
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TraceListAdapter.ViewHolder holder;
        final Trace trace = getItem(position);
        if (convertView != null) {
            holder = (com.java.lifelog_backend.TraceListAdapter.ViewHolder) convertView.getTag();
        } else {
            holder = new com.java.lifelog_backend.TraceListAdapter.ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_trace, parent, false);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.event = (TextView) convertView.findViewById(R.id.event);
            holder.mood = (TextView) convertView.findViewById(R.id.mood);
            holder.tvTopLine = (TextView) convertView.findViewById(R.id.tvTopLine);
            holder.tvDot = (TextView) convertView.findViewById(R.id.tvDot);
            holder.delete_btn = convertView.findViewById(R.id.btn_delete_item);
            holder.insert_btn = convertView.findViewById(R.id.btn_insert_item);
            convertView.setTag(holder);
        }

        if (getItemViewType(position) == TYPE_TOP) {
            // 第一行头的竖线不显示
            holder.tvTopLine.setVisibility(View.INVISIBLE);
            // 字体颜色加深
//            holder.tvAcceptTime.setTextColor(0xff555555);
//            holder.tvAcceptStation.setTextColor(0xff555555);
            holder.time.setTextColor(0xff999999);
            holder.event.setTextColor(0xff999999);
            holder.mood.setTextColor(0xff999999);
            holder.tvDot.setBackgroundResource(R.drawable.timeline_dot_first);
        } else if (getItemViewType(position) == TYPE_NORMAL) {
            holder.time.setTextColor(0xff999999);
            holder.event.setTextColor(0xff999999);
            holder.mood.setTextColor(0xff999999);
            holder.tvDot.setBackgroundResource(R.drawable.timeline_dot_normal);
        }

        holder.delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                traceList.remove(position);
                notifyDataSetChanged();
            }
        });
        holder.insert_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double[] mood = new double[2];
                mood[0] = 0;
                mood[1] = 0;
//                adapter.addItem("time", "event", mood);
                traceList.add(position + 1, new Trace("00:00", "activity", mood));
                notifyDataSetChanged();
            }
        });

        holder.time.setText(trace.getTime());
        holder.event.setText(trace.getEvent());
        StringBuilder sb = new StringBuilder();
        sb.append("Your mood: \n");
        sb.append(String.format("%.2f", trace.getMood()[0]));
        sb.append(" , ");
        sb.append(String.format("%.2f", trace.getMood()[1]));
        holder.mood.setText(sb);
        return convertView;
    }

    @Override
    public int getItemViewType(int id) {
        if (id == 0) {
            return TYPE_TOP;
        }
        return TYPE_NORMAL;
    }

    static class ViewHolder {
        public TextView time, event, mood;
        public TextView tvTopLine, tvDot;
        public Button delete_btn;
        public Button insert_btn;
    }
}
