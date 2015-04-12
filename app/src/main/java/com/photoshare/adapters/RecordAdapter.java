package com.photoshare.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.photoshare.R;
import com.photoshare.model.Record;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by longjianlin on 15/3/22.
 */
public class RecordAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Record> records;
    private SimpleDateFormat sdfM = new SimpleDateFormat("M月");
    private SimpleDateFormat sdfD = new SimpleDateFormat("dd");
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public RecordAdapter(Context context, List<Record> records) {
        this.inflater = LayoutInflater.from(context);
        this.records = records;
    }

    @Override
    public int getCount() {
        return records == null ? 0 : records.size();
    }

    @Override
    public Record getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_list_record, null);
            holder = new ViewHolder();
            holder.tv_content = (TextView) view.findViewById(R.id.tv_content);
            holder.tv_d = (TextView) view.findViewById(R.id.tv_d);
            holder.tv_m = (TextView) view.findViewById(R.id.tv_m);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        Record record = getItem(position);
        holder.tv_content.setText(record.content);
        try {
            Date date = format.parse(record.date);
            holder.tv_d.setText(sdfD.format(date));
            holder.tv_m.setText(sdfM.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return view;
    }

    class ViewHolder {
        public TextView tv_content;
        public TextView tv_d;
        public TextView tv_m;
    }

}
