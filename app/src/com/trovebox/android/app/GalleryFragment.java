
package com.trovebox.android.app;

import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.trovebox.android.app.NavigationHandlerFragment.TitleChangedHandler;
import com.trovebox.android.app.PhotoDetailsActivity.PhotoDetailsUiFragment;
import com.trovebox.android.app.model.utils.AlbumUtils;
import com.trovebox.android.app.net.ProfileResponseUtils;
import com.trovebox.android.common.model.Album;
import com.trovebox.android.common.model.Photo;
import com.trovebox.android.common.model.ProfileInformation;
import com.trovebox.android.common.model.ProfileInformation.AccessPermissions;
import com.trovebox.android.common.net.ReturnSizes;
import com.trovebox.android.common.ui.adapter.PhotosEndlessAdapter;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.ImageFlowUtils.ViewHolder;
import com.trovebox.android.common.util.RunnableWithParameter;
import com.trovebox.android.common.util.TrackerUtils;

public class GalleryFragment extends com.trovebox.android.common.fragment.gallery.GalleryFragment {

    public GalleryFragment() {
        super(R.layout.fragment_gallery, true);
    }

    private TitleChangedHandler mTitleChangedHandler;
    private StartNowHandler startNowHandler;
    private boolean mSkipPermissionsCheck;

    private CollaboratorAlbumRunnable mCollaboratorAlbumRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (savedInstanceState == null) {
            mSkipPermissionsCheck = false;
        }
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        startNowHandler = ((StartNowHandler) activity);
        mTitleChangedHandler = ((TitleChangedHandler) activity);
    
    }

    @Override
    protected ReturnSizes getReturnSizes(ReturnSizes thumbSize) {
        return PhotoDetailsUiFragment.getReturnSizes(thumbSize,
                PhotoDetailsUiFragment.getDetailsReturnSizes(getActivity()));
    }

    @Override
    protected GalleryAdapterExt createGalleryAdapterForNoParams() {
        if (Preferences.isLimitedAccountAccessType() && !mSkipPermissionsCheck) {
            galleryAdapter = null;
            mCollaboratorAlbumRunnable = new CollaboratorAlbumRunnable();
            ProfileResponseUtils.runWithProfileInformationAsync(true, mCollaboratorAlbumRunnable,
                    null, loadingControl);
        } else {
            galleryAdapter = new GalleryAdapterExt();
        }
        return galleryAdapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCollaboratorAlbumRunnable != null) {
            mCollaboratorAlbumRunnable.cancel();
        }
    }

    @Override
    public void pageDeactivated() {
        super.pageDeactivated();
        if (mCollaboratorAlbumRunnable != null) {
            mCollaboratorAlbumRunnable.cancel();
        }
    };

    @Override
    protected void additionalSingleImageViewInit(View view, final Photo value) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        ImageView imageView = viewHolder.getImageView();
        imageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TrackerUtils.trackButtonClickEvent("image", GalleryFragment.this);
                Intent intent = new Intent(getActivity(), PhotoDetailsActivity.class);
                intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_PHOTOS,
                        new PhotosEndlessAdapter.ParametersHolder(galleryAdapter, value));
                startActivity(intent);
                clearImageWorkerCaches(true);
            }
        });
    }

    @Override
    protected void processLoadResponse(List<?> items) {
        // show start now notification in case response returned no items
        // and there are no already loaded items
        showStartNowNotification(startNowHandler, GalleryFragment.this,
                items != null && items.isEmpty() && galleryAdapter.getItems().isEmpty()
                        && currentTags == null && currentAlbum == null);
    }

    /**
     * Adjust start now notification visibility state and init it in case it is
     * visible. When user clicked on int startNowHandler.startNow will be
     * executed
     * 
     * @param startNowHandler
     * @param fragment
     * @param show
     */
    public static void showStartNowNotification(final StartNowHandler startNowHandler,
            final Fragment fragment, final boolean show) {
        GuiUtils.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                View view = fragment.getView();
                if (view != null) {
                    view = view.findViewById(R.id.upload_new_images);
                    if (show) {
                        view.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                startNowHandler.startNow();
                            }
                        });
                    }
                    view.setVisibility(show ? View.VISIBLE : View.GONE);
                }

            }
        });
    }

    class CollaboratorAlbumRunnable implements RunnableWithParameter<ProfileInformation> {

        boolean mCancelled = false;

        @Override
        public void run(ProfileInformation parameter) {
            if (mCancelled) {
                return;
            }
            try {
                ProfileInformation viewer = parameter.getViewer();
                AccessPermissions permissions = viewer == null ? null : viewer.getPermissions();
                if (permissions == null || permissions.isFullCreateAccess()
                        || permissions.getCreateAlbumAccessIds() == null
                        || permissions.getCreateAlbumAccessIds().length == 0) {
                    mSkipPermissionsCheck = true;
                    refresh();
                } else {
                    AlbumUtils.getAlbumAndRunAsync(permissions.getCreateAlbumAccessIds()[0],
                            new RunnableWithParameter<Album>() {

                                @Override
                                public void run(Album parameter) {
                                    if (!mCancelled) {
                                        currentAlbum = parameter;
                                        mTitleChangedHandler.titleChanged();
                                        refresh();
                                        cancel();
                                    }
                                }
                            }, new Runnable() {

                                @Override
                                public void run() {
                                    cancel();
                                }
                            }, loadingControl);
                }
            } catch (Exception ex) {
                GuiUtils.error(TAG, ex);
            }

        }

        void cancel() {
            mCancelled = true;
            if (mCollaboratorAlbumRunnable == this) {
                mSkipPermissionsCheck = false;
                mCollaboratorAlbumRunnable = null;
            }
        }
    }

    public static interface StartNowHandler {
        void startNow();
    }
}
