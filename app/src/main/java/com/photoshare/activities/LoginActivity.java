package com.photoshare.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.photoshare.Constants;
import com.photoshare.R;
import com.photoshare.base.PhotoFragmentActivity;

/**
 * Created by longjianlin on 15/4/12.
 */
public class LoginActivity extends PhotoFragmentActivity {
    private static SharedPreferences mAccountPrefs;
    private static EditText mMobile;
    private static EditText mPwd;
    private Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        mAccountPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mMobile = (EditText) findViewById(R.id.et_mobile);
        mPwd = (EditText) findViewById(R.id.et_pwd);
        mLogin = (Button) findViewById(R.id.btn_login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, PhotoShareActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    /**
     * 验证非空数据
     *
     * @return
     */
    private boolean validate() {
        if (mMobile.getText() == null || mMobile.getText().toString().length() == 0) {
            Toast.makeText(LoginActivity.this, R.string.mobile_is_not_null, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mPwd.getText() == null || mPwd.getText().toString().length() == 0) {
            Toast.makeText(LoginActivity.this, R.string.pwd_is_not_null, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /**
     * 保存登录手机号
     *
     * @param mobile
     */
    private static void setMobileToPrefs(final String mobile) {
        if (null != mAccountPrefs) {
            mAccountPrefs.edit().putString(Constants.PREF_MOBILE, mobile).commit();
        }
    }

    /**
     * 保存登录密码
     *
     * @param pwd
     */
    private static void setPwdToPrefs(final String pwd) {
        if (null != mAccountPrefs) {
            mAccountPrefs.edit().putString(Constants.PREF_PWD, pwd).commit();
        }
    }

    private Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 500) {
                Toast.makeText(LoginActivity.this, R.string.login_fail, Toast.LENGTH_SHORT).show();
            }
        }
    };


}
