package com.photoshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.photoshare.PhotoApplication;
import com.photoshare.PhotoController;
import com.photoshare.R;
import com.photoshare.model.Photo;
import com.photoshare.views.PhotoImageView;
import com.photoshare.views.PhotoItemLayout;

import java.util.List;

/**
 * Created by longjianlin on 15/3/20.
 */
public class SelectedPhotosBaseAdapter extends BaseAdapter {

    private List<Photo> mItems;

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final PhotoController mController;
    private final boolean mShowCheckbox;

    public SelectedPhotosBaseAdapter(Context context, boolean showCheckbox) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mShowCheckbox = showCheckbox;

        PhotoApplication app = PhotoApplication.getApplication(context);
        mController = app.getPhotoUploadController();
        mItems = mController.getSelected();
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
        layout.setShowCheckbox(mShowCheckbox);

        // If we're showing the checkbox, then check the background too
        if (mShowCheckbox) {
            layout.setChecked(true);
        }

        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        mItems = mController.getSelected();
        super.notifyDataSetChanged();
    }

}

