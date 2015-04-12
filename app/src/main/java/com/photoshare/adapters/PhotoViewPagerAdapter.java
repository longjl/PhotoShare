/*
 * Copyright 2013 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.photoshare.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.photoshare.PhotoApplication;
import com.photoshare.PhotoController;
import com.photoshare.model.Photo;
import com.photoshare.views.MultiTouchImageView;
import com.photoshare.views.PhotoTagItemLayout;

import java.util.List;

/**
 * 浏览图片
 */
public class PhotoViewPagerAdapter extends PagerAdapter {

    private final Context mContext;
    private List<Photo> mItems;
    private final PhotoController mController;

    public PhotoViewPagerAdapter(Context context, List<Photo> photos) {
        mContext = context;
        mItems = photos;
        PhotoApplication app = PhotoApplication.getApplication(context);
        mController = app.getPhotoUploadController();
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        PhotoTagItemLayout view = (PhotoTagItemLayout) object;

        MultiTouchImageView imageView = view.getImageView();
        imageView.cancelRequest();

        ((ViewPager) container).removeView(view);
    }

    @Override
    public int getCount() {
        return null != mItems ? mItems.size() : 0;
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public Photo getItem(int position) {
        if (position >= 0 && position < getCount()) {
            return mItems.get(position);
        }
        return null;
    }

    @Override
    public Object instantiateItem(View container, int position) {
        Photo upload = mItems.get(position);
        PhotoTagItemLayout view = new PhotoTagItemLayout(mContext, mController, upload, true);
        view.setPosition(position);

        upload.setFaceDetectionListener(view);

        MultiTouchImageView imageView = view.getImageView();
        imageView.requestFullSize(upload, true, null);

        ((ViewPager) container).addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View) object);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    protected Context getContext() {
        return mContext;
    }

}
