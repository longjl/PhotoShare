package com.tintinshare.model;

import android.content.Context;

/**
 * Created by longjianlin on 15/3/19.
 */
public class MediaStoreBucket {
    private final String mBucketId;
    private final String mBucketName;

    public MediaStoreBucket(String id, String name) {
        mBucketId = id;
        mBucketName = name;
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
        return new MediaStoreBucket(null, "All Photo");
    }
}
