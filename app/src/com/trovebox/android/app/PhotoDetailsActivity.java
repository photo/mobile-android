
package com.trovebox.android.app;

import java.lang.ref.WeakReference;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.trovebox.android.app.FacebookFragment.FacebookLoadingControlAccessor;
import com.trovebox.android.app.TwitterFragment.TwitterLoadingControlAccessor;
import com.trovebox.android.app.bitmapfun.util.ImageCacheUtils;
import com.trovebox.android.app.facebook.FacebookProvider;
import com.trovebox.android.app.facebook.FacebookUtils;
import com.trovebox.android.app.share.ShareUtils;
import com.trovebox.android.app.share.ShareUtils.TwitterShareRunnable;
import com.trovebox.android.app.twitter.TwitterUtils;
import com.trovebox.android.common.activity.CommonActivity;
import com.trovebox.android.common.fragment.photo_details.PhotoDetailsFragment;
import com.trovebox.android.common.model.Photo;
import com.trovebox.android.common.model.utils.PhotoUtils;
import com.trovebox.android.common.ui.widget.YesNoDialogFragment;
import com.trovebox.android.common.ui.widget.YesNoDialogFragment.YesNoButtonPressedHandler;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.LoadingControl;
import com.trovebox.android.common.util.ProgressDialogLoadingControl;
import com.trovebox.android.common.util.TrackerUtils;

/**
 * The general photo viewing screen
 * 
 * @author pboos
 */
public class PhotoDetailsActivity extends CommonActivity implements TwitterLoadingControlAccessor,
        FacebookLoadingControlAccessor {

    private static final String TAG = PhotoDetailsActivity.class.getSimpleName();

    public final static int AUTHORIZE_ACTIVITY_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up activity to go full screen
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new PhotoDetailsUiFragment())
                    .commit();
        }
        addRegisteredReceiver(ImageCacheUtils.getAndRegisterOnDiskCacheClearedBroadcastReceiver(
                TAG, this));
    }

    PhotoDetailsUiFragment getContentFragment()
    {
        return (PhotoDetailsUiFragment) getSupportFragmentManager().findFragmentById(
                android.R.id.content);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null)
        {
            if (intent.getData() != null)
            {
                Uri uri = intent.getData();
                TwitterUtils.verifyOAuthResponse(
                        new ProgressDialogLoadingControl(this, true,
                                false, getString(R.string.share_twitter_verifying_authentication)),
                        this,
                        uri,
                        TwitterUtils.getPhotoDetailsCallbackUrl(this),
                        null);
            }
            getContentFragment().reinitFromIntent(intent);
        }
    }

    @Override
    public LoadingControl getTwitterLoadingControl() {
        return new ProgressDialogLoadingControl(this, true, false, getString(R.string.loading));
    }

    @Override
    public LoadingControl getFacebookLoadingControl() {
        return new ProgressDialogLoadingControl(this, true, false, getString(R.string.loading));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
        /*
         * if this is the activity result from authorization flow, do a call
         * back to authorizeCallback Source Tag: login_tag
         */
            case AUTHORIZE_ACTIVITY_REQUEST_CODE: {
                FacebookProvider.getFacebook().authorizeCallback(requestCode,
                        resultCode,
                        data);
                break;
            }
        }
    }

    public static class PhotoDetailsUiFragment extends PhotoDetailsFragment
    {
        static WeakReference<PhotoDetailsUiFragment> sCurrentInstance;
        static CurrentInstanceManager<PhotoDetailsUiFragment> sCurrentInstanceManager = new CurrentInstanceManager<PhotoDetailsUiFragment>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setCurrentInstance(WeakReference<PhotoDetailsUiFragment> instance) {
                sCurrentInstance = instance;
            }

            @Override
            protected WeakReference<PhotoDetailsUiFragment> getCurrentInstance() {
                return sCurrentInstance;
            }

        };


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            sCurrentInstanceManager.onCreate(this);
            setHasOptionsMenu(true);
        }

        @Override
        public void onDestroy() {
            sCurrentInstanceManager.onDestroy(this, TAG);
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
            FacebookUtils.extendAceessTokenIfNeeded(getActivity());
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.photo_details, menu);
        }
        
        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            reinitMenu(menu);
            super.onPrepareOptionsMenu(menu);
        }

        protected void reinitMenu(Menu menu) {
            try {
                if (Preferences.isLimitedAccountAccessType()) {
                    MenuItem deleteItem = menu.findItem(R.id.menu_delete_parent);
                    deleteItem.setVisible(false);
                    MenuItem editItem = menu.findItem(R.id.menu_edit);
                    editItem.setVisible(false);
                }
            } catch (Exception ex) {
                GuiUtils.noAlertError(TAG, ex);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            detailsVisible = true;
            boolean result = true;
            switch (item.getItemId())
            {
                case R.id.menu_delete:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_delete", getSupportActivity());
                    deleteCurrentPhoto();
                    break;
                case R.id.menu_share:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_share", getSupportActivity());
                    break;
                case R.id.menu_share_email:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_share_email",
                            getSupportActivity());
                    confirmPrivatePhotoSharingAndRun(new Runnable() {

                        @Override
                        public void run() {
                            shareActivePhotoViaEMail();
                        }
                    });
                    break;
                case R.id.menu_share_system:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_share_system",
                            getSupportActivity());
                    confirmPrivatePhotoSharingAndRun(new Runnable() {

                        @Override
                        public void run() {
                            shareActivePhotoViaSystem();
                        }
                    });
                    break;
                case R.id.menu_share_twitter:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_share_twitter",
                            getSupportActivity());
                    confirmPrivatePhotoSharingAndRun(new Runnable() {

                        @Override
                        public void run() {
                            shareActivePhotoViaTwitter();
                        }
                    });
                    break;
                case R.id.menu_share_facebook:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_share_facebook",
                            getSupportActivity());
                    confirmPrivatePhotoSharingAndRun(new Runnable() {

                        @Override
                        public void run() {
                            shareActivePhotoViaFacebook();
                        }
                    });
                    break;
                case R.id.menu_edit:
                    TrackerUtils.trackOptionsMenuClickEvent("menu_edit", getSupportActivity());
                    PhotoDetailsEditFragment detailsFragment = new PhotoDetailsEditFragment();
                    detailsFragment.setPhoto(getActivePhoto());
                    detailsFragment.show(getSupportActivity());
                    break;
                default:
                    result = super.onOptionsItemSelected(item);
            }
            return result;
        }

        public void shareActivePhotoViaFacebook() {
            Photo photo = getActivePhoto();
            if (photo != null)
            {
                FacebookUtils.runAfterFacebookAuthentication(getSupportActivity(),
                        AUTHORIZE_ACTIVITY_REQUEST_CODE,
                        new ShareUtils.FacebookShareRunnable(
                                photo, sCurrentInstanceManager));
            }
        }

        public void shareActivePhotoViaTwitter() {
            Photo photo = getActivePhoto();
            if (photo != null)
            {
                TwitterUtils.runAfterTwitterAuthentication(
                        new ProgressDialogLoadingControl(getSupportActivity(), true, false,
                                getString(R.string.share_twitter_requesting_authentication)),
                        getSupportActivity(),
                        TwitterUtils.getPhotoDetailsCallbackUrl(getActivity()),
                        new TwitterShareRunnable(photo, sCurrentInstanceManager));
            }
        }

        public void shareActivePhotoViaEMail() {
            Photo photo = getActivePhoto();
            if (photo != null)
            {
                ShareUtils.shareViaEMail(photo, getActivity(),
                        new ProgressDialogLoadingControl(
                                getSupportActivity(), true, false,
                                getString(R.string.loading)));
            }
        }

        public void shareActivePhotoViaSystem() {
            Photo photo = getActivePhoto();
            if (photo != null)
            {
                ShareUtils.shareViaSystem(photo, getActivity(),
                        new ProgressDialogLoadingControl(
                                getSupportActivity(), true, false,
                                getString(R.string.loading)));
            }
        }

        public void confirmPrivatePhotoSharingAndRun(final Runnable runnable)
        {
            ShareUtils.confirmPrivatePhotoSharingAndRun(getActivePhoto(), runnable,
                    getSupportActivity());
        }

        void deleteCurrentPhoto()
        {
            final Photo photo = getActivePhoto();
            YesNoDialogFragment dialogFragment = YesNoDialogFragment
                    .newInstance(R.string.delete_photo_confirmation_question,
                            new YesNoButtonPressedHandler()
                            {
                                @Override
                                public void yesButtonPressed(
                                        DialogInterface dialog)
                                {
                                    final ProgressDialogLoadingControl loadingControl = new ProgressDialogLoadingControl(
                                            getActivity(), true, false,
                                            getString(R.string.deleting_photo_message)
                                            );
                                    PhotoUtils.deletePhoto(photo,
                                            loadingControl);
                                }

                                @Override
                                public void noButtonPressed(
                                        DialogInterface dialog)
                                {
                                    // DO NOTHING
                                }
                            });
            dialogFragment.show(getSupportActivity());
        }

    }

}
