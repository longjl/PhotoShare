package com.tintinshare.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.Checkable;

import com.tintinshare.PhotoApplication;
import com.tintinshare.PhotoController;
import com.tintinshare.R;
import com.tintinshare.model.Photo;
import com.tintinshare.util.MediaStoreCursorHelper;
import com.tintinshare.views.PhotoImageView;
import com.tintinshare.views.PhotoItemLayout;

/**
 * 图片适配器
 * Created by longjianlin on 15/3/19.
 */
public class PhotosCursorAdapter extends ResourceCursorAdapter {
    private final PhotoController mController;
    public PhotosCursorAdapter(Context context, Cursor c) {
        super(context, R.layout.item_grid_photo, c, 0);

        PhotoApplication app = PhotoApplication.getApplication(context);
        mController = app.getPhotoUploadController();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        PhotoItemLayout layout = (PhotoItemLayout) view;
        PhotoImageView iv = layout.getImageView();

       final Photo photo = MediaStoreCursorHelper.photosCursorToSelection(
                MediaStoreCursorHelper.MEDIA_STORE_CONTENT_URI, cursor);

        if (null != photo) {
            iv.setFadeInDrawables(false);
            iv.requestThumbnail(photo, false);
            layout.setPhotoSelection(photo);

            if (null != mController) {
                ((Checkable) view).setChecked(mController.isSelected(photo));
            }
        }
    }

}
