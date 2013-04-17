
package com.trovebox.android.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.Switch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.trovebox.android.app.common.CommonFragment;
import com.trovebox.android.app.facebook.FacebookUtils;
import com.trovebox.android.app.model.utils.AlbumUtils;
import com.trovebox.android.app.net.UploadMetaData;
import com.trovebox.android.app.net.account.AccountLimitUtils;
import com.trovebox.android.app.provider.UploadsProviderAccessor;
import com.trovebox.android.app.service.UploaderService;
import com.trovebox.android.app.twitter.TwitterUtils;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.ProgressDialogLoadingControl;
import com.trovebox.android.app.util.SyncUtils;
import com.trovebox.android.app.util.TrackerUtils;
import com.trovebox.android.app.util.concurrent.AsyncTaskEx;
import com.trovebox.android.app.util.data.StringMapParcelableWrapper;

public class SyncUploadFragment extends CommonFragment implements OnClickListener
{
    static final String TAG = SyncUploadFragment.class.getSimpleName();
    static final String SELECTED_ALBUMS = "SELECTED_ALBUMS";
    public static final int REQUEST_ALBUMS = MainActivity.REQUEST_ALBUMS;
    PreviousStepFlow previousStepFlow;
    private LoadingControl loadingControl;
    EditText editTitle;
    EditText editTags;
    EditText albumsText;
    Switch privateSwitch;
    Switch twitterSwitch;
    Switch facebookSwitch;
    StringMapParcelableWrapper albumsWrapper;

    static SyncUploadFragment instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        instance = this;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.sync_upload, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_back: {
                TrackerUtils
                        .trackOptionsMenuClickEvent("menu_back", SyncUploadFragment.this);
                if (previousStepFlow != null)
                {
                    previousStepFlow.activatePreviousStep();
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SELECTED_ALBUMS, (Parcelable) albumsText.getTag());
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
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view, savedInstanceState);
    }

    public void init(View v, Bundle savedInstanceState)
    {
        final Button uploadBtn = (Button) v.findViewById(R.id.uploadBtn);
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
        uploadBtn.setEnabled(false);
        editTitle = (EditText) v.findViewById(
                R.id.edit_title);
        editTags = (EditText) v.findViewById(
                R.id.edit_tags);
        albumsText = ((EditText) v.findViewById(R.id.edit_albums));
        if (savedInstanceState != null)
        {
            albumsWrapper = savedInstanceState.getParcelable(SELECTED_ALBUMS);
        }
        if (albumsWrapper != null)
        {
            albumsText.setTag(albumsWrapper);
        }
        privateSwitch = (Switch) v.findViewById(R.id.private_switch);
        twitterSwitch = (Switch) v.findViewById(R.id.twitter_switch);
        facebookSwitch = (Switch) v.findViewById(R.id.facebook_switch);

        albumsText.setOnClickListener(this);
        privateSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reinitShareSwitches(!isChecked);
            }
        });
        reinitShareSwitches();
        AccountLimitUtils.checkQuotaPerUploadAvailableAndRunAsync(
                new Runnable() {

                    @Override
                    public void run() {
                        CommonUtils.debug(TAG, "Upload limit check passed");
                        TrackerUtils.trackLimitEvent("sync_upload_enabled_check", "success");
                        uploadBtn.setEnabled(true);
                    }
                },
                new Runnable() {

                    @Override
                    public void run() {
                        CommonUtils.debug(TAG, "Upload limit check failed");
                        TrackerUtils.trackLimitEvent("sync_upload_enabled_check", "fail");
                    }
                },
                previousStepFlow.getSelectedCount(),
                loadingControl);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_albums: {
                TrackerUtils.trackButtonClickEvent("select_albums", SyncUploadFragment.this);
                Intent i = new Intent(getActivity(), SelectAlbumsActivity.class);
                i.putExtra(SelectAlbumsActivity.SELECTED_ALBUMS,
                        (Parcelable) albumsText.getTag());
                startActivityForResult(i, REQUEST_ALBUMS);
            }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ALBUMS:
                if (resultCode == Activity.RESULT_OK && data.getExtras() != null) {
                    StringMapParcelableWrapper albumsWrapper = data.getExtras().getParcelable(
                            SelectAlbumsActivity.SELECTED_ALBUMS);
                    Map<String, String> albums = albumsWrapper.getMap();
                    albumsText.setText(AlbumUtils.getAlbumsString(albums));
                    albumsText.setTag(albumsWrapper);
                    this.albumsWrapper = albumsWrapper;
                }
                break;
            default:
                break;
        }
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
        /**
         * Activate previous step in the sync flow
         */
        void activatePreviousStep();

        /**
         * Get selected images file names
         * 
         * @return
         */
        ArrayList<String> getSelectedFileNames();

        /**
         * Get the selected images count
         * 
         * @return
         */
        int getSelectedCount();
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
                StringMapParcelableWrapper albumsWrapper = (StringMapParcelableWrapper) albumsText
                        .getTag();
                if (albumsWrapper != null)
                {
                    metaData.setAlbums(albumsWrapper.getMap());
                }

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
