package com.photoshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.photoshare.R;
import com.photoshare.model.Photo;

/**
 * 相机适配器
 * Created by longjianlin on 15/3/19.
 */
public class CameraBaseAdapter extends BaseAdapter {

    private final LayoutInflater mLayoutInflater;

    public CameraBaseAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public Photo getItem(int position) {
        return null;
    }

    public View getView(int position, View view, ViewGroup parent) {
        if (null == view) {
            view = mLayoutInflater.inflate(R.layout.item_grid_camera, parent, false);
        }
        return view;
    }

}
