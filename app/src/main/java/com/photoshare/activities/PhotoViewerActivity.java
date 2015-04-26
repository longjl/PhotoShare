package com.photoshare.activities;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.photoshare.Constants;
import com.photoshare.PhotoController;
import com.photoshare.R;
import com.photoshare.adapters.PhotosViewPagerAdapter;
import com.photoshare.adapters.SelectedPhotosViewPagerAdapter;
import com.photoshare.base.PhotoFragmentActivity;
import com.photoshare.events.PhotoSelectionErrorEvent;
import com.photoshare.events.PhotoSelectionRemovedEvent;
import com.photoshare.listeners.OnSingleTapListener;
import com.photoshare.model.Filter;
import com.photoshare.model.Photo;
import com.photoshare.util.Analytics;
import com.photoshare.util.CursorPagerAdapter;
import com.photoshare.util.MediaStoreCursorHelper;
import com.photoshare.util.PhotoCursorLoader;
import com.photoshare.views.FiltersRadioGroup;
import com.photoshare.views.MultiTouchImageView;
import com.photoshare.views.PhotoTagItemLayout;

import de.greenrobot.event.EventBus;

/**
 * Created by longjianlin on 15/3/25.
 */
public class PhotoViewerActivity extends PhotoFragmentActivity implements OnSingleTapListener,
        OnCheckedChangeListener, OnPageChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {


    public static int MODE_ALL_VALUE = 100;
    public static int MODE_SELECTED_VALUE = 101;

    static final int REQUEST_CROP_PHOTO = 200;

    class PhotoRemoveAnimListener implements AnimationListener {

        private final View mView;

        public PhotoRemoveAnimListener(View view) {
            mView = view;
        }

        public void onAnimationEnd(Animation animation) {
            mView.setVisibility(View.GONE);
            animation.setAnimationListener(null);

            if (!mController.hasSelections()) {
                finish();
            } else {
                View view = (View) mView.getParent();
                view.post(new Runnable() {
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }

    }

    private ViewPager mViewPager;
    private PagerAdapter mAdapter;
    private ViewGroup mContentView;
    private FiltersRadioGroup mFilterGroup;

    private Animation mFadeOutAnimation;
    private PhotoController mController;

    private boolean mIgnoreFilterCheckCallback = false;

    private int mMode = MODE_SELECTED_VALUE;
    private String mBucketId;
    private int mRequestedPosition = -1;

    @Override
    public void onBackPressed() {
        if (hideFiltersView()) {
            return;
        } else {
            super.onBackPressed();
        }
    }

    private void rotateCurrentPhoto() {
        PhotoTagItemLayout currentView = getCurrentView();
        Photo upload = currentView.getPhotoSelection();
        upload.rotateClockwise();
        reloadView(currentView);
    }

    private void resetCurrentPhoto() {
        PhotoTagItemLayout currentView = getCurrentView();
        Photo upload = currentView.getPhotoSelection();

        upload.reset();
        reloadView(currentView);
    }

    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (!mIgnoreFilterCheckCallback) {
            Filter filter = checkedId != -1 ? Filter.mapFromId(checkedId) : null;
            PhotoTagItemLayout currentView = getCurrentView();
            Photo upload = currentView.getPhotoSelection();

            upload.setFilterUsed(filter);
            reloadView(currentView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_photo_viewer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_filters:
                showFiltersView();
                Analytics.logEvent(Analytics.EVENT_PHOTO_FILTERS);
                return true;
            case R.id.menu_rotate:
                Analytics.logEvent(Analytics.EVENT_PHOTO_ROTATE);
                rotateCurrentPhoto();
                return true;
            case R.id.menu_crop:
                Analytics.logEvent(Analytics.EVENT_PHOTO_CROP);
                CropImageActivity.CROP_SELECTION = getCurrentUpload();
                startActivityForResult(new Intent(this, CropImageActivity.class),
                        REQUEST_CROP_PHOTO);
                return true;
            case R.id.menu_reset:
                Analytics.logEvent(Analytics.EVENT_PHOTO_RESET);
                resetCurrentPhoto();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onPageScrolled(int position, float arg1, int arg2) {
        // NO-OP
    }

    public void onPageScrollStateChanged(int state) {
        if (state != ViewPager.SCROLL_STATE_IDLE) {
            clearFaceDetectionPasses();
        }
    }

    public void onPageSelected(int position) {
        PhotoTagItemLayout currentView = getCurrentView();
        if (null != currentView) {
            Photo upload = currentView.getPhotoSelection();

            if (null != upload) {
                // Request Face Detection
                currentView.getImageView().postFaceDetection(upload);

                if (null != mFilterGroup && mFilterGroup.getVisibility() == View.VISIBLE) {
                    updateFiltersView();
                }
            }
        }
    }

    public boolean onSingleTap() {
        return hideFiltersView();
    }

    public void onEvent(PhotoSelectionRemovedEvent event) {
        if (event.isSingleChange()) {
            animatePhotoUploadOut(event.getTarget());
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 添加图片异常(最多只能添加9张图片)
     *
     * @param event
     */
    public void onEvent(PhotoSelectionErrorEvent event) {
        mAdapter.notifyDataSetChanged();
    }


    private void animatePhotoUploadOut(Photo upload) {
        if (mMode == MODE_SELECTED_VALUE) {
            PhotoTagItemLayout view = getCurrentView();

            if (upload.equals(view.getPhotoSelection())) {
                mFadeOutAnimation.setAnimationListener(new PhotoRemoveAnimListener(view));
                view.startAnimation(mFadeOutAnimation);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    reloadView(getCurrentView());
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_viewer);
        mContentView = (ViewGroup) findViewById(R.id.fl_root);

        mController = PhotoController.getFromContext(this);
        EventBus.getDefault().register(this);

        final Intent intent = getIntent();
        mMode = intent.getIntExtra(Constants.EXTRA_MODE, MODE_ALL_VALUE);

        if (mMode == MODE_ALL_VALUE) {
            mBucketId = intent.getStringExtra(Constants.EXTRA_BUCKET_ID);
        }

        mViewPager = (ViewPager) findViewById(R.id.vp_photos);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.viewpager_margin));
        mViewPager.setOnPageChangeListener(this);

        if (mMode == MODE_ALL_VALUE) {
            mAdapter = new PhotosViewPagerAdapter(this, this);
            getSupportLoaderManager().initLoader(0, null, this);
        } else {
            mAdapter = new SelectedPhotosViewPagerAdapter(this, this);
        }
        mViewPager.setAdapter(mAdapter);

        if (intent.hasExtra(Constants.EXTRA_POSITION)) {
            mRequestedPosition = intent.getIntExtra(Constants.EXTRA_POSITION, 0);
            mViewPager.setCurrentItem(mRequestedPosition);
        }

        mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.photo_fade_out);


        /**
         * Nasty hack, basically we need to know when the ViewPager is laid out,
         * we then manually call onPageSelected. This is to fix onPageSelected
         * not being called on the first item.
         */
        mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                mViewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                onPageSelected(mViewPager.getCurrentItem());
                showTapToTagPrompt();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mController.updateDatabase();
        super.onDestroy();
    }

    private Photo getCurrentUpload() {
        PhotoTagItemLayout view = getCurrentView();
        if (null != view) {
            return view.getPhotoSelection();
        }
        return null;
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

    private void reloadView(PhotoTagItemLayout currentView) {
        if (null != currentView) {
            MultiTouchImageView imageView = currentView.getImageView();
            Photo selection = currentView.getPhotoSelection();
            imageView.requestFullSize(selection, true, false, null);
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


    private boolean hideFiltersView() {
        if (null != mFilterGroup && mFilterGroup.isShowing()) {
            mFilterGroup.hide();
            getSupportActionBar().show();
            return true;
        }
        return false;
    }


    private void showFiltersView() {
        ActionBar ab = getSupportActionBar();
        if (ab.isShowing()) {
            ab.hide();
        }

        if (null == mFilterGroup) {
            View view = getLayoutInflater().inflate(R.layout.layout_filters, mContentView);
            mFilterGroup = (FiltersRadioGroup) view.findViewById(R.id.rg_filters);
            mFilterGroup.setOnCheckedChangeListener(this);
        }

        mFilterGroup.show();
        updateFiltersView();
    }

    private void showTapToTagPrompt() {
        // Toast.makeText(this, R.string.tag_friend_prompt, Toast.LENGTH_SHORT).show();
    }

    private void updateFiltersView() {
        mIgnoreFilterCheckCallback = true;
        mFilterGroup.setPhotoUpload(getCurrentUpload());
        mIgnoreFilterCheckCallback = false;
    }


    public void onPhotoLoadStatusChanged(boolean finished) {
        // TODO Fix this setProgressBarIndeterminateVisibility(!finished);
    }


    public Loader<Cursor> onCreateLoader(int id, Bundle params) {
        String selection = null;
        String[] selectionArgs = null;
        if (null != mBucketId) {
            selection = Images.Media.BUCKET_ID + " = ?";
            selectionArgs = new String[]{mBucketId};
        }

        return new PhotoCursorLoader(this, MediaStoreCursorHelper.MEDIA_STORE_CONTENT_URI,
                MediaStoreCursorHelper.PHOTOS_PROJECTION, selection, selectionArgs,
                MediaStoreCursorHelper.PHOTOS_ORDER_BY, false);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (mAdapter instanceof CursorPagerAdapter) {
            ((CursorPagerAdapter) mAdapter).swapCursor(cursor);
        }

        if (mRequestedPosition != -1) {
            mViewPager.setCurrentItem(mRequestedPosition, false);
            mRequestedPosition = -1;
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        onLoadFinished(loader, null);
    }
}
