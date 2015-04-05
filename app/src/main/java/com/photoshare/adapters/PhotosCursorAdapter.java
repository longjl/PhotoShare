package com.photoshare.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.Checkable;

import com.photoshare.PhotoApplication;
import com.photoshare.PhotoController;
import com.photoshare.R;
import com.photoshare.model.Photo;
import com.photoshare.util.MediaStoreCursorHelper;
import com.photoshare.views.PhotoImageView;
import com.photoshare.views.PhotoItemLayout;

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
