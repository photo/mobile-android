
package com.trovebox.android.app;

import org.holoeverywhere.widget.TextView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;

import com.trovebox.android.app.util.LoginUtils;
import com.trovebox.android.common.activity.CommonActivity;
import com.trovebox.android.common.fragment.common.CommonFragmentUtils;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.TrackerUtils;

public class AccountActivity extends CommonActivity {
    private static final String TAG = AccountActivity.class.getSimpleName();
    private static int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        init();
        addRegisteredReceiver(LoginUtils
                .getAndRegisterDestroyOnLoginActionBroadcastReceiver(TAG, this));
    }

    void init() {
        getGoogleLoginFragment();
        Button googleLoginButton = (Button) findViewById(R.id.account_google_login_button);
        googleLoginButton.setText(Html.fromHtml(getString(R.string.account_google_login_button)));
        findViewById(R.id.google_login_view).setVisibility(
                CommonUtils.isFroyoOrHigher() ? View.GONE : View.GONE);
        TextView signInInstructions = (TextView) findViewById(R.id.instant_sign_in_instructions);
        signInInstructions.setText(Html.fromHtml(getString(R.string.instant_sign_in_instructions)));
        TextView signUpInstructions = (TextView) findViewById(R.id.signup_instructions);
        signUpInstructions.setMovementMethod(LinkMovementMethod.getInstance());
        signUpInstructions.setText(Html.fromHtml(getString(R.string.account_signup_instructions)));
    }

    public void accountLoginButtonAction(View view) {
        CommonUtils.debug(TAG, "Start account login button action");
        TrackerUtils.trackButtonClickEvent("account_login_button", AccountActivity.this);
        Intent intent = new Intent(this, AccountLogin.class);
        startActivity(intent);
    }

    /**
     * Used as account_google_login_button onClick handler
     * 
     * @param view
     */
    public void accountGoogleLoginButtonAction(View view) {
        CommonUtils.debug(TAG, "Google login button action");
        TrackerUtils.trackButtonClickEvent("account_google_login_button", AccountActivity.this);
        getGoogleLoginFragment().doLogin(GOOGLE_PLAY_SERVICES_REQUEST_CODE);
    }

    /**
     * Get the google login fragment. Create it if it is null
     * 
     * @return
     */
    GoogleLoginFragment getGoogleLoginFragment() {
        return CommonFragmentUtils.findOrCreateFragment(
                GoogleLoginFragment.class,
                getSupportFragmentManager());
    }
}
