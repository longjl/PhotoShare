package com.tintinshare.base;

import com.actionbarsherlock.app.SherlockActivity;
import com.tintinshare.util.Analytics;

/**
 * Created by longjianlin on 15/3/19.
 */
public class PhotoActivity extends SherlockActivity {
    @Override
    protected void onStart() {
        super.onStart();
        Analytics.onStartSession(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.onEndSession(this);
    }
}
