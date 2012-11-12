
package me.openphoto.android.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import me.openphoto.android.app.facebook.FacebookProvider;
import me.openphoto.android.app.facebook.FacebookUtils;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
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
import com.facebook.android.Facebook;
import com.facebook.android.R;

/**
 * @author Eugene Popovich
 */
public class FacebookFragment extends CommonDialogFragment
{
    public static final String TAG = FacebookFragment.class.getSimpleName();
    static final String PHOTO = "FacebookFragmentPhoto";

    Photo photo;

    private EditText messageEt;
    private LoadingControl loadingControl;

    private Button sendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_facebook, container);
        if (savedInstanceState != null)
        {
            photo = savedInstanceState.getParcelable(PHOTO);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
        init(view);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PHOTO, photo);
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
            new ShowCurrentlyLoggedInUserTask(view).execute();
            messageEt = (EditText) view.findViewById(R.id.message);
            messageEt.setText(null);
            Button logOutButton = (Button) view.findViewById(R.id.logoutBtn);
            logOutButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    performFacebookLogout();
                }

            });
            sendButton = (Button) view.findViewById(R.id.sendBtn);
            sendButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    postPhoto();
                }
            });
        } catch (Exception ex)
        {
            GuiUtils.error(TAG, R.string.errorCouldNotInitFacebookFragment, ex,
                    getActivity());
            dismiss();
        }
    }

    protected void postPhoto()
    {
        new PostPhotoTask().execute();
    }

    private void performFacebookLogout()
    {
        FacebookUtils.logoutRequest(getActivity());
        dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog result = super.onCreateDialog(savedInstanceState);
        result.setTitle(R.string.share_facebook_dialog_title);
        return result;
    }

    private class ShowCurrentlyLoggedInUserTask extends
            AsyncTask<Void, Void, Boolean>
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
                Facebook facebook = FacebookProvider.getFacebook();
                Bundle bparams = new Bundle();
                bparams.putString("fields", "name");
                String response = facebook.request("me", bparams);
                JSONObject jsonObject = new JSONObject(response);

                name = jsonObject.getString("name");
                return true;
            } catch (Exception ex)
            {
                GuiUtils.error(TAG,
                        R.string.errorCouldNotRetrieveFacebookScreenName,
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
                                activity.getString(R.string.share_facebook_logged_in_as),
                                name));
            }
        }
    }

    private class PostPhotoTask extends
            AsyncTask<Void, Void, Boolean>
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
                sharePhoto(messageEt.getText().toString(), photo, activity);
                return true;
            } catch (Exception ex)
            {
                GuiUtils.error(TAG, R.string.errorCouldNotSendFacebookPhoto,
                        ex,
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
                        R.string.share_facebook_success_message);
            }
            Dialog dialog = FacebookFragment.this.getDialog();
            if (dialog != null && dialog.isShowing())
            {
                FacebookFragment.this.dismiss();
            }
        }
    }

    public static void sharePhoto(
            String message,
            Photo photo,
            Context context) throws FileNotFoundException,
            MalformedURLException, IOException
    {
        Facebook facebook = FacebookProvider.getFacebook();
        Bundle bparams = new Bundle();
        bparams.putString(
                "message",
                message);
        bparams.putString(
                "name",
                photo.getTitle());
        bparams.putString(
                "caption",
                context.getString(R.string.share_facebook_default_caption));
        bparams.putString("description", context
                .getString(R.string.share_facebook_default_description));
        bparams.putString("picture", photo.getUrl(Photo.URL));
        facebook.request("feed", bparams, "POST");
    }
}
