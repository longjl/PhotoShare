package com.photoshare.events;

import com.photoshare.model.Photo;

/**
 * Created by longjianlin on 15/3/19.
 */
public class UploadStateChangedEvent {
    private final Photo mUpload;

    public UploadStateChangedEvent(Photo upload) {
        mUpload = upload;
    }

    public Photo getUpload() {
        return mUpload;
    }
}
