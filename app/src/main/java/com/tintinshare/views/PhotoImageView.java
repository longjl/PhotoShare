package com.tintinshare.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.tintinshare.PhotoApplication;
import com.tintinshare.model.Photo;
import com.tintinshare.tasks.PhotoThreadRunnable;

import java.lang.ref.WeakReference;
import java.util.concurrent.Future;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapWrapper;
import uk.co.senab.bitmapcache.CacheableImageView;

/**
 * Created by longjianlin on 15/3/19.
 */
public class PhotoImageView extends CacheableImageView {
    private Future<?> mCurrentRunnable;
    private boolean mFadeInDrawables = false;
    private Drawable mFadeFromDrawable;
    private int mFadeDuration;

    /**
     * 图片加载 Listener
     */
    public static interface OnPhotoLoadListener {
        /**
         * 图片加载完成
         *
         * @param bitmap
         */
        void onPhotoLoadFinished(Bitmap bitmap);
    }

    public PhotoImageView(Context context) {
        super(context);
    }

    public PhotoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    static final class FaceDetectionRunnable extends PhotoThreadRunnable {

        private final Photo mPhoto;
        private final CacheableBitmapWrapper mBitmapWrapper;

        public FaceDetectionRunnable(Photo photo, CacheableBitmapWrapper bitmap) {
            mPhoto = photo;
            mBitmapWrapper = bitmap;
        }

        public void runImpl() {
            if (mBitmapWrapper.hasValidBitmap()) {
                mPhoto.detectPhotoTags(mBitmapWrapper.getBitmap());
            }
            mBitmapWrapper.setBeingUsed(false);
        }
    }

    static final class PhotoFilterRunnable extends PhotoThreadRunnable {

        private final WeakReference<PhotoImageView> mImageView;
        private final Photo mPhoto;
        private final boolean mFullSize;
        private final BitmapLruCache mCache;
        private final OnPhotoLoadListener mListener;

        public PhotoFilterRunnable(PhotoImageView imageView, Photo photo,
                                   BitmapLruCache cache,
                                   final boolean fullSize, final OnPhotoLoadListener listener) {
            mImageView = new WeakReference<PhotoImageView>(imageView);
            mPhoto = photo;
            mFullSize = fullSize;
            mCache = cache;
            mListener = listener;
        }

        public void runImpl() {
            final PhotoImageView imageView = mImageView.get();
            if (null == imageView) {
                return;
            }

            final Context context = imageView.getContext();
            final Bitmap filteredBitmap;

            final String key = mFullSize ? mPhoto.getDisplayImageKey()
                    : mPhoto.getThumbnailImageKey();
            CacheableBitmapWrapper wrapper = mCache.get(key);

            if (null == wrapper || !wrapper.hasValidBitmap()) {
                Bitmap bitmap = mFullSize ? mPhoto.getDisplayImage(context)
                        : mPhoto.getThumbnailImage(context);
                wrapper = new CacheableBitmapWrapper(key, bitmap);
                wrapper.setBeingUsed(true);
                mCache.put(wrapper);
            } else {
                wrapper.setBeingUsed(true);
            }

            // Don't process if we've been interrupted
            if (!isInterrupted()) {
                filteredBitmap = mPhoto.processBitmap(wrapper.getBitmap(), mFullSize, false);
            } else {
                filteredBitmap = null;
            }

            // Make sure we release the original bitmap
            wrapper.setBeingUsed(false);

            // If we haven't been interrupted, update the view
            if (!isInterrupted()) {

                imageView.post(new Runnable() {
                    public void run() {
                        imageView.setImageBitmap(filteredBitmap);

                        if (null != mListener) {
                            mListener.onPhotoLoadFinished(filteredBitmap);
                        }
                    }
                });
            }
        }
    }

    static final class PhotoLoadRunnable extends PhotoThreadRunnable {

        private final WeakReference<PhotoImageView> mImageView;
        private final Photo mPhoto;
        private final boolean mFullSize;
        private final BitmapLruCache mCache;
        private final OnPhotoLoadListener mListener;

        public PhotoLoadRunnable(PhotoImageView imageView, Photo photo,
                                 BitmapLruCache cache,
                                 final boolean fullSize, final OnPhotoLoadListener listener) {
            mImageView = new WeakReference<PhotoImageView>(imageView);
            mPhoto = photo;
            mFullSize = fullSize;
            mCache = cache;
            mListener = listener;
        }

        public void runImpl() {
            final PhotoImageView imageView = mImageView.get();
            if (null == imageView) {
                return;
            }

            final Context context = imageView.getContext();
            final CacheableBitmapWrapper wrapper;

            final Bitmap bitmap = mFullSize ? mPhoto.getDisplayImage(context)
                    : mPhoto.getThumbnailImage(context);

            if (null != bitmap) {
                final String key = mFullSize ? mPhoto.getDisplayImageKey()
                        : mPhoto.getThumbnailImageKey();
                wrapper = new CacheableBitmapWrapper(key, bitmap);
            } else {
                wrapper = null;
            }

            // If we're interrupted, just update the cache and return
            if (isInterrupted()) {
                mCache.put(wrapper);
                return;
            }

            // If we're still running, update the Views
            if (null != wrapper) {
                imageView.post(new Runnable() {
                    public void run() {
                        imageView.setImageCachedBitmap(wrapper);
                        mCache.put(wrapper);

                        if (null != mListener) {
                            mListener.onPhotoLoadFinished(wrapper.getBitmap());
                        }
                    }
                });
            }
        }
    }

    static class RequestFaceDetectionPassRunnable implements Runnable {

        private final PhotoImageView mImageView;
        private final Photo mSelection;

        public RequestFaceDetectionPassRunnable(PhotoImageView imageView, Photo selection) {
            mImageView = imageView;
            mSelection = selection;
        }

        public void run() {
            mImageView.requestFaceDetection(mSelection);
        }
    }

    private void requestFaceDetection(final Photo photo) {
        CacheableBitmapWrapper wrapper = getCachedBitmapWrapper();
        if (null != wrapper && wrapper.hasValidBitmap()) {
            wrapper.setBeingUsed(true);

            PhotoApplication app = PhotoApplication.getApplication(getContext());
            app.getMultiThreadExecutorService().submit(new FaceDetectionRunnable(photo, wrapper));
        }
    }

    private void requestFiltered(final Photo photo, boolean fullSize,
                                 final OnPhotoLoadListener listener) {
        PhotoApplication app = PhotoApplication.getApplication(getContext());
        mCurrentRunnable = app.getPhotoFilterThreadExecutorService().submit(
                new PhotoFilterRunnable(this, photo, app.getImageCache(), fullSize, listener));
    }

    private void requestImage(final Photo photo, final boolean fullSize,
                              final OnPhotoLoadListener listener) {
        final String key = fullSize ? photo.getDisplayImageKey() : photo.getThumbnailImageKey();
        BitmapLruCache cache = PhotoApplication.getApplication(getContext()).getImageCache();
        final CacheableBitmapWrapper cached = cache.get(key);

        if (null != cached && cached.hasValidBitmap()) {
            setImageCachedBitmap(cached);
            if (null != listener) {
                listener.onPhotoLoadFinished(cached.getBitmap());
            }
        } else {
            // Means we have an object with an invalid bitmap so remove it
            if (null != cached) {
                cache.remove(key);
            }

            PhotoApplication app = PhotoApplication.getApplication(getContext());
            mCurrentRunnable = app.getMultiThreadExecutorService().submit(
                    new PhotoLoadRunnable(this, photo, cache, fullSize, listener));
        }
    }

    private void resetForRequest(final boolean clearDrawable) {
        cancelRequest();

        // Clear currently display bitmap
        if (clearDrawable) {
            setImageDrawable(null);
        }
    }

    public void cancelRequest() {
        if (null != mCurrentRunnable) {
            mCurrentRunnable.cancel(true);
            mCurrentRunnable = null;
        }
    }

    public void recycleBitmap() {
        Bitmap currentBitmap = getCurrentBitmap();
        if (null != currentBitmap) {
            setImageDrawable(null);
            currentBitmap.recycle();
        }
    }

    public Bitmap getCurrentBitmap() {
        Drawable d = getDrawable();
        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        }
        return null;
    }

    public void setFadeInDrawables(boolean fadeIn) {
        mFadeInDrawables = fadeIn;

        if (fadeIn && null == mFadeFromDrawable) {
            mFadeFromDrawable = new ColorDrawable(Color.TRANSPARENT);
            mFadeDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        }
    }

    public void requestThumbnail(final Photo upload, final boolean honourFilter) {
        resetForRequest(true);

        if (upload.requiresProcessing(false) && honourFilter) {
            requestFiltered(upload, false, null);
        } else {
            requestImage(upload, false, null);
        }
    }
}
