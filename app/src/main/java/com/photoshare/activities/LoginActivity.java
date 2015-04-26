package com.photoshare.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.photoshare.Constants;
import com.photoshare.R;
import com.photoshare.URLs;
import com.photoshare.base.PhotoFragmentActivity;
import com.photoshare.network.PhotoClient;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by longjianlin on 15/4/12.
 */
public class LoginActivity extends PhotoFragmentActivity {
    private static SharedPreferences mAccountPrefs;
    private static EditText mMobile;
    private static EditText mPwd;
    private TextView mRegister;
    private Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        mAccountPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mMobile = (EditText) findViewById(R.id.et_mobile);
        mPwd = (EditText) findViewById(R.id.et_pwd);

        mRegister = (TextView) findViewById(R.id.tv_register);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        mLogin = (Button) findViewById(R.id.btn_login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    login(mMobile.getText().toString(), mPwd.getText().toString());
                }
            }
        });
    }

    /**
     * 用户登录
     *
     * @param mobile
     * @param password
     */
    private void login(final String mobile, final String password) {
        RequestParams params = new RequestParams();
        params.put("mobile", mobile);
        params.put("pwd", password);

        PhotoClient.post(URLs.LOGIN_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (response.optInt("code") == 200) {
                    setMobileToPrefs(mobile);
                    setPwdToPrefs(password);
                    handler.sendEmptyMessage(200);
                } else {
                    Toast.makeText(LoginActivity.this, "登录失败,用户名或密码不匹配", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("**************",throwable.getMessage());
                Toast.makeText(LoginActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 200) {
                startActivity(new Intent(LoginActivity.this, PhotoShareActivity.class));
                finish();
            }
        }
    };
}
