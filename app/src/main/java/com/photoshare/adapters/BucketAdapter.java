package com.photoshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.photoshare.R;
import com.photoshare.model.MediaStoreBucket;

import java.util.ArrayList;

/**
 * Created by longjianlin on 15/3/22.
 */
public class BucketAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<MediaStoreBucket> mBuckets;

    public BucketAdapter(Context context, ArrayList<MediaStoreBucket> buckets) {
        inflater = LayoutInflater.from(context);
        mBuckets = buckets;
    }

    @Override
    public int getCount() {
        return null == mBuckets ? 0 : mBuckets.size();
    }

    @Override
    public MediaStoreBucket getItem(int position) {
        return mBuckets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_bucket, null);
            holder = new ViewHolder();
            holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
            holder.tv_count = (TextView) view.findViewById(R.id.tv_count);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        MediaStoreBucket bucket = mBuckets.get(position);
        holder.tv_name.setText(bucket.getName());
        if (bucket.getId() != null) {
            holder.tv_count.setText("(" + bucket.getImageCount() + ")");
        } else {
            holder.tv_count.setText("");
        }
        return view;
    }

    class ViewHolder {
        public TextView tv_name;
        public TextView tv_count;
    }
}
