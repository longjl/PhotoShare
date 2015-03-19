package com.tintinshare.tasks;

import java.util.concurrent.ThreadFactory;

/**
 * Created by longjianlin on 15/3/19.
 */
public class PhotoThreadFactory implements ThreadFactory {
    private final String mThreadName;

    public PhotoThreadFactory(String threadName) {
        mThreadName = threadName;
    }

    public PhotoThreadFactory() {
        this(null);
    }

    public Thread newThread(final Runnable r) {
        if (null != mThreadName) {
            return new Thread(r, mThreadName);
        } else {
            return new Thread(r);
        }
    }
}
