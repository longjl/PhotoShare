package com.photoshare.model;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.lightbox.android.photoprocessing.PhotoProcessing;
import com.lightbox.android.photoprocessing.utils.BitmapUtils;
import com.photoshare.Constants;
import com.photoshare.Flags;
import com.photoshare.PhotoApplication;
import com.photoshare.R;
import com.photoshare.events.UploadStateChangedEvent;
import com.photoshare.events.UploadsModifiedEvent;
import com.photoshare.listeners.OnFaceDetectionListener;
import com.photoshare.listeners.OnPhotoTagsChangedListener;
import com.photoshare.util.Utils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 图片 Model
 * Created by longjianlin on 15/3/19.
 */
@DatabaseTable(tableName = "photo")
public class Photo {
    static final String LOG_TAG = "Photo";
    private static final HashMap<Uri, Photo> SELECTION_CACHE = new HashMap<Uri, Photo>();

    public static final int STATE_UPLOAD_COMPLETED = 5;
    public static final int STATE_UPLOAD_ERROR = 4;
    public static final int STATE_UPLOAD_IN_PROGRESS = 3;
    public static final int STATE_UPLOAD_WAITING = 2;
    public static final int STATE_SELECTED = 1;
    public static final int STATE_NONE = 0;

    public static final String FIELD_STATE = "state";
    static final String FIELD_URI = "uri";
    static final String FIELD_COMPLETED_DETECTION = "tag_detection";
    static final String FIELD_USER_ROTATION = "user_rotation";
    static final String FIELD_FILTER = "filter";
    static final String FIELD_CROP_L = "crop_l";
    static final String FIELD_CROP_T = "crop_t";
    static final String FIELD_CROP_R = "crop_r";
    static final String FIELD_CROP_B = "crop_b";
    static final String FIELD_ACCOUNT_ID = "acc_id";          //用户编号
    static final String FIELD_TARGET_ID = "target_id";
    static final String FIELD_QUALITY = "quality";            //清晰度
    static final String FIELD_RESULT_POST_ID = "r_post_id";

    static final float CROP_THRESHOLD = 0.01f; // 1%
    static final int MINI_THUMBNAIL_SIZE = 300;
    static final int MICRO_THUMBNAIL_SIZE = 96;
    static final float MIN_CROP_VALUE = 0.0f;
    static final float MAX_CROP_VALUE = 1.0f;

    /**
     * Edit Variables
     */
    @DatabaseField(columnName = FIELD_COMPLETED_DETECTION)
    public boolean mCompletedDetection;
    @DatabaseField(columnName = FIELD_USER_ROTATION)
    public int mUserRotation;
    @DatabaseField(columnName = FIELD_FILTER)
    public Filter mFilter;
    @DatabaseField(columnName = FIELD_CROP_L)
    public float mCropLeft;
    @DatabaseField(columnName = FIELD_CROP_T)
    public float mCropTop;
    @DatabaseField(columnName = FIELD_CROP_R)
    public float mCropRight;
    @DatabaseField(columnName = FIELD_CROP_B)
    public float mCropBottom;

    /**
     * Upload Variables
     */
    @DatabaseField(columnName = FIELD_ACCOUNT_ID)
    public String mAccountId;
    @DatabaseField(columnName = FIELD_TARGET_ID)
    public String mTargetId;
    @DatabaseField(columnName = FIELD_QUALITY)
    public UploadQuality mQuality;
    @DatabaseField(columnName = FIELD_RESULT_POST_ID)
    public String mResultPostId;
    @DatabaseField(columnName = FIELD_STATE)
    public int mState;

    /**
     * Uri and Database Key
     */
    public Uri mFullUri;
    @DatabaseField(columnName = FIELD_URI, id = true)
    public String mFullUriString;


    private HashSet<PhotoTag> mTags;
    private Account mAccount;
    private int mProgress;
    private Bitmap mBigPictureNotificationBmp;
    private boolean mNeedsSaveFlag = false;

    /**
     * Listeners
     */
    private WeakReference<OnFaceDetectionListener> mFaceDetectListener;
    private WeakReference<OnPhotoTagsChangedListener> mTagChangedListener;


    public int getPhotoTagsCount() {
        return null != mTags ? mTags.size() : 0;
    }

    /**
     * 根据Uri获取Photo
     *
     * @param uri
     * @return
     */
    public static Photo getSelection(Uri uri) {
        Photo photo = SELECTION_CACHE.get(uri);
        if (photo == null) {
            photo = new Photo(uri);
            SELECTION_CACHE.put(uri, photo);
        }
        return photo;
    }

    public static Photo getSelection(Uri baseUri, long id) {
        return getSelection(Uri.withAppendedPath(baseUri, String.valueOf(id)));
    }

    /**
     * 清理图片缓存
     */
    public static void clearCache() {
        SELECTION_CACHE.clear();
    }

    public Photo() {

    }

    /**
     * 图片构造方法
     *
     * @param uri
     */
    public Photo(Uri uri) {
        mFullUri = uri;
        mFullUriString = uri.toString();
        reset();
    }

    /**
     * 重置图片
     */
    public void reset() {
        mState = STATE_NONE;
        mUserRotation = 0;
        mCropLeft = mCropTop = MIN_CROP_VALUE;
        mCropRight = mCropBottom = MAX_CROP_VALUE;
        mFilter = null;
        mTags = null;
        mCompletedDetection = false;

        setRequiresSaveFlag();
    }

    private void setRequiresSaveFlag() {
        mNeedsSaveFlag = true;
    }

    public void detectPhotoTags(final Bitmap originalBitmap) {
        // If we've already done Face detection, don't do it again...
        if (mCompletedDetection) {
            return;
        }

        final OnFaceDetectionListener listener = mFaceDetectListener.get();
        if (null != listener) {
            listener.onFaceDetectionStarted(this);
        }

        final int bitmapWidth = originalBitmap.getWidth();
        final int bitmapHeight = originalBitmap.getHeight();

        Bitmap bitmap = originalBitmap;

        // The Face detector only accepts 565 bitmaps, so create one if needed
        if (Bitmap.Config.RGB_565 != bitmap.getConfig()) {
            bitmap = originalBitmap.copy(Bitmap.Config.RGB_565, false);
        }

        final FaceDetector detector = new FaceDetector(bitmapWidth, bitmapHeight,
                Constants.FACE_DETECTOR_MAX_FACES);
        final FaceDetector.Face[] faces = new FaceDetector.Face[Constants.FACE_DETECTOR_MAX_FACES];
        final int detectedFaces = detector.findFaces(bitmap, faces);

        // We must have created a converted 565 bitmap
        if (bitmap != originalBitmap) {
            bitmap.recycle();
            bitmap = null;
        }

        FaceDetector.Face face;
        final PointF point = new PointF();
        for (int i = 0, z = faces.length; i < z; i++) {
            face = faces[i];
            if (null != face) {
                face.getMidPoint(point);
                addPhotoTag(new PhotoTag(point.x, point.y, bitmapWidth, bitmapWidth));
            }
        }

        if (null != listener) {
            listener.onFaceDetectionFinished(this);
        }
        mFaceDetectListener = null;
        mCompletedDetection = true;
    }

    public void addPhotoTag(PhotoTag tag) {
        if (null == mTags) {
            mTags = new HashSet<PhotoTag>();
        }
        mTags.add(tag);
        notifyTagListener(tag, true);

        setRequiresSaveFlag();
    }

    private void notifyTagListener(PhotoTag tag, boolean added) {
        if (null != mTagChangedListener) {
            OnPhotoTagsChangedListener listener = mTagChangedListener.get();
            if (null != listener) {
                listener.onPhotoTagsChanged(tag, added);
            }
        }
    }


    /**
     * 获取Bitmap
     *
     * @param context
     * @return
     */
    public Bitmap getDisplayImage(Context context) {
        try {
            final int size = PhotoApplication.getApplication(context).getSmallestScreenDimension();
            Bitmap bitmap = Utils
                    .decodeImage(context.getContentResolver(), getOriginalPhotoUri(), size);
            bitmap = Utils.rotate(bitmap, getExifRotation(context));
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getDisplayImageKey() {
        return "dsply_" + getOriginalPhotoUri();
    }

    public Uri getOriginalPhotoUri() {
        if (null == mFullUri && !TextUtils.isEmpty(mFullUriString)) {
            mFullUri = Uri.parse(mFullUriString);
        }
        return mFullUri;
    }

    public String getThumbnailImageKey() {
        return "thumb_" + getOriginalPhotoUri();
    }

    public int getExifRotation(Context context) {
        return Utils
                .getOrientationFromContentUri(context.getContentResolver(), getOriginalPhotoUri());
    }

    public Bitmap getThumbnailImage(Context context) {
        if (ContentResolver.SCHEME_CONTENT.equals(getOriginalPhotoUri().getScheme())) {
          return getThumbnailImageFromMediaStore(context);
        }

        final Resources res = context.getResources();
        int size = res.getBoolean(R.bool.load_mini_thumbnails) ? MINI_THUMBNAIL_SIZE : MICRO_THUMBNAIL_SIZE;
        if (size == MINI_THUMBNAIL_SIZE && res.getBoolean(R.bool.sample_mini_thumbnails)) {
            size /= 2;
        }
        try {
            Bitmap bitmap = Utils
                    .decodeImage(context.getContentResolver(), getOriginalPhotoUri(), size);
            bitmap = Utils.rotate(bitmap, getExifRotation(context));
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getThumbnailImageFromMediaStore(Context context) {
        Resources res = context.getResources();

        final int kind = res.getBoolean(R.bool.load_mini_thumbnails) ? MediaStore.Images.Thumbnails.MINI_KIND
                : MediaStore.Images.Thumbnails.MICRO_KIND;

        BitmapFactory.Options opts = null;
        if (kind == MediaStore.Images.Thumbnails.MINI_KIND && res.getBoolean(R.bool.sample_mini_thumbnails)) {
            opts = new BitmapFactory.Options();
            opts.inSampleSize = 2;
        }

        try {
            final long id = Long.parseLong(getOriginalPhotoUri().getLastPathSegment());

            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, kind, opts);
            bitmap = Utils.rotate(bitmap, getExifRotation(context));
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public Bitmap processBitmap(Bitmap bitmap, final boolean fullSize,
                                final boolean modifyOriginal) {
        if (requiresProcessing(fullSize)) {
            return processBitmapUsingFilter(bitmap, mFilter, fullSize, modifyOriginal);
        } else {
            return bitmap;
        }
    }


    public boolean requiresProcessing(final boolean fullSize) {
        return getUserRotation() != 0 || beenFiltered() || (fullSize && beenCropped());
    }

    public Bitmap processBitmapUsingFilter(final Bitmap bitmap, final Filter filter,
                                           final boolean fullSize,
                                           final boolean modifyOriginal) {
        //Utils.checkPhotoProcessingThread();

        PhotoProcessing.sendBitmapToNative(bitmap);
        if (modifyOriginal) {
            bitmap.recycle();
        }

        if (fullSize && beenCropped()) {
            RectF rect = getCropValues();
            PhotoProcessing.nativeCrop(rect.left, rect.top, rect.right, rect.bottom);
        }

        if (null != filter) {
            PhotoProcessing.filterPhoto(filter.getId());
        }

        switch (getUserRotation()) {
            case 90:
                PhotoProcessing.nativeRotate90();
                break;
            case 180:
                PhotoProcessing.nativeRotate180();
                break;
            case 270:
                PhotoProcessing.nativeRotate180();
                PhotoProcessing.nativeRotate90();
                break;
        }

        return PhotoProcessing.getBitmapFromNative(null);
    }

    public int getUserRotation() {
        return mUserRotation % 360;
    }


    public boolean beenFiltered() {
        return null != mFilter && mFilter != Filter.ORIGINAL;
    }

    public boolean beenCropped() {
        return checkCropValues(mCropLeft, mCropTop, mCropRight, mCropBottom);
    }

    private static float santizeCropValue(float value) {
        return Math.min(1f, Math.max(0f, value));
    }

    private static boolean checkCropValues(float left, float top, float right, float bottom) {
        return Math.max(left, top) >= (MIN_CROP_VALUE + CROP_THRESHOLD)
                || Math.min(right, bottom) <= (MAX_CROP_VALUE - CROP_THRESHOLD);
    }

    public RectF getCropValues() {
        return new RectF(mCropLeft, mCropTop, mCropRight, mCropBottom);
    }

    public RectF getCropValues(final int width, final int height) {
        return new RectF(mCropLeft * width, mCropTop * height, mCropRight * width,
                mCropBottom * height);
    }

    public boolean requiresSaving() {
        return mNeedsSaveFlag;
    }

    public void resetSaveFlag() {
        mNeedsSaveFlag = false;
    }

    public static void populateCache(List<Photo> uploads) {
        for (Photo upload : uploads) {
            SELECTION_CACHE.put(upload.getOriginalPhotoUri(), upload);
        }
    }

    public boolean isValid(Context context) {
        final String path = Utils.getPathFromContentUri(context.getContentResolver(), getOriginalPhotoUri());
        if (null != path) {
            File file = new File(path);
            return file.exists();
        }
        return false;
    }

    public void setUploadState(final int state) {
        if (mState != state) {
            mState = state;

            switch (state) {
                case STATE_UPLOAD_ERROR:
                case STATE_UPLOAD_COMPLETED:
                    mBigPictureNotificationBmp = null;
                    EventBus.getDefault().post(new UploadsModifiedEvent());
                    break;
                case STATE_SELECTED:
                case STATE_UPLOAD_WAITING:
                    mProgress = -1;
                    break;
            }

            notifyUploadStateListener();
            setRequiresSaveFlag();
        }
    }

    private void notifyUploadStateListener() {
        EventBus.getDefault().post(new UploadStateChangedEvent(this));
    }

    public UploadQuality getUploadQuality() {
        return null != mQuality ? mQuality : UploadQuality.MEDIUM;
    }

    public boolean requiresNativeEditing(Context context) {
        return beenCropped() || beenFiltered() || getTotalRotation(context) != 0;
    }

    public int getTotalRotation(Context context) {
        return (getExifRotation(context) + getUserRotation()) % 360;
    }

    public File getUploadSaveFile() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "photo");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, System.currentTimeMillis() + ".jpg");
    }


    public Bitmap getUploadImage(Context context, final UploadQuality quality) {
        return getUploadImageNative(context, quality);
    }

    private Bitmap getUploadImageNative(final Context context, final UploadQuality quality) {
        Utils.checkPhotoProcessingThread();
        try {
            String path = Utils
                    .getPathFromContentUri(context.getContentResolver(), getOriginalPhotoUri());
            if (null != path) {
                BitmapUtils.BitmapSize size = BitmapUtils.getBitmapSize(path);

                if (quality.requiresResizing()) {
                    final float resizeRatio = Math.max(size.width, size.height) / (float) quality
                            .getMaxDimension();
                    size = new BitmapUtils.BitmapSize(Math.round(size.width / resizeRatio),
                            Math.round(size.height / resizeRatio));
                }

                boolean doAndroidDecode = true;

                if (Flags.USE_INTERNAL_DECODER) {
                    doAndroidDecode =
                            PhotoProcessing.nativeLoadResizedBitmap(path, size.width * size.height)
                                    != 0;

                    if (Flags.DEBUG) {
                        if (doAndroidDecode) {
                            Log.d("MediaStorePhotoUpload",
                                    "getUploadImage. Native decode failed :(");
                        } else {
                            Log.d("MediaStorePhotoUpload",
                                    "getUploadImage. Native decode complete!");
                        }
                    }
                }

                if (doAndroidDecode) {
                    if (Flags.DEBUG) {
                        Log.d("MediaStorePhotoUpload", "getUploadImage. Doing Android decode");
                    }

                    // Just in case
                    PhotoProcessing.nativeDeleteBitmap();

                    // Decode in Android and send to native
                    Bitmap bitmap = Utils
                            .decodeImage(context.getContentResolver(), getOriginalPhotoUri(),
                                    quality.getMaxDimension());

                    if (null != bitmap) {
                        PhotoProcessing.sendBitmapToNative(bitmap);
                        bitmap.recycle();

                        // Resize image to correct size
                        PhotoProcessing.nativeResizeBitmap(size.width, size.height);
                    } else {
                        return null;
                    }
                }

                /**
                 * Apply crop if needed
                 */
                if (beenCropped()) {
                    RectF rect = getCropValues();
                    PhotoProcessing.nativeCrop(rect.left, rect.top, rect.right, rect.bottom);
                }

                /**
                 * Apply filter if needed
                 */
                if (beenFiltered()) {
                    PhotoProcessing.filterPhoto(getFilterUsed().getId());
                    if (Flags.DEBUG) {
                        Log.d("MediaStorePhotoUpload", "getUploadImage. Native filter complete!");
                    }
                }

                /**
                 * Rotate if needed
                 */
                final int rotation = getTotalRotation(context);
                switch (rotation) {
                    case 90:
                        PhotoProcessing.nativeRotate90();
                        break;
                    case 180:
                        PhotoProcessing.nativeRotate180();
                        break;
                    case 270:
                        PhotoProcessing.nativeRotate180();
                        PhotoProcessing.nativeRotate90();
                        break;
                }
                if (Flags.DEBUG) {
                    Log.d("MediaStorePhotoUpload",
                            "getUploadImage. " + rotation + " degree rotation complete!");
                }

                if (Flags.DEBUG) {
                    Log.d("MediaStorePhotoUpload", "getUploadImage. Native worked!");
                }

                return PhotoProcessing.getBitmapFromNative(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Just in case...
            PhotoProcessing.nativeDeleteBitmap();
        }

        return null;
    }

    public Filter getFilterUsed() {
        if (null == mFilter) {
            mFilter = Filter.ORIGINAL;
        }
        return mFilter;
    }

    public int getUploadState() {
        return mState;
    }

    public void setTagChangedListener(OnPhotoTagsChangedListener tagChangedListener) {
        mTagChangedListener = new WeakReference<OnPhotoTagsChangedListener>(tagChangedListener);
    }

    public List<PhotoTag> getPhotoTags() {
        if (null != mTags) {
            return new ArrayList<PhotoTag>(mTags);
        }
        return null;
    }

    public void removePhotoTag(PhotoTag tag) {
        if (null != mTags) {
            mTags.remove(tag);
            notifyTagListener(tag, false);

            if (mTags.isEmpty()) {
                mTags = null;
            }
        }
        setRequiresSaveFlag();
    }

    public void rotateClockwise() {
        mUserRotation += 90;
        setRequiresSaveFlag();
    }

    public void setFilterUsed(Filter filter) {
        mFilter = filter;
        setRequiresSaveFlag();
    }

    public boolean requiresFaceDetectPass() {
        return !mCompletedDetection;
    }


    public void setFaceDetectionListener(OnFaceDetectionListener listener) {
        // No point keeping listener if we've already done a pass
        if (!mCompletedDetection) {
            mFaceDetectListener = new WeakReference<OnFaceDetectionListener>(listener);
        }
    }

    public void setCropValues(RectF cropValues) {
        if (checkCropValues(cropValues.left, cropValues.top, cropValues.right, cropValues.bottom)) {

            mCropLeft = santizeCropValue(cropValues.left);
            mCropTop = santizeCropValue(cropValues.top);
            mCropRight = santizeCropValue(cropValues.right);
            mCropBottom = santizeCropValue(cropValues.bottom);
            if (Flags.DEBUG) {
                Log.d(LOG_TAG, "Valid Crop Values: " + cropValues.toString());
            }
        } else {
            if (Flags.DEBUG) {
                Log.d(LOG_TAG, "Invalid Crop Values: " + cropValues.toString());
            }
            mCropLeft = mCropTop = MIN_CROP_VALUE;
            mCropRight = mCropBottom = MAX_CROP_VALUE;
        }

        setRequiresSaveFlag();
    }
}
