
package com.trovebox.android.app;

import java.io.File;
import java.util.ArrayList;


import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.Switch;

import com.trovebox.android.app.R;
import com.trovebox.android.app.common.CommonFragment;
import com.trovebox.android.app.facebook.FacebookUtils;
import com.trovebox.android.app.net.UploadMetaData;
import com.trovebox.android.app.provider.UploadsProviderAccessor;
import com.trovebox.android.app.service.UploaderService;
import com.trovebox.android.app.twitter.TwitterUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.ProgressDialogLoadingControl;
import com.trovebox.android.app.util.SyncUtils;
import com.trovebox.android.app.util.TrackerUtils;
import com.trovebox.android.app.util.concurrent.AsyncTaskEx;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class SyncUploadFragment extends CommonFragment
{
    static final String TAG = SyncUploadFragment.class.getSimpleName();
    PreviousStepFlow previousStepFlow;
    private LoadingControl loadingControl;
    EditText editTitle;
    EditText editTags;
    Switch privateSwitch;
    Switch twitterSwitch;
    Switch facebookSwitch;

    static SyncUploadFragment instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_sync_upload_settings,
                container, false);
        init(v);
        return v;
    }

    public void init(View v)
    {
        Button previousStepBtn = (Button) v.findViewById(R.id.previousBtn);
        previousStepBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TrackerUtils.trackButtonClickEvent("previousBtn", SyncUploadFragment.this);
                if (previousStepFlow != null)
                {
                    previousStepFlow.activatePreviousStep();
                }
            }
        });
        Button uploadBtn = (Button) v.findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TrackerUtils.trackButtonClickEvent("uploadBtn", SyncUploadFragment.this);
                v.setEnabled(false);
                uploadSelectedFiles(true, true);
            }
        });
        editTitle = (EditText) v.findViewById(
                R.id.edit_title);
        editTags = (EditText) v.findViewById(
                R.id.edit_tags);
        privateSwitch = (Switch) v.findViewById(R.id.private_switch);
        twitterSwitch = (Switch) v.findViewById(R.id.twitter_switch);
        facebookSwitch = (Switch) v.findViewById(R.id.facebook_switch);

        privateSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reinitShareSwitches(!isChecked);
            }
        });
        reinitShareSwitches();
    }

    void reinitShareSwitches()
    {
        reinitShareSwitches(!privateSwitch.isChecked());
    }

    void reinitShareSwitches(boolean enabled)
    {
        twitterSwitch.setEnabled(enabled);
        facebookSwitch.setEnabled(enabled);
    }
    void uploadSelectedFiles(
            final boolean checkTwitter,
            final boolean checkFacebook)
    {
        if (checkTwitter && twitterSwitch.isEnabled() && twitterSwitch.isChecked())
        {
            Runnable runnable = new Runnable()
            {

                @Override
                public void run()
                {
                    instance.uploadSelectedFiles(false, checkFacebook);
                }
            };
            TwitterUtils.runAfterTwitterAuthentication(
                    new ProgressDialogLoadingControl(getActivity(), true, false,
                            getString(R.string.share_twitter_requesting_authentication)),
                    getSupportActivity(),
                    runnable, runnable);
            return;
        }
        if (checkFacebook && facebookSwitch.isEnabled() && facebookSwitch.isChecked())
        {
            Runnable runnable = new Runnable()
            {

                @Override
                public void run()
                {
                    instance.uploadSelectedFiles(checkTwitter, false);
                }
            };
            FacebookUtils.runAfterFacebookAuthentication(getSupportActivity(),
                    MainActivity.AUTHORIZE_ACTIVITY_REQUEST_CODE,
                    runnable, runnable);
            return;
        }
        new UploadInitTask().execute();
    }

    public PreviousStepFlow getPreviousStepFlow()
    {
        return previousStepFlow;
    }

    public void setPreviousStepFlow(PreviousStepFlow previousStepFlow)
    {
        this.previousStepFlow = previousStepFlow;
    }

    static interface PreviousStepFlow
    {
        void activatePreviousStep();

        ArrayList<String> getSelectedFileNames();
    }

    private class UploadInitTask extends
            AsyncTaskEx<Void, Void, Boolean>
    {

        private ArrayList<String> selectedFiles;

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                UploadsProviderAccessor uploads = new UploadsProviderAccessor(
                        TroveboxApplication.getContext());
                UploadMetaData metaData = new UploadMetaData();
                metaData.setTitle(editTitle
                        .getText().toString());
                metaData.setTags(editTags
                        .getText().toString());
                metaData.setPrivate(privateSwitch.isChecked());
                boolean shareOnFacebook = facebookSwitch.isChecked();
                boolean shareOnTwitter = twitterSwitch.isChecked();

                selectedFiles = getPreviousStepFlow()
                        .getSelectedFileNames();
                for (String fileName : selectedFiles)
                {
                    File uploadFile = new File(fileName);
                    uploads.addPendingUpload(Uri.fromFile(uploadFile),
                            metaData, shareOnTwitter,
                            shareOnFacebook);
                }
                TroveboxApplication.getContext().startService(
                        new Intent(TroveboxApplication.getContext(),
                                UploaderService.class));
                return true;
            } catch (Exception e)
            {
                GuiUtils.error(TAG,
                        e);
            }
            return false;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            loadingControl.startLoading();
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            loadingControl.stopLoading();
            if (result.booleanValue())
            {
                GuiUtils.alert(R.string.uploading_in_background);
                SyncUtils.sendSyncStartedBroadcast(selectedFiles);
            }
        }

    }

    public void clear()
    {
        // TODO Auto-generated method stub
    }
}
