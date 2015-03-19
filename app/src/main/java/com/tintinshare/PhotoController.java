package com.tintinshare;

import android.content.Context;

import com.tintinshare.events.PhotoSelectionAddedEvent;
import com.tintinshare.events.PhotoSelectionRemovedEvent;
import com.tintinshare.model.Photo;
import com.tintinshare.util.PhotoDatabaseHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by longjianlin on 15/3/19.
 */
public class PhotoController {
    private final Context mContext;
    private final ArrayList<Photo> mSelectedPhotoList;  //选中图片的集合

    public static PhotoController getFromContext(Context context) {
        return PhotoApplication.getApplication(context).getPhotoUploadController();
    }

    public PhotoController(Context context) {
        mContext = context;
        mSelectedPhotoList = new ArrayList<Photo>();
        populateFromDatabase();
    }

    private static List<Photo> checkListForInvalid(final Context context,
                                                   final List<Photo> uploads) {
        ArrayList<Photo> toBeRemoved = null;

        for (Photo upload : uploads) {
            if (!upload.isValid(context)) {
                if (null == toBeRemoved) {
                    toBeRemoved = new ArrayList<Photo>();
                }
                toBeRemoved.add(upload);
            }
        }

        if (null != toBeRemoved) {
            uploads.removeAll(toBeRemoved);

            // Delete from Database
            if (Flags.ENABLE_DB_PERSISTENCE) {
                PhotoDatabaseHelper.deleteFromDatabase(context, toBeRemoved);
            }
        }
        return toBeRemoved;
    }


    public void populateFromDatabase() {
        if (Flags.ENABLE_DB_PERSISTENCE) {
            final List<Photo> selectedFromDb = PhotoDatabaseHelper
                    .getSelected(mContext);
            if (null != selectedFromDb) {
                mSelectedPhotoList.addAll(selectedFromDb);
                checkSelectedForInvalid(false);
                Photo.populateCache(selectedFromDb);
            }

            final List<Photo> uploadsFromDb = PhotoDatabaseHelper.getUploads(mContext);
            if (null != uploadsFromDb) {
                //checkUploadsForInvalid(false);
                Photo.populateCache(uploadsFromDb);
            }
        }
    }


    private void checkSelectedForInvalid(final boolean sendEvent) {
        if (!mSelectedPhotoList.isEmpty()) {
            List<Photo> removedUploads = checkListForInvalid(mContext, mSelectedPhotoList);

            // Delete from Database
            if (Flags.ENABLE_DB_PERSISTENCE) {
                PhotoDatabaseHelper.deleteAllSelected(mContext);
            }

            if (sendEvent && null != removedUploads) {
                postEvent(new PhotoSelectionRemovedEvent(removedUploads));
            }
        }
    }

    private void postEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    public synchronized boolean addSelection(final Photo selection) {
        boolean result = false;

        if (!mSelectedPhotoList.contains(selection)) {
            selection.setUploadState(Photo.STATE_SELECTED);
            mSelectedPhotoList.add(selection);

            // Save to Database
            if (Flags.ENABLE_DB_PERSISTENCE) {
                PhotoDatabaseHelper.saveToDatabase(mContext, selection);
            }

            postEvent(new PhotoSelectionAddedEvent(selection));
            result = true;
        }
        return result;
    }

    public boolean removeSelection(final Photo selection) {
        boolean removed = false;
        synchronized (this) {
            removed = mSelectedPhotoList.remove(selection);
        }

        if (removed) {
            // Delete from Database
            if (Flags.ENABLE_DB_PERSISTENCE) {
                PhotoDatabaseHelper.deleteFromDatabase(mContext, selection);
            }

            // Reset State (as may still be in cache)
            selection.setUploadState(Photo.STATE_NONE);

            postEvent(new PhotoSelectionRemovedEvent(selection));
        }

        return removed;
    }

    public synchronized boolean isSelected(Photo selection) {
        return mSelectedPhotoList.contains(selection);
    }

    public synchronized void addSelections(List<Photo> selections) {
        final HashSet<Photo> currentSelectionsSet = new HashSet<Photo>(
                mSelectedPhotoList);
        boolean listModified = false;

        for (final Photo selection : selections) {
            if (!currentSelectionsSet.contains(selection)) {
                selection.setUploadState(Photo.STATE_SELECTED);
                mSelectedPhotoList.add(selection);
                listModified = true;
            }
        }

        if (listModified) {
            // Save to Database
            if (Flags.ENABLE_DB_PERSISTENCE) {
                PhotoDatabaseHelper.saveToDatabase(mContext, mSelectedPhotoList, true);
            }

            postEvent(new PhotoSelectionAddedEvent(selections));
        }
    }
}
