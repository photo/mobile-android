package me.openphoto.android.app;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.twitter.TwitterProvider;
import me.openphoto.android.app.twitter.TwitterUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author Eugene Popovich
 * @version
 *          10.10.2012
 *          <br>- created
 */
public class TwitterFragment extends CommonDialogFragment
{
	public static final String TAG = TwitterFragment.class.getSimpleName();

	Photo photo;

	private EditText messageEt;
	private LoadingControl loadingControl;

	private Button sendButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_twitter, container);
		init(view);
		return view;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		loadingControl = (LoadingControl) activity;
	}
	public void setPhoto(Photo photo)
	{
		this.photo = photo;
	}

	void init(View view)
	{
		try
		{
			final TextView loggedInAsText = (TextView) view
					.findViewById(R.id.loggedInAs);
			loggedInAsText.setText(null);
			// should to invoke it in the new thread to avoid network in
			// main thread exception
			new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						Twitter twitter = TwitterProvider
								.getTwitter(getActivity());
						loggedInAsText.setText(String.format(
								getString(R.string.share_twitter_logged_in_as),
								twitter.getScreenName()));
					} catch (Exception ex)
					{
						GuiUtils.error(TAG, null, ex,
								getActivity());
					}
				}
			}).start();
			messageEt = (EditText) view.findViewById(R.id.message);
			messageEt.setText(String.format(
					getString(R.string.share_twitter_default_msg),
					photo.getUrl(Photo.ORIGINAL_SIZE)));
			Button logOutButton = (Button) view.findViewById(R.id.logoutBtn);
			logOutButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					performTwitterLogout();
				}

			});
			sendButton = (Button) view.findViewById(R.id.sendBtn);
			sendButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					postTweet();
				}
			});
		} catch (Exception ex)
		{
			GuiUtils.error(TAG, "Could not init twitter fragment", ex,
					getActivity());
			dismiss();
		}
	}

	protected void postTweet()
	{
		new TweetTask().execute();
	}

	private void performTwitterLogout()
	{
		TwitterUtils.logout(getActivity());
		dismiss();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Dialog result = super.onCreateDialog(savedInstanceState);
		result.setTitle(R.string.share_twitter_dialog_title);
		return result;
	}

	private class TweetTask extends
			AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			sendButton.setEnabled(false);
			loadingControl.startLoading();
		}
		@Override
		protected Boolean doInBackground(Void... params)
		{
			try
			{
				Twitter twitter = TwitterProvider.getTwitter(getActivity());
				StatusUpdate update = new StatusUpdate(messageEt.getText()
						.toString());
				twitter.updateStatus(update);
				return true;
			} catch (Exception ex)
			{
				GuiUtils.error(TAG, "Could not send tweet", ex,
						getActivity());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			loadingControl.stopLoading();
			if (result.booleanValue())
			{
				GuiUtils.info(
						getString(R.string.share_twitter_success_message),
						getActivity());
			}
			TwitterFragment.this.dismiss();
		}
	}
}
