package com.photoshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.photoshare.R;

/**
 * Created by longjianlin on 15/3/22.
 */
public class ShareAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private int[] share_res = {R.drawable.logo_sinaweibo, R.drawable.logo_webchatmoments};
    private int[] titles = {R.string.sina_weibo, R.string.WebChat_Moments};
    private String[] share_keys;

    public ShareAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        share_keys = context.getResources().getStringArray(R.array.share_keys);
    }

    @Override
    public int getCount() {
        return share_res.length;
    }

    @Override
    public String getItem(int position) {
        return share_keys[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_grid_share, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.iv_share);
            holder.tv_title = (TextView) view.findViewById(R.id.tv_title);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.imageView.setImageResource(share_res[position]);
        holder.tv_title.setText(titles[position]);
        return view;
    }

    class ViewHolder {
        public ImageView imageView;
        public TextView tv_title;
    }

}
