package com.tintinshare.listeners;

import com.tintinshare.model.PhotoTag;

/**
 * Created by longjianlin on 15/3/19.
 */
public interface OnPhotoTagsChangedListener {
    void onPhotoTagsChanged(PhotoTag tag, boolean added);
}
