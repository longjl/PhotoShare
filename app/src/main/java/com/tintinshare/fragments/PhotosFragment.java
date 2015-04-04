package com.tintinshare.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.commonsware.cwac.merge.MergeAdapter;
import com.tintinshare.Constants;
import com.tintinshare.Flags;
import com.tintinshare.PhotoController;
import com.tintinshare.R;
import com.tintinshare.activities.PhotoViewerActivity;
import com.tintinshare.adapters.CameraBaseAdapter;
import com.tintinshare.adapters.PhotosCursorAdapter;
import com.tintinshare.events.PhotoSelectionAddedEvent;
import com.tintinshare.events.PhotoSelectionErrorEvent;
import com.tintinshare.events.PhotoSelectionRemovedEvent;
import com.tintinshare.model.MediaStoreBucket;
import com.tintinshare.model.Photo;
import com.tintinshare.tasks.MediaStoreBucketsAsyncTask;
import com.tintinshare.util.MediaStoreCursorHelper;
import com.tintinshare.util.PhotoCursorLoader;
import com.tintinshare.util.Utils;
import com.tintinshare.views.PhotoImageView;
import com.tintinshare.views.PhotoItemLayout;

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
        AdapterView.OnItemSelectedListener,
        MediaScannerConnection.OnScanCompletedListener {

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

    static final int RESULT_CAMERA = 101;
    static final String SAVE_PHOTO_URI = "camera_photo_uri";

    static final String LOADER_PHOTOS_BUCKETS_PARAM = "bucket_id";
    static final int LOADER_USER_PHOTOS_EXTERNAL = 0x01;

    private MergeAdapter mAdapter;
    private PhotosCursorAdapter mPhotoAdapter;
    private GridView mPhotoGrid;

    private ArrayAdapter<MediaStoreBucket> mBucketAdapter;
    private Spinner mBucketSpinner;
    private final ArrayList<MediaStoreBucket> mBuckets = new ArrayList<MediaStoreBucket>();

    private TextView mCountTextView;//总数量
    private TextView mSelectedCountTextView;//已选

    private PhotoController mPhotoSelectionController;
    private File mPhotoFile;
    private SharedPreferences mPrefs;

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
            mBucketAdapter.notifyDataSetChanged();
            // setSelectedBucketFromPrefs();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAdapter = new MergeAdapter();
        if (Utils.hasCamera(getActivity())) {
            mAdapter.addAdapter(new CameraBaseAdapter(getActivity()));
        }
        mPhotoAdapter = new PhotosCursorAdapter(getActivity(), null);
        mAdapter.addAdapter(mPhotoAdapter);

        mBucketAdapter = new ArrayAdapter<MediaStoreBucket>(getActivity(),
                R.layout.layout_spinner_item, mBuckets);
        mBucketAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        EventBus.getDefault().register(this);
    }

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
        View view = inflater.inflate(R.layout.fragment_photos, null);

        mPhotoGrid = (GridView) view.findViewById(R.id.gv_photos);
        mPhotoGrid.setAdapter(mAdapter);
        mPhotoGrid.setOnItemClickListener(this);

        mBucketSpinner = (Spinner) view.findViewById(R.id.sp_buckets);
        mBucketSpinner.setOnItemSelectedListener(this);
        mBucketSpinner.setAdapter(mBucketAdapter);

        mCountTextView = (TextView) view.findViewById(R.id.tv_count);
        mSelectedCountTextView = (TextView) view.findViewById(R.id.tv_selected_count);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        saveSelectedBucketToPrefs();
    }

    public void onItemClick(AdapterView<?> gridView, View view, int position, long id) {
        if (view.getId() == R.id.iv_camera_button) {
            takePhoto();
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
            intent.putExtra(PhotoViewerActivity.EXTRA_POSITION, position - 1);
            intent.putExtra(PhotoViewerActivity.EXTRA_MODE, PhotoViewerActivity.MODE_ALL_VALUE);

            MediaStoreBucket bucket = (MediaStoreBucket) mBucketSpinner.getSelectedItem();
            intent.putExtra(PhotoViewerActivity.EXTRA_BUCKET_ID, bucket.getId());
            ActivityCompat.startActivity(getActivity(), intent, b);
        }
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        MediaStoreBucket item = (MediaStoreBucket) adapterView.getItemAtPosition(position);
        if (null != item) {
            loadBucketId(item.getId());
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
                setCountTextView(data.getCount());
                break;
        }
    }

    private void setCountTextView(final int count) {
        if (mCountTextView != null) {
            mCountTextView.setText(String.valueOf(count));
        }
    }

    private void setSelectedCountTextView(final int count) {
        if (mSelectedCountTextView != null) {
            mSelectedCountTextView.setText(String.valueOf(count));
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

        setSelectedCountTextView(mPhotoSelectionController.getSelectedCount());
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
        setSelectedCountTextView(mPhotoSelectionController.getSelectedCount());
    }

    /**
     * 添加图片异常(最多只能添加9张图片)
     *
     * @param event
     */
    public void onEvent(PhotoSelectionErrorEvent event) {
        Toast.makeText(getActivity(), "你最多只能添加9张图片", Toast.LENGTH_SHORT).show();
        mPhotoAdapter.notifyDataSetChanged();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (null != mPhotoFile) {
            outState.putString(SAVE_PHOTO_URI, mPhotoFile.getAbsolutePath());
        }
        super.onSaveInstanceState(outState);
    }

    public void onScanCompleted(String path, Uri uri) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                MediaStoreBucket bucket = getSelectedBucket();
                if (null != bucket) {
                    loadBucketId(bucket.getId());
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Load buckets
        MediaStoreBucketsAsyncTask.execute(getActivity(), this);
    }

    public void selectAll() {
        Cursor cursor = mPhotoAdapter.getCursor();
        if (null != cursor) {
            ArrayList<Photo> selections = MediaStoreCursorHelper.photosCursorToSelectionList(
                    MediaStoreCursorHelper.MEDIA_STORE_CONTENT_URI, cursor);
            mPhotoSelectionController.addSelections(selections);
        }
    }

    private MediaStoreBucket getSelectedBucket() {
        if (null != mBucketSpinner) {
            return (MediaStoreBucket) mBucketSpinner.getSelectedItem();
        }
        return null;
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

    private void saveSelectedBucketToPrefs() {
        MediaStoreBucket bucket = getSelectedBucket();
        if (null != bucket && null != mPrefs) {
            mPrefs.edit()
                    .putString(Constants.PREF_SELECTED_MEDIA_BUCKET_ID, bucket.getId())
                    .commit();
        }
    }

    private void setSelectedBucketFromPrefs() {
        if (null != mBucketSpinner) {
            int newSelection = 0;

            if (null != mPrefs) {
                final String lastBucketId = mPrefs
                        .getString(Constants.PREF_SELECTED_MEDIA_BUCKET_ID, null);
                if (null != lastBucketId) {
                    for (int i = 0, z = mBuckets.size(); i < z; i++) {
                        if (lastBucketId.equals(mBuckets.get(i).getId())) {
                            newSelection = i;
                            break;
                        }
                    }
                }
            }

            // If we have a new position, then just set it. If it's our current
            // position, then call onItemSelected manually
            if (newSelection != mBucketSpinner.getSelectedItemPosition()) {
                mBucketSpinner.setSelection(newSelection);
            } else {
                onItemSelected(mBucketSpinner, null, newSelection, 0);
            }
        }
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
}