package com.photoshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;

import com.photoshare.activities.LoginActivity;
import com.photoshare.base.PhotoFragmentActivity;

/**
 * Created by longjianlin on 15/4/12.
 */
public class SplashActivity extends PhotoFragmentActivity {
    private LinearLayout rootLayout;
    private SharedPreferences mAccountPrefs;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_splash);
        mAccountPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        rootLayout = (LinearLayout) findViewById(R.id.splash_root);
        AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(3000);
        rootLayout.startAnimation(animation);
    }

    /**
     * 从Prefs 中获取手机号
     *
     * @return
     */
    private String getMobileFromPrefs() {
        if (null != mAccountPrefs) {
            return mAccountPrefs.getString(Constants.PREF_MOBILE, null);
        }
        return null;
    }


    /**
     * 从Prefs 中获取密码
     *
     * @return
     */
    private String getPwdFromPrefs() {
        if (null != mAccountPrefs) {
            return mAccountPrefs.getString(Constants.PREF_PWD, null);
        }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
       /* if (getMobileFromPrefs() != null && getPwdFromPrefs() != null) {
            go();
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }*/

        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }
}
