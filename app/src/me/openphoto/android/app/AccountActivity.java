
package me.openphoto.android.app;

import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.LoginUtils;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.holoeverywhere.app.Activity;

public class AccountActivity extends Activity
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
        Intent intent = new Intent(this, AccountSignup.class);
        startActivity(intent);
    }

    public void accountLoginButtonAction(View view) {
        CommonUtils.debug(TAG, "Start account login button action");
        Intent intent = new Intent(this, AccountLogin.class);
        startActivity(intent);
    }

    public void accountOwnServerButtonAction(View view)
    {
        CommonUtils.debug(TAG, "Start own server button action");
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
    }

}
