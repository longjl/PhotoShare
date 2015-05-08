package com.photoshare.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by longjianlin on 15/1/28.
 */
public class Version {
    public int versionCode;         //本地版本号
    public String versionName;      //本地app名字

    public int remoteCode;          //远程版本号
    public String remoteName;       //远程app名字

    public String downloadUrl;      //下载地址

    public Version() {
    }

    public Version(Context context, int remoteCode, String remoteName, String downloadUrl) {
        this.versionCode = getVersionCode(context);
        this.versionName = getVersionName(context);
        this.remoteCode = remoteCode;
        this.remoteName = remoteName;
        this.downloadUrl = downloadUrl;
    }

    /**
     * 是否更新
     *
     * @return
     */
    public boolean isUpdate() {
        if (remoteCode > versionCode) {
            return true;
        }
        return false;
    }


    private String getVersionName(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packInfo.versionName;
    }

    private int getVersionCode(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packInfo.versionCode;
    }

}
