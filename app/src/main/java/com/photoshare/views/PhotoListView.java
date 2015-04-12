package com.photoshare.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by longjianlin on 15/4/11.
 */
public class PhotoListView extends ListView {
    public PhotoListView(Context context) {
        super(context);
    }

    public PhotoListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
