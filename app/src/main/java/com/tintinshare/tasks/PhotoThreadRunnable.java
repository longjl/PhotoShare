package com.tintinshare.tasks;

import android.os.*;
import android.os.Process;

/**
 * Created by longjianlin on 15/3/19.
 */
public abstract class PhotoThreadRunnable implements Runnable {
    public final void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        runImpl();
    }

    public abstract void runImpl();

    protected boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

}
