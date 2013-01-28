
package com.trovebox.android.app;

import com.trovebox.android.app.R;
import com.trovebox.android.app.common.CommonActivity;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.LoginUtils;
import com.trovebox.android.app.util.TrackerUtils;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AccountActivity extends CommonActivity
{

    private static final String TAG = AccountActivity.class.getSimpleName();

    BroadcastReceiver loginBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        loginBroadcastReceiver = LoginUtils
                .getAndRegisterDestroyOnLoginActionBroadcastReceiver(TAG, this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(loginBroadcastReceiver);
    }

    public void accountSignupButtonAction(View view) {
        CommonUtils.debug(TAG, "Start account signup button action");
        TrackerUtils.trackButtonClickEvent("signup_button", AccountActivity.this);
        Intent intent = new Intent(this, AccountSignup.class);
        startActivity(intent);
    }

    public void accountLoginButtonAction(View view) {
        CommonUtils.debug(TAG, "Start account login button action");
        TrackerUtils.trackButtonClickEvent("account_login_button", AccountActivity.this);
        Intent intent = new Intent(this, AccountLogin.class);
        startActivity(intent);
    }

    public void accountOwnServerButtonAction(View view)
    {
        CommonUtils.debug(TAG, "Start own server button action");
        TrackerUtils.trackButtonClickEvent("own_server_login_button", AccountActivity.this);
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
    }

}
