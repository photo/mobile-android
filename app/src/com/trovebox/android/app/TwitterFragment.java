
package com.trovebox.android.app;


import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.widget.ProgressBar;

import com.trovebox.android.app.R;
import com.trovebox.android.app.common.CommonStyledDialogFragment;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.twitter.TwitterProvider;
import com.trovebox.android.app.twitter.TwitterUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.LoadingControlWithCounter;
import com.trovebox.android.app.util.SimpleAsyncTaskEx;
import com.trovebox.android.app.util.TrackerUtils;
import com.trovebox.android.app.util.concurrent.AsyncTaskEx;

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

/**
 * @author Eugene Popovich
 */
public class TwitterFragment extends CommonStyledDialogFragment
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
                    TrackerUtils.trackButtonClickEvent("logoutBtn", TwitterFragment.this);
                    performTwitterLogout();
                }

            });
            sendButton = (Button) view.findViewById(R.id.sendBtn);
            sendButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    TrackerUtils.trackButtonClickEvent("sendBtn", TwitterFragment.this);
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
            SimpleAsyncTaskEx
    {
        TextView loggedInAsText;
        String name;
        Context activity = getActivity();

        ShowCurrentlyLoggedInUserTask(final View view)
        {
            super(new LoadingControlWithCounter() {

                ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

                @Override
                public void stopLoadingEx() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void startLoadingEx() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
            loggedInAsText = (TextView) view
                    .findViewById(R.id.loggedInAs);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            loggedInAsText.setText(null);
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
        protected void onSuccessPostExecute() {
            loggedInAsText.setText(String
                    .format(
                            activity.getString(R.string.share_twitter_logged_in_as),
                            name));
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
        TrackerUtils.trackSocial("twitter", "status update",
                message);
        twitter.updateStatus(update);
    }

    public static interface TwitterLoadingControlAccessor
    {
        LoadingControl getTwitterLoadingControl();
    }
}
