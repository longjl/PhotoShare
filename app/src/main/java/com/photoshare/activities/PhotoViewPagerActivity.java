package com.photoshare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewTreeObserver;

import com.actionbarsherlock.view.MenuItem;
import com.photoshare.Constants;
import com.photoshare.R;
import com.photoshare.adapters.PhotoViewPagerAdapter;
import com.photoshare.base.PhotoFragmentActivity;
import com.photoshare.model.History;
import com.photoshare.model.Photo;
import com.photoshare.model.Record;
import com.photoshare.tasks.RecordAsyncTask;
import com.photoshare.views.PhotoTagItemLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longjianlin on 15/4/11.
 */
public class PhotoViewPagerActivity extends PhotoFragmentActivity implements ViewPager.OnPageChangeListener, RecordAsyncTask.RecordResultListener {
    private ViewPager mViewPager;
    private PagerAdapter mAdapter;


    private int mRequestedPosition = -1;
    private Intent intent;

    private List<Photo> photos = new ArrayList<Photo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_viewer);

        mViewPager = (ViewPager) findViewById(R.id.vp_photos);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.viewpager_margin));
        mViewPager.setOnPageChangeListener(this);

        mAdapter = new PhotoViewPagerAdapter(this, photos);
        mViewPager.setAdapter(mAdapter);
        intent = getIntent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        /**
         * Nasty hack, basically we need to know when the ViewPager is laid out,
         * we then manually call onPageSelected. This is to fix onPageSelected
         * not being called on the first item.
         */
        mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                mViewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                onPageSelected(mViewPager.getCurrentItem());
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (intent.hasExtra(Constants.RECORD_ID)) {
            RecordAsyncTask.execute(this, this, intent.getIntExtra(Constants.RECORD_ID, -1));
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        PhotoTagItemLayout currentView = getCurrentView();
        if (null != currentView) {
            Photo upload = currentView.getPhotoSelection();

            if (null != upload) {
                // Request Face Detection
                currentView.getImageView().postFaceDetection(upload);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state != ViewPager.SCROLL_STATE_IDLE) {
            clearFaceDetectionPasses();
        }
    }

    private void clearFaceDetectionPasses() {
        for (int i = 0, z = mViewPager.getChildCount(); i < z; i++) {
            PhotoTagItemLayout child = (PhotoTagItemLayout) mViewPager.getChildAt(i);
            if (null != child) {
                child.getImageView().clearFaceDetection();
            }
        }
    }

    private PhotoTagItemLayout getCurrentView() {
        final int currentPos = mViewPager.getCurrentItem();

        for (int i = 0, z = mViewPager.getChildCount(); i < z; i++) {
            PhotoTagItemLayout child = (PhotoTagItemLayout) mViewPager.getChildAt(i);
            if (null != child && child.getPosition() == currentPos) {
                return child;
            }
        }
        return null;
    }


    @Override
    public void onRecordLoaded(Record record) {
        if (record != null) {
            for (History history : record.histories) {
                Photo photo = new Photo();
                photo.mCompletedDetection = history.mCompletedDetection;
                photo.mUserRotation = history.mUserRotation;
                photo.mFilter = history.mFilter;
                photo.mCropLeft = history.mCropLeft;
                photo.mCropTop = history.mCropTop;
                photo.mCropRight = history.mCropRight;
                photo.mCropBottom = history.mCropBottom;
                photo.mAccountId = history.mAccountId;
                photo.mTargetId = history.mTargetId;
                photo.mQuality = history.mQuality;
                photo.mResultPostId = history.mResultPostId;
                photo.mState = history.mState;
                photo.mFullUriString = history.mFullUriString;
                photos.add(photo);
            }
            mAdapter.notifyDataSetChanged();
            if (intent.hasExtra(Constants.EXTRA_POSITION)) {
                mRequestedPosition = intent.getIntExtra(Constants.EXTRA_POSITION, 0);
                mViewPager.setCurrentItem(mRequestedPosition);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
