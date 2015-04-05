package com.photoshare.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.photoshare.PhotoApplication;
import com.photoshare.PhotoController;
import com.photoshare.R;
import com.photoshare.model.Photo;

/**
 * Created by longjianlin on 15/3/19.
 */
public class PhotoItemLayout extends CheckableFrameLayout implements View.OnClickListener {
    private final PhotoImageView mImageView;
    private final CheckableImageView mButton;
    private Photo mSelection;
    private boolean mAnimateCheck = true;
    private final PhotoController mController;

    public PhotoItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.item_grid_photo_internal, this);

        mController = PhotoApplication.getApplication(context).getPhotoUploadController();

        mImageView = (PhotoImageView) findViewById(R.id.iv_photo);

        mButton = (CheckableImageView) findViewById(R.id.civ_button);
        mButton.setOnClickListener(this);
    }

    public PhotoImageView getImageView() {
        return mImageView;
    }

    public void setAnimateWhenChecked(boolean animate) {
        mAnimateCheck = animate;
    }

    public void setShowCheckbox(boolean visible) {
        if (visible) {
            mButton.setVisibility(View.VISIBLE);
            mButton.setOnClickListener(this);
        } else {
            mButton.setVisibility(View.GONE);
            mButton.setOnClickListener(null);
        }
    }

    public void onClick(View v) {
        if (null != mSelection) {

            // Toggle check to show new state
            toggle();

            // Update the controller
            if (isChecked()) {
                mController.addSelection(mSelection);
            } else {
                mController.removeSelection(mSelection);
            }

            // Show animate if we've been set to
            if (mAnimateCheck) {
                Animation anim = AnimationUtils
                        .loadAnimation(getContext(), isChecked() ? R.anim.photo_selection_added
                                : R.anim.photo_selection_removed);
                v.startAnimation(anim);
            }
        }
    }

    @Override
    public void setChecked(final boolean b) {
        super.setChecked(b);
        if (View.VISIBLE == mButton.getVisibility()) {
            mButton.setChecked(b);
        }
    }

    public Photo getPhotoSelection() {
        return mSelection;
    }

    public void setPhotoSelection(Photo selection) {
        if (mSelection != selection) {
            mButton.clearAnimation();
            mSelection = selection;
        }
    }

}
