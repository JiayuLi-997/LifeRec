package com.java.lifelog_backend;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

class MusicViewHolder
{
    TextView musicNameText;
    TextView musicSingerText;
//    TextView musicValenceText;
    String musicURL;

    public MusicViewHolder(View convertView)
    {
        musicNameText = (TextView) convertView.findViewById(R.id.music_name);
        musicSingerText = (TextView) convertView.findViewById(R.id.music_singer);
//        musicValenceText = (TextView) convertView.findViewById(R.id.music_valence);
    }

    public void setContent(MusicInfo musicInfo)
    {
        musicNameText.setText(musicInfo.musicName);
        musicSingerText.setText(musicInfo.musicSinger);
//        musicValenceText.setText(musicInfo.musicValence);
        musicURL = musicInfo.musicURL;
    }
}

public class MusicAdapter extends BaseAdapter {
    private List<MusicInfo> musicInfos;
    private LayoutInflater layoutInflater;

    public MusicAdapter(Context context, List<MusicInfo>list)
    {
        layoutInflater = LayoutInflater.from(context);
        musicInfos = list;
    }

    @Override
    public int getCount() {
        return musicInfos.size()+1;
    }

    @Override
    public Object getItem(int position)
    {// from 0
        Iterator it =musicInfos.iterator();
        for(int i=0;i<position; i++){
            if(it.hasNext()) it.next();
            else return null;
        }
        if(it.hasNext()) return it.next();
        else return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MusicViewHolder viewHolder;  final int p = position;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.music_item, null);
            viewHolder = new MusicViewHolder(convertView);
        }
        else {// 缓存区重复利用
            viewHolder = (MusicViewHolder) convertView.getTag();
        }
        if(position == 0)
        {
            convertView.setTag(viewHolder);
            return  convertView;
        }
        final MusicInfo info = (MusicInfo) getItem(position-1);
        viewHolder.setContent(info);
        convertView.setTag(viewHolder);
        return convertView;
    }
}
