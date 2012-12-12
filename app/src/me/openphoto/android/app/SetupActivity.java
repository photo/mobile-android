
package me.openphoto.android.app;

import me.openphoto.android.app.common.CommonActivity;
import me.openphoto.android.app.common.CommonFragment;
import me.openphoto.android.app.oauth.OAuthUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.ProgressDialogLoadingControl;
import me.openphoto.android.app.util.TrackerUtils;
import me.openphoto.android.app.util.regex.Patterns;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.ProgressDialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;

/**
 * The activity that gets presented to the user in case the user is not logged
 * in to a server. - setup screen
 * 
 * @author Patrick Boos
 */
public class SetupActivity extends CommonActivity
{
    public static final String TAG = SetupActivity.class.getSimpleName();
    ProgressDialog progress;

    /**
     * Called when Setup Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content,
                            new SetupUiFragment()).commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if (intent != null && intent.getData() != null)
        {
            Uri uri = intent.getData();
            OAuthUtils.verifyOAuthResponse(this, new ProgressDialogLoadingControl(this, true,
                    false,
                    getString(R.string.logging_in_message)), uri, new Runnable()
            {

                @Override
                public void run()
                {
                    startActivity(new Intent(SetupActivity.this,
                            MainActivity.class));
                    finish();
                }
            });
        }
    }

    public static class SetupUiFragment extends CommonFragment implements
            OnClickListener
    {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState)
        {
            View v = inflater
                    .inflate(R.layout.activity_setup, container, false);
            ((Button) v.findViewById(R.id.button_login))
                    .setOnClickListener(this);
            return v;
        }

        @Override
        public void onClick(View v)
        {

            switch (v.getId())
            {
                case R.id.button_login:
                    TrackerUtils.trackButtonClickEvent("button_login", SetupUiFragment.this);
                    String server = ((EditText) getView().findViewById(
                            R.id.edit_server))
                            .getText().toString().trim();
                    if (!(URLUtil.isHttpUrl(server) || URLUtil.isHttpsUrl(server)))
                    {
                        server = "http://" + server;
                    }
                    if (!Patterns.WEB_URL.matcher(server).matches())
                    {
                        GuiUtils.alert(R.string.errorSpecifiedUrlIsInvalid);
                    } else
                    {
                        Preferences.setServer(getActivity(), server);
                        OAuthUtils.askOAuth(getActivity());
                    }
                    break;
            }

        }
    }
}
