package com.photoshare.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.photoshare.model.MediaStoreBucket;
import com.photoshare.model.Photo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by longjianlin on 15/3/19.
 */
public class MediaStoreCursorHelper {
    public static final String[] PHOTOS_PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.MINI_THUMB_MAGIC,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID};
    public static final String PHOTOS_ORDER_BY = MediaStore.Images.Media.DATE_ADDED + " desc";

    public static final Uri MEDIA_STORE_CONTENT_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    public static ArrayList<Photo> photosCursorToSelectionList(Uri contentUri,
                                                               Cursor cursor) {
        ArrayList<Photo> items = new ArrayList<Photo>(cursor.getCount());
        Photo item;

        if (cursor.moveToFirst()) {
            do {
                try {
                    item = photosCursorToSelection(contentUri, cursor);
                    if (null != item) {
                        items.add(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        // Need to reset the List so that oldest is first
        Collections.reverse(items);

        return items;
    }

    /**
     * 获取图片
     *
     * @param contentUri
     * @param cursor
     * @return
     */
    public static Photo photosCursorToSelection(Uri contentUri, Cursor cursor) {
        Photo item = null;
        try {
            File file = new File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)));
            if (file.exists()) {
                item = Photo.getSelection(contentUri,
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    /**
     * 查询图片分类列表
     *
     * @param cursor
     * @param items
     */
    public static void photosCursorToBucketList(Cursor cursor, ArrayList<MediaStoreBucket> items) {
        final HashSet<String> bucketIds = new HashSet<String>();

        final int idColumn = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);
        final int nameColumn = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);

        if (cursor.moveToFirst()) {
            do {
                try {
                    final String bucketId = cursor.getString(idColumn);
                    if (bucketIds.add(bucketId)) {
                        items.add(new MediaStoreBucket(bucketId, cursor.getString(nameColumn)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
    }

    /**
     * 扫描图片
     *
     * @param context
     * @param contentUri
     * @return
     */
    public static Cursor openPhotosCursor(Context context, Uri contentUri) {
        return context.getContentResolver()
                .query(contentUri, PHOTOS_PROJECTION, null, null, PHOTOS_ORDER_BY);
    }


    /**
     * 删除照片
     *
     * @param context
     * @param contentUri
     * @param name
     */
    public static void deletePhotosCursor(Context context, Uri contentUri, String name) {
        context.getContentResolver().delete(contentUri, "bucket_display_name=?", new String[]{name});
    }
}
