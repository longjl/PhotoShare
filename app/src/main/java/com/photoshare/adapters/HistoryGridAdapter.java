package com.photoshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.photoshare.R;
import com.photoshare.model.Photo;
import com.photoshare.views.PhotoImageView;
import com.photoshare.views.PhotoItemLayout;

import java.util.List;

/**
 * Created by longjianlin on 15/3/20.
 */
public class HistoryGridAdapter extends BaseAdapter {

    private List<Photo> mItems;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    public HistoryGridAdapter(Context context, List<Photo> photos) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mItems = photos;
    }

    public int getCount() {
        return null != mItems ? mItems.size() : 0;
    }

    public long getItemId(int position) {
        return position;
    }

    public Photo getItem(int position) {
        return mItems.get(position);
    }

    public View getView(int position, View view, ViewGroup parent) {
        if (null == view) {
            view = mLayoutInflater.inflate(R.layout.item_grid_photo_selected, parent, false);
        }

        PhotoItemLayout layout = (PhotoItemLayout) view;
        PhotoImageView iv = layout.getImageView();

        final Photo upload = getItem(position);

        iv.requestThumbnail(upload, true);
        layout.setAnimateWhenChecked(false);
        layout.setPhotoSelection(upload);
        layout.setShowCheckbox(false);
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        //mItems = mController.getSelected();
        super.notifyDataSetChanged();
    }

}

