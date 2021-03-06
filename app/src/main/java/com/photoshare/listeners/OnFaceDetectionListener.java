package com.photoshare.listeners;

import com.photoshare.model.Photo;

/**
 * Created by longjianlin on 15/3/19.
 */
public interface OnFaceDetectionListener {
    void onFaceDetectionStarted(Photo selection);

    void onFaceDetectionFinished(Photo selection);
}
