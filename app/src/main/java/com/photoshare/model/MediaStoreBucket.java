package com.photoshare.model;

import android.content.Context;

/**
 * Created by longjianlin on 15/3/19.
 */
public class MediaStoreBucket {
    private String mBucketId;
    private String mBucketName;
    private String mImagePath;//照片路径
    private int mImageCount;//照片数

    public MediaStoreBucket(String id, String name, String imagePath, int imageCount) {
        mBucketId = id;
        mBucketName = name;
        mImagePath = imagePath;
        mImageCount = imageCount;
    }

    public void setImageCount(int imageCount) {
        mImageCount = imageCount;
    }

    public int getImageCount() {
        return mImageCount;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String imagePath) {
        mImagePath = imagePath;
    }

    public String getId() {
        return mBucketId;
    }

    public String getName() {
        return mBucketName;
    }

    @Override
    public String toString() {
        return mBucketName;
    }

    public static MediaStoreBucket getAllPhotosBucket(Context context) {
        return new MediaStoreBucket(null, "All Photo", null, 0);
    }
}
