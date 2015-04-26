package com.photoshare.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.photoshare.util.NotificationUtil;

/**
 * Created by longjianlin on 15/4/24.
 */
public class NotificationDownloadService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int id = intent.getIntExtra("id", -1);
        String downloadUrl = intent.getStringExtra("downloadUrl");
        NotificationUtil.notifyProgressNotification(this, id, downloadUrl);
        return super.onStartCommand(intent, flags, startId);
    }
}
