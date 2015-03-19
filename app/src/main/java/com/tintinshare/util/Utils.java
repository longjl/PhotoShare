package com.tintinshare.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.lightbox.android.photoprocessing.utils.MediaUtils;
import com.tintinshare.Flags;
import com.tintinshare.PhotoApplication;
import com.tintinshare.R;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by longjianlin on 15/3/19.
 */
public class Utils {
    public static Bitmap drawViewOntoBitmap(View view) {
        Bitmap image = Bitmap
                .createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(image);
        view.draw(canvas);
        return image;
    }
    public static Bitmap decodeImage(final ContentResolver resolver, final Uri uri,
                                     final int MAX_DIM)
            throws FileNotFoundException {

        // Get original dimensions
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(resolver.openInputStream(uri), null, o);
        } catch (SecurityException se) {
            se.printStackTrace();
            return null;
        }

        final int origWidth = o.outWidth;
        final int origHeight = o.outHeight;

        // Holds returned bitmap
        Bitmap bitmap;

        o.inJustDecodeBounds = false;
        o.inScaled = false;
        o.inPurgeable = true;
        o.inInputShareable = true;
        o.inDither = true;
        o.inPreferredConfig = Bitmap.Config.RGB_565;

        if (origWidth > MAX_DIM || origHeight > MAX_DIM) {
            int k = 1;
            int tmpHeight = origHeight, tmpWidth = origWidth;
            while ((tmpWidth / 2) >= MAX_DIM || (tmpHeight / 2) >= MAX_DIM) {
                tmpWidth /= 2;
                tmpHeight /= 2;
                k *= 2;
            }
            o.inSampleSize = k;

            bitmap = BitmapFactory.decodeStream(resolver.openInputStream(uri), null, o);
        } else {
            bitmap = BitmapFactory.decodeStream(resolver.openInputStream(uri), null, o);
        }
        return bitmap;
    }

    public static Bitmap rotate(Bitmap original, final int angle) {
        if ((angle % 360) == 0) {
            return original;
        }

        final boolean dimensionsChanged = angle == 90 || angle == 270;
        final int oldWidth = original.getWidth();
        final int oldHeight = original.getHeight();
        final int newWidth = dimensionsChanged ? oldHeight : oldWidth;
        final int newHeight = dimensionsChanged ? oldWidth : oldHeight;

        Bitmap bitmap = Bitmap.createBitmap(newWidth, newHeight, original.getConfig());
        Canvas canvas = new Canvas(bitmap);

        Matrix matrix = new Matrix();
        matrix.preTranslate((newWidth - oldWidth) / 2f, (newHeight - oldHeight) / 2f);
        matrix.postRotate(angle, bitmap.getWidth() / 2f, bitmap.getHeight() / 2);
        canvas.drawBitmap(original, matrix, null);

        original.recycle();

        return bitmap;
    }

    public static int getOrientationFromContentUri(ContentResolver cr, Uri contentUri) {
        int returnValue = 0;

        if (ContentResolver.SCHEME_CONTENT.equals(contentUri.getScheme())) {
            // can post image
            String[] proj = {MediaStore.Images.Media.ORIENTATION};
            Cursor cursor = cr.query(contentUri, proj, null, null, null);

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    returnValue = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));
                }
                cursor.close();
            }
        } else if (ContentResolver.SCHEME_FILE.equals(contentUri.getScheme())) {
            returnValue = MediaUtils.getExifOrientation(contentUri.getPath());
        }

        return returnValue;
    }

    public static void checkPhotoProcessingThread() {
        if (!PhotoApplication.THREAD_FILTERS.equals(Thread.currentThread().getName())) {
            throw new IllegalStateException("PhotoProcessing should be done on corrent thread!");
        }
    }

    public static boolean hasCamera(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    // And to convert the image URI to the direct file system path of the image
    // file
    public static String getPathFromContentUri(ContentResolver cr, Uri contentUri) {
        if (Flags.DEBUG) {
            Log.d("Utils", "Getting file path for Uri: " + contentUri);
        }

        String returnValue = null;

        if (ContentResolver.SCHEME_CONTENT.equals(contentUri.getScheme())) {
            // can post image
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = cr.query(contentUri, proj, null, null, null);

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    returnValue = cursor
                            .getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                }
                cursor.close();
            }
        } else if (ContentResolver.SCHEME_FILE.equals(contentUri.getScheme())) {
            returnValue = contentUri.getPath();
        }

        return returnValue;
    }

    public static void scanMediaJpegFile(final Context context, final File file,
                                         final MediaScannerConnection.OnScanCompletedListener listener) {
        MediaScannerConnection
                .scanFile(context, new String[]{file.getAbsolutePath()}, new String[]{"image/jpg"},
                        listener);
    }

    public static File getCameraPhotoFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(dir, "photo_" + System.currentTimeMillis() + ".jpg");
    }

    public static int getSpinnerItemResId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return android.R.layout.simple_spinner_item;
        } else {
            return R.layout.layout_spinner_item;
        }
    }
}
