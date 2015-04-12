package com.photoshare;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.photoshare.model.Account;
import com.photoshare.tasks.PhotoThreadFactory;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.co.senab.bitmapcache.BitmapLruCache;

/**
 * Created by longjianlin on 15/3/19.
 */
public class PhotoApplication extends Application {
    public static final String THREAD_FILTERS = "filters_thread";
    static final float EXECUTOR_POOL_SIZE_PER_CORE = 1.5f;
    private ExecutorService mMultiThreadExecutor, mSingleThreadExecutor, mDatabaseThreadExecutor;
    private BitmapLruCache mImageCache;
    private PhotoController mPhotoController;
    public Account account;

    @Override
    public void onCreate() {
        super.onCreate();
        initAccount();
        mPhotoController = new PhotoController(this);
    }

    private void initAccount() {
        account = new Account();
        account.acc_id = "1234567890";
        account.nickname = "longjl";
    }

    public static PhotoApplication getApplication(Context context) {
        return (PhotoApplication) context.getApplicationContext();
    }


    @SuppressWarnings("deprecation")
    public int getSmallestScreenDimension() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return Math.min(display.getHeight(), display.getWidth());
    }

    public ExecutorService getMultiThreadExecutorService() {
        if (null == mMultiThreadExecutor || mMultiThreadExecutor.isShutdown()) {
            final int numThreads = Math.round(Runtime.getRuntime().availableProcessors()
                    * EXECUTOR_POOL_SIZE_PER_CORE);
            mMultiThreadExecutor = Executors
                    .newFixedThreadPool(numThreads, new PhotoThreadFactory());

        }
        return mMultiThreadExecutor;
    }

    public ExecutorService getPhotoFilterThreadExecutorService() {
        if (null == mSingleThreadExecutor || mSingleThreadExecutor.isShutdown()) {
            mSingleThreadExecutor = Executors
                    .newSingleThreadExecutor(new PhotoThreadFactory(THREAD_FILTERS));
        }
        return mSingleThreadExecutor;
    }

    public BitmapLruCache getImageCache() {
        if (null == mImageCache) {
            mImageCache = new BitmapLruCache(this, Constants.IMAGE_CACHE_HEAP_PERCENTAGE);
        }
        return mImageCache;
    }


    public PhotoController getPhotoUploadController() {
        return mPhotoController;
    }


    public ExecutorService getDatabaseThreadExecutorService() {
        if (null == mDatabaseThreadExecutor || mDatabaseThreadExecutor.isShutdown()) {
            mDatabaseThreadExecutor = Executors.newSingleThreadExecutor(new PhotoThreadFactory());
        }
        return mDatabaseThreadExecutor;
    }


    /**
     * 判断是否安装应用
     *
     * @param context
     * @param packageName
     * @return
     */
    public boolean uninstallSoftware(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
            //判断是否获取到了对应的包名信息
            if (pInfo != null) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("uninstallSoftware", e.getMessage());
        }
        return false;
    }


    /**
     * 删除本地缓冲内容
     *
     * @param directory
     */
    public void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

    /**
     * 隐藏软键盘
     *
     * @param context
     * @param view
     */
    public void hideSoftInputFormWindow(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {//处于打开状态
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (null != mImageCache) {
            mImageCache.trimMemory();
        }
    }

}
