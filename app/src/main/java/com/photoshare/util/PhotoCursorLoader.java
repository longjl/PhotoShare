package com.photoshare.util;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by longjianlin on 15/3/19.
 */
public class PhotoCursorLoader extends CursorLoader {

    private final boolean mRequeryOnChange;

    public PhotoCursorLoader(Context context, Uri uri, String[] projection, String selection,
                             String[] selectionArgs,
                             String sortOrder, boolean requeryOnChange) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
        mRequeryOnChange = requeryOnChange;
    }

    @Override
    public void onContentChanged() {
        if (mRequeryOnChange) {
            super.onContentChanged();
        }
    }

}