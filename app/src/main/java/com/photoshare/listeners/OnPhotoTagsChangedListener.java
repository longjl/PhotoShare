package com.photoshare.listeners;

import com.photoshare.model.PhotoTag;

/**
 * Created by longjianlin on 15/3/19.
 */
public interface OnPhotoTagsChangedListener {
    void onPhotoTagsChanged(PhotoTag tag, boolean added);
}
