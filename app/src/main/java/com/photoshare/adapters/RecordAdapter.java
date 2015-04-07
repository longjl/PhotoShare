package com.photoshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.photoshare.R;
import com.photoshare.model.Record;

import java.util.List;

/**
 * Created by longjianlin on 15/3/22.
 */
public class RecordAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Record> records;

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
            holder.tv_date = (TextView) view.findViewById(R.id.tv_date);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tv_content.setText(records.get(position).content);
        holder.tv_date.setText(records.get(position).date);
        return view;
    }

    class ViewHolder {
        public TextView tv_content;
        public TextView tv_date;
    }

}
