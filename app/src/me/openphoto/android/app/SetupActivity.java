
package me.openphoto.android.app;

import me.openphoto.android.app.oauth.OAuthUtils;
import me.openphoto.android.app.util.LoadingControl;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * The activity that gets presented to the user in case the user is not logged
 * in to a server. - setup screen
 * 
 * @author Patrick Boos
 */
public class SetupActivity extends SherlockFragmentActivity implements
		LoadingControl
{
    public static final String TAG = SetupActivity.class.getSimpleName();
	ProgressDialog progress;
    /**
     * Called when Setup Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (savedInstanceState == null)
		{
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content,
							new UiFragment()).commit();
		}
    }

    @Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		super.onNewIntent(intent);
		if (intent != null && intent.getData() != null)
		{
			Uri uri = intent.getData();
			OAuthUtils.verifyOAuthResponse(this, this, uri, new Runnable()
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

	@Override
	public void startLoading()
	{
		progress = ProgressDialog.show(this,
				getString(R.string.logging_in_message), null, true, false);
	}

	@Override
	public void stopLoading()
	{
		if (progress != null && progress.isShowing())
		{
			progress.dismiss();
			progress = null;
		}
	}

	public static class UiFragment extends SherlockFragment implements
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
			((Button) v.findViewById(R.id.button_create_account))
					.setOnClickListener(this);
			return v;
		}

		@Override
		public void onClick(View v)
		{

			switch (v.getId())
			{
				case R.id.button_login:
					String server = ((EditText) getView().findViewById(
							R.id.edit_server))
							.getText().toString();
					Preferences.setServer(getActivity(), server);
					OAuthUtils.askOAuth(getActivity());
				break;
				case R.id.button_create_account:
					Intent browserIntent = new Intent(Intent.ACTION_VIEW,
							Uri.parse("https://openphoto.me/"));
					startActivity(browserIntent);
				break;
			}

		}
	}
}
