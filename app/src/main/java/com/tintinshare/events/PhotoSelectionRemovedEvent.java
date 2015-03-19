package com.tintinshare.events;

import com.tintinshare.model.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longjianlin on 15/3/19.
 */
public class PhotoSelectionRemovedEvent {
    private final List<Photo> mUploads;

    public PhotoSelectionRemovedEvent(List<Photo> uploads) {
        mUploads = uploads;
    }

    public PhotoSelectionRemovedEvent(Photo upload) {
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
