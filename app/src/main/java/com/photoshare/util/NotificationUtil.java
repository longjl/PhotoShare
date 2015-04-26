package com.photoshare.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.photoshare.R;
import com.photoshare.activities.PhotoShareActivity;
import com.photoshare.tasks.ProgressAsyncTask;

import java.io.File;

/**
 * Created by longjianlin on 15/1/28.
 */
public class NotificationUtil {
    private static NotificationManager nm;
    private static final String TITLE = "照片分享应用更新提示";

    public static void notifyProgressNotification(Context context, int notifyId, String downloadUrl) {
        if (nm == null) {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        String savePath = getSavePath(context, "/photoshare/apks");
        if (TextUtils.isEmpty(savePath)) {
            //去市场更新
            /*Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("market://details?id="
                    + context.getPackageName()));
            mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT));
            mBuilder.setContentText("您的SD卡不可用,建议到市场更新本程序");
            mBuilder.setContentTitle(TITLE);
            setNotificationIcon(context, mBuilder);
            nm.notify(notifyId, mBuilder.build());*/
            return;
        }
        // 当前手机版本>=4.0
        if (hasIceCreamSandwich()) {
            Intent intent = new Intent(context, PhotoShareActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
            mBuilder.setContentTitle(TITLE).setContentText("下载进度：");
            setNotificationIcon(context, mBuilder);
            new ProgressAsyncTask(context, downloadUrl, savePath, null, true, nm, mBuilder,
                    notifyId).execute();
        } else {// 4.0以前版本
            // // 一定要设置Icon,否则不显示
            // setNotificationIcon(context, mBuilder);
            new ProgressAsyncTask(context, downloadUrl, savePath, null, false, nm, notifyId,
                    getNotification(context, TITLE)).execute();
        }
    }

    /**
     * 设置Notification的icon,<br>
     * 如果不设置图标则无法显示Notification,并且LargeIcon,SmallIcon都需要设置
     *
     * @param context
     * @param mBuilder
     */
    private static void setNotificationIcon(Context context,
                                            final NotificationCompat.Builder mBuilder) {
        // 如果不设置小图标则无法显示Notification
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon));
        mBuilder.setSmallIcon(R.drawable.icon);
    }

    /**
     * 兼容4.0以下设备
     *
     * @param context
     * @param title
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Notification getNotification(Context context, String title) {
        Notification notification = new Notification(
                R.drawable.icon, "下载提醒",
                System.currentTimeMillis());
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.contentView = new RemoteViews(context.getPackageName(),
                R.layout.notification_progress_layout);
        notification.contentView.setProgressBar(
                R.id.notification_progress_layout_pb, 100, 0, false);
        notification.contentView.setTextViewText(
                R.id.notification_progress_layout_tv_title, title);
        notification.contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, PhotoShareActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        return notification;
    }

    /**
     * 获取文件保存目录
     *
     * @return
     */
    private static String getSavePath(Context context, String subDir) {
        // 判断SD卡是否存在
        boolean sdCardExist = StorageUtil.isExternalStorageAvailable();
        File file = null;
        if (sdCardExist) {
            file = Environment.getExternalStorageDirectory();
        } else {// 内存存储空间
            file = context.getFilesDir();
            return getPath(file, subDir);
        }
        return getPath(file, subDir);
    }

    private static String getPath(File f, String subDir) {
        File file = new File(f.getAbsolutePath() + subDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 4.0
     *
     * @return
     */
    public static boolean hasIceCreamSandwich() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }
}
