package com.photoshare.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.photoshare.model.Photo;
import com.photoshare.model.UploadQuality;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 分享
 * Created by longjianlin on 15/3/22.
 */
public class ShareUtils {
    /**
     * 分享方法
     *
     * @param context
     * @param platform 分享平台(微博,微信 ...)
     * @param photos   分享图片
     * @param content  分享内容
     */
    public static void share(Context context, String platform, List<Photo> photos, String content) {
        if (platform.equals("SinaWeibo")) {
            shareMultiplePictureToSinaWeibo(context, photos, content);
        } else if (platform.equals("WebChatMoments")) {
            shareMultiplePictureToTimeLine(context, photos, content);
        }
    }

    /**
     * 分享到新浪微博 (多张图)
     *
     * @param context
     * @param photos  分享图片
     * @param content 分享内容
     */
    public static void shareMultiplePictureToSinaWeibo(Context context, List<Photo> photos, String content) {

        ComponentName comp = new ComponentName("com.sina.weibo", "com.sina.weibo.EditActivity");
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(comp);

        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        for (Photo photo : photos) {
            File uploadFile;
            UploadQuality quality = photo.getUploadQuality();
            if (UploadQuality.ORIGINAL == quality && !photo.requiresNativeEditing(context)) {
                final String filePath = Utils
                        .getPathFromContentUri(context.getContentResolver(),
                                photo.getOriginalPhotoUri());
                uploadFile = new File(filePath);
            } else {
                uploadFile = photo.getUploadSaveFile();
                if (uploadFile.exists()) {
                    uploadFile.delete();
                }
                Bitmap bitmap = photo.getUploadImage(context, quality);
                OutputStream os = null;
                try {
                    uploadFile.createNewFile();
                    os = new BufferedOutputStream(new FileOutputStream(uploadFile));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality.getJpegQuality(), os);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != os) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                bitmap.recycle();
            }
            imageUris.add(Uri.fromFile(uploadFile));
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        context.startActivity(intent);
    }

    /**
     * 分享到微信朋友圈(多张图)
     *
     * @param context
     * @param photos
     */
    private static void shareMultiplePictureToTimeLine(Context context, List<Photo> photos, String content) {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");
        intent.putExtra("Kdescription", content);//发表的内容或者描述
        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        for (Photo photo : photos) {
            File uploadFile;
            UploadQuality quality = photo.getUploadQuality();
            if (UploadQuality.ORIGINAL == quality && !photo.requiresNativeEditing(context)) {
                final String filePath = Utils
                        .getPathFromContentUri(context.getContentResolver(),
                                photo.getOriginalPhotoUri());
                uploadFile = new File(filePath);
            } else {
                uploadFile = photo.getUploadSaveFile();
                if (uploadFile.exists()) {
                    uploadFile.delete();
                }
                Bitmap bitmap = photo.getUploadImage(context, quality);
                OutputStream os = null;
                try {
                    uploadFile.createNewFile();
                    os = new BufferedOutputStream(new FileOutputStream(uploadFile));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality.getJpegQuality(), os);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != os) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                bitmap.recycle();
            }
            imageUris.add(Uri.fromFile(uploadFile));
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        context.startActivity(intent);
    }
}
