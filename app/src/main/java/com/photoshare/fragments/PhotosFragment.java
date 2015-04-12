package com.photoshare.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.commonsware.cwac.merge.MergeAdapter;
import com.photoshare.Constants;
import com.photoshare.Flags;
import com.photoshare.PhotoController;
import com.photoshare.R;
import com.photoshare.activities.PhotoViewerActivity;
import com.photoshare.adapters.BucketAdapter;
import com.photoshare.adapters.CameraBaseAdapter;
import com.photoshare.adapters.PhotosCursorAdapter;
import com.photoshare.events.BucketEvent;
import com.photoshare.events.PhotoSelectionAddedEvent;
import com.photoshare.events.PhotoSelectionErrorEvent;
import com.photoshare.events.PhotoSelectionRemovedEvent;
import com.photoshare.model.MediaStoreBucket;
import com.photoshare.model.Photo;
import com.photoshare.tasks.MediaStoreBucketsAsyncTask;
import com.photoshare.util.MediaStoreCursorHelper;
import com.photoshare.util.PhotoCursorLoader;
import com.photoshare.util.Utils;
import com.photoshare.views.PhotoImageView;
import com.photoshare.views.PhotoItemLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 图片
 * Created by longjianlin on 15/3/19.
 */
public class PhotosFragment extends SherlockFragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, MediaStoreBucketsAsyncTask.MediaStoreBucketsResultListener,
        MediaScannerConnection.OnScanCompletedListener, View.OnClickListener {


    static class ScaleAnimationListener implements Animation.AnimationListener {

        private final PhotoImageView mAnimatedView;
        private final ViewGroup mParent;

        public ScaleAnimationListener(ViewGroup parent, PhotoImageView view) {
            mParent = parent;
            mAnimatedView = view;
        }

        public void onAnimationEnd(Animation animation) {
            mAnimatedView.setVisibility(View.GONE);
            mParent.post(new Runnable() {
                public void run() {
                    mParent.removeView(mAnimatedView);
                    mAnimatedView.recycleBitmap();
                }
            });
        }

        public void onAnimationRepeat(Animation animation) {
            // NO-OP
        }

        public void onAnimationStart(Animation animation) {
            // NO-OP
        }
    }

    public static final String PREF_SELECTED_MEDIA_BUCKET_ID = "selected_media_store_bucket";
    static final int RESULT_CAMERA = 101;
    static final String SAVE_PHOTO_URI = "camera_photo_uri";

    static final String LOADER_PHOTOS_BUCKETS_PARAM = "bucket_id";
    static final int LOADER_USER_PHOTOS_EXTERNAL = 0x01;
    private DisplayMetrics dm;
    private MergeAdapter mAdapter;
    private PhotosCursorAdapter mPhotoAdapter;
    private GridView mPhotoGrid;

    private final ArrayList<MediaStoreBucket> mBuckets = new ArrayList<MediaStoreBucket>();
    private PhotoController mPhotoSelectionController;
    private File mPhotoFile;
    private SharedPreferences mPrefs;

    private PopupWindow popupWindow;
    private View popupWindowView;
    private ListView lv_bucket;
    private BucketAdapter bucketAdapter;
    private Button btn_cancel;

    private View view;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (null != savedInstanceState) {
            if (savedInstanceState.containsKey(SAVE_PHOTO_URI)) {
                mPhotoFile = new File(savedInstanceState.getString(SAVE_PHOTO_URI));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_CAMERA:
                if (null != mPhotoFile) {
                    if (resultCode == Activity.RESULT_OK) {
                        Utils.scanMediaJpegFile(getActivity(), mPhotoFile, this);
                    } else {
                        if (Flags.DEBUG) {
                            Log.d("UserPhotosFragment", "Deleting Photo File");
                        }
                        mPhotoFile.delete();
                    }
                    mPhotoFile = null;
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Activity activity) {
        mPhotoSelectionController = PhotoController.getFromContext(activity);
        super.onAttach(activity);
    }

    public void onBucketsLoaded(final List<MediaStoreBucket> buckets) {
        if (null != buckets && !buckets.isEmpty()) {
            mBuckets.clear();
            mBuckets.addAll(buckets);
            bucketAdapter.notifyDataSetChanged();
            loadBucketId(getSelectedBucketFromPrefs());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dm = getResources().getDisplayMetrics();        //获取屏幕分辨率
        mAdapter = new MergeAdapter();
        if (Utils.hasCamera(getActivity())) {
            mAdapter.addAdapter(new CameraBaseAdapter(getActivity()));
        }
        mPhotoAdapter = new PhotosCursorAdapter(getActivity(), null);
        mAdapter.addAdapter(mPhotoAdapter);
        bucketAdapter = new BucketAdapter(getActivity(), mBuckets);
        EventBus.getDefault().register(this);
    }


    /**
     * 加载所有图片
     *
     * @param id
     * @param bundle
     * @return
     */
    public Loader<Cursor> onCreateLoader(final int id, Bundle bundle) {
        CursorLoader cursorLoader = null;
        switch (id) {
            case LOADER_USER_PHOTOS_EXTERNAL:
                String selection = null;
                String[] selectionArgs = null;

                if (null != bundle && bundle.containsKey(LOADER_PHOTOS_BUCKETS_PARAM)) {
                    selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
                    selectionArgs = new String[]{bundle.getString(LOADER_PHOTOS_BUCKETS_PARAM)};
                }
                //保存选择的bucket_id
                setSelectedBucketToPrefs(bundle.getString(LOADER_PHOTOS_BUCKETS_PARAM));
                cursorLoader = new PhotoCursorLoader(getActivity(),
                        MediaStoreCursorHelper.MEDIA_STORE_CONTENT_URI,
                        MediaStoreCursorHelper.PHOTOS_PROJECTION, selection, selectionArgs,
                        MediaStoreCursorHelper.PHOTOS_ORDER_BY, false);
                break;
        }
        return cursorLoader;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_photos, null);
        mPhotoGrid = (GridView) view.findViewById(R.id.gv_photos);
        mPhotoGrid.setAdapter(mAdapter);
        mPhotoGrid.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Load buckets
        MediaStoreBucketsAsyncTask.execute(getActivity(), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(getActivity(), "onResume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onItemClick(AdapterView<?> gridView, View view, int position, long id) {
        if (view.getId() == R.id.iv_camera_button) {
            takePhoto();
        } else if (view.getId() == R.id.item_bucket) {
            MediaStoreBucket item = bucketAdapter.getItem(position);
            if (null != item) {
                loadBucketId(item.getId());
            }
            dismissPopupWindow();
        } else {
            Bundle b = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeThumbnailScaleUpAnimation(view,
                                Utils.drawViewOntoBitmap(view), 0, 0);
                b = options.toBundle();
            }

            Intent intent = new Intent(getActivity(), PhotoViewerActivity.class);
            // Need take Camera icon into account so minus 1
            intent.putExtra(Constants.EXTRA_POSITION, position - 1);
            intent.putExtra(Constants.EXTRA_MODE, PhotoViewerActivity.MODE_ALL_VALUE);

            intent.putExtra(Constants.EXTRA_BUCKET_ID, getSelectedBucketFromPrefs());
            ActivityCompat.startActivity(getActivity(), intent, b);
        }
    }


    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_USER_PHOTOS_EXTERNAL:
                mPhotoAdapter.swapCursor(null);
                break;
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_USER_PHOTOS_EXTERNAL:
                mPhotoAdapter.swapCursor(data);
                mPhotoGrid.setSelection(0);
                break;
        }
    }


    public void onNothingSelected(AdapterView<?> view) {
        // NO-OP
    }

    private void updateUploadView(Photo upload, boolean added) {
        for (int i = 0, z = mPhotoGrid.getChildCount(); i < z; i++) {
            View view = mPhotoGrid.getChildAt(i);

            if (view instanceof PhotoItemLayout) {
                PhotoItemLayout layout = (PhotoItemLayout) view;
                if (upload.equals(layout.getPhotoSelection())) {
                    if (Flags.DEBUG) {
                        Log.d("PhotosFragment", "Found View, setChecked");
                    }
                    layout.setChecked(added);
                    break;
                }
            }
        }
    }


    /**
     * 添加
     *
     * @param event
     */
    public void onEvent(PhotoSelectionAddedEvent event) {
        if (event.isSingleChange()) {
            updateUploadView(event.getTarget(), true);
        } else {
            mPhotoAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 移除
     *
     * @param event
     */

    public void onEvent(PhotoSelectionRemovedEvent event) {
        if (event.isSingleChange()) {
            updateUploadView(event.getTarget(), false);
        } else {
            mPhotoAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 添加图片异常(最多只能添加9张图片)
     *
     * @param event
     */
    public void onEvent(PhotoSelectionErrorEvent event) {
        Toast.makeText(getActivity(), R.string.choose_limit_nine, Toast.LENGTH_SHORT).show();
        mPhotoAdapter.notifyDataSetChanged();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (null != mPhotoFile) {
            outState.putString(SAVE_PHOTO_URI, mPhotoFile.getAbsolutePath());
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * 拍照完成后执行
     *
     * @param path
     * @param uri
     */
    public void onScanCompleted(String path, Uri uri) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                loadBucketId(getSelectedBucketFromPrefs());
            }
        });
    }

    private void loadBucketId(String id) {
        if (isAdded()) {
            Bundle bundle = new Bundle();
            if (null != id) {
                bundle.putString(LOADER_PHOTOS_BUCKETS_PARAM, id);
            }
            try {
                getLoaderManager().restartLoader(LOADER_USER_PHOTOS_EXTERNAL, bundle, this);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                // Can sometimes catch with: Fragment not attached to Activity.
                // Not much we can do to recover
            }
        }
    }

    private void setSelectedBucketToPrefs(String bucket_id) {
        if (null != mPrefs) {
            mPrefs.edit().putString(Constants.PREF_SELECTED_MEDIA_BUCKET_ID, bucket_id).commit();
        }
    }

    private String getSelectedBucketFromPrefs() {
        if (null != mPrefs) {
            return mPrefs.getString(PREF_SELECTED_MEDIA_BUCKET_ID, null);
        }
        return null;
    }

    /**
     * 照相
     */
    private void takePhoto() {
        if (null == mPhotoFile) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mPhotoFile = Utils.getCameraPhotoFile();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
            startActivityForResult(takePictureIntent, RESULT_CAMERA);
        }
    }

    /**
     * 分享弹出框
     */
    private void showPopupWindow() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        popupWindowView = inflater.inflate(R.layout.fragment_popupwindow_bucket, null);
        popupWindow = new PopupWindow(popupWindowView, ViewGroup.LayoutParams.MATCH_PARENT, dm.heightPixels / 2 + 300, true);
        ColorDrawable cd = new ColorDrawable(0x000000);
        popupWindow.setBackgroundDrawable(cd);

        //产生背景变暗效果
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = 0.4f;
        getActivity().getWindow().setAttributes(lp);

        //设置PopupWindow的弹出和消失效果
        popupWindow.setAnimationStyle(R.style.popupAnimation);
        popupWindow.update();
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            //在dismiss中恢复透明度
            public void onDismiss() {
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 1f;
                getActivity().getWindow().setAttributes(lp);
            }
        });

        btn_cancel = (Button) popupWindowView.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);
        lv_bucket = (ListView) popupWindowView.findViewById(R.id.lv_bucket);
        lv_bucket.setAdapter(bucketAdapter);
        lv_bucket.setOnItemClickListener(this);
        popupWindow.showAtLocation(lv_bucket, Gravity.BOTTOM, 0, 0);
    }

    /**
     * 销毁PopupWindow
     */
    private void dismissPopupWindow() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btn_cancel.getId()) {
            dismissPopupWindow();
        }
    }

    public void onEvent(BucketEvent event) {
        showPopupWindow();
    }
}