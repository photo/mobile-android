
package me.openphoto.android.app;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.twitter.TwitterProvider;
import me.openphoto.android.app.twitter.TwitterUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.concurrent.AsyncTaskEx;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.app.Activity;
import com.WazaBe.HoloEverywhere.app.Dialog;

/**
 * @author Eugene Popovich
 */
public class TwitterFragment extends CommonDialogFragment
{
    public static final String TAG = TwitterFragment.class.getSimpleName();
    static final String TWEET = "TwitterFragmentTweet";

    Photo photo;

    private EditText messageEt;
    private LoadingControl loadingControl;

    private Button sendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_twitter, container);
        init(view, savedInstanceState);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TWEET, messageEt.getText().toString());
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadingControl = ((TwitterLoadingControlAccessor) activity).getTwitterLoadingControl();
    }

    public void setPhoto(Photo photo)
    {
        this.photo = photo;
    }

    void init(View view, Bundle savedInstanceState)
    {
        try
        {
            new ShowCurrentlyLoggedInUserTask(view).execute();
            messageEt = (EditText) view.findViewById(R.id.message);
            if (savedInstanceState != null)
            {
                messageEt.setText(savedInstanceState.getString(TWEET));
            } else
            {
                messageEt.setText(String.format(
                        getString(R.string.share_twitter_default_msg),
                        photo.getUrl(Photo.URL)));
            }
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
            GuiUtils.error(TAG, R.string.errorCouldNotInitTwitterFragment, ex,
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

    private class ShowCurrentlyLoggedInUserTask extends
            AsyncTaskEx<Void, Void, Boolean>
    {
        TextView loggedInAsText;
        String name;
        Context activity = getActivity();

        ShowCurrentlyLoggedInUserTask(View view)
        {
            loggedInAsText = (TextView) view
                    .findViewById(R.id.loggedInAs);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            loggedInAsText.setText(null);
            loadingControl.startLoading();
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                Twitter twitter = TwitterProvider.getTwitter(activity);
                name = twitter.getScreenName();
                return true;
            } catch (Exception ex)
            {
                GuiUtils.error(TAG,
                        R.string.errorCouldNotRetrieveTwitterScreenName,
                        ex,
                        activity);
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
                loggedInAsText.setText(String
                        .format(
                                activity.getString(R.string.share_twitter_logged_in_as),
                                name));
            }
        }
    }

    private class TweetTask extends
            AsyncTaskEx<Void, Void, Boolean>
    {
        Context activity = getActivity();

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
                sendTweet(messageEt.getText()
                        .toString(), activity);
                return true;
            } catch (Exception ex)
            {
                GuiUtils.error(TAG, R.string.errorCouldNotSendTweet, ex,
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
                        R.string.share_twitter_success_message);
            }
            Dialog dialog = TwitterFragment.this.getDialog();
            if (dialog != null && dialog.isShowing())
            {
                TwitterFragment.this.dismiss();
            }
        }
    }

    public static void sendTweet(String message, Context context)
            throws TwitterException
    {
        Twitter twitter = TwitterProvider.getTwitter(context);
        if (twitter != null)
        {
            sendTweet(message, twitter);
        }
    }

    public static void sendTweet(String message, Twitter twitter)
            throws TwitterException
    {
        StatusUpdate update = new StatusUpdate(message);
        twitter.updateStatus(update);
    }

    public static interface TwitterLoadingControlAccessor
    {
        LoadingControl getTwitterLoadingControl();
    }
}
