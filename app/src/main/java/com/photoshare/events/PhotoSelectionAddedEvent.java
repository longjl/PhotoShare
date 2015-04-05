package com.photoshare.events;

import com.photoshare.model.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longjianlin on 15/3/19.
 */
public class PhotoSelectionAddedEvent {
    private final List<Photo> mUploads;

    public PhotoSelectionAddedEvent(List<Photo> uploads) {
        mUploads = uploads;
    }

    public PhotoSelectionAddedEvent(Photo upload) {
        mUploads = new ArrayList<Photo>();
        mUploads.add(upload);
    }

    public List<Photo> getTargets() {
        return mUploads;
    }

    public Photo getTarget() {
        if (isSingleChange()) {
            return mUploads.get(0);
        } else {
            throw new IllegalStateException("Can only call this when isSingleChange returns true");
        }
    }

    public boolean isSingleChange() {
        return mUploads.size() == 1;
    }
}
