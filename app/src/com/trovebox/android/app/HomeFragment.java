
package com.trovebox.android.app;

import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.ContextMenu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.trovebox.android.app.bitmapfun.util.ImageCache;
import com.trovebox.android.app.bitmapfun.util.ImageFetcher;
import com.trovebox.android.app.common.CommonRefreshableFragmentWithImageWorker;
import com.trovebox.android.app.facebook.FacebookBaseDialogListener;
import com.trovebox.android.app.facebook.FacebookUtils;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.model.utils.PhotoUtils;
import com.trovebox.android.app.model.utils.PhotoUtils.PhotoDeletedHandler;
import com.trovebox.android.app.model.utils.PhotoUtils.PhotoUpdatedHandler;
import com.trovebox.android.app.net.ReturnSizes;
import com.trovebox.android.app.net.TroveboxApi;
import com.trovebox.android.app.share.ShareUtils;
import com.trovebox.android.app.share.ShareUtils.FacebookShareRunnable;
import com.trovebox.android.app.share.ShareUtils.TwitterShareRunnable;
import com.trovebox.android.app.twitter.TwitterUtils;
import com.trovebox.android.app.ui.adapter.PhotosEndlessAdapter;
import com.trovebox.android.app.ui.widget.AspectRatioImageView;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.ProgressDialogLoadingControl;
import com.trovebox.android.app.util.RunnableWithParameter;
import com.trovebox.android.app.util.TrackerUtils;

public class HomeFragment extends CommonRefreshableFragmentWithImageWorker
        implements PhotoDeletedHandler, PhotoUpdatedHandler
{
    public static final String TAG = HomeFragment.class.getSimpleName();

    private LoadingControl loadingControl;
    private StartNowHandler startNowHandler;
    private NewestPhotosAdapter mAdapter;
    private LayoutInflater mInflater;
    private Photo activePhoto;

    private ListView list;
    private ReturnSizes photoSize;
    private ReturnSizes returnSizes;

    float aspectRatio = 14f / 13f;

    static HomeFragment currentInstance;

    static FragmentAccessor<HomeFragment> currentInstanceAccessor = new FragmentAccessor<HomeFragment>() {
        private static final long serialVersionUID = 1L;

        @Override
        public HomeFragment run() {
            return currentInstance;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        currentInstance = this;
    }

    @Override
    public void onDestroy() {
        currentInstance = null;
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mInflater = inflater;

        refresh(v);
        return v;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);
        startNowHandler = ((StartNowHandler) activity);

    }

    @Override
    protected void initImageWorker() {
        // Fetch screen height and width, to use as our max size when loading
        // images as this
        // activity runs full screen
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(displaymetrics);
        final int height = displaymetrics.heightPixels;
        final int width = displaymetrics.widthPixels;
        final int longest = height > width ? height : width;

        photoSize = new ReturnSizes(longest, (int) (longest / aspectRatio), true);

        returnSizes = PhotosEndlessAdapter.getReturnSizes(photoSize,
                PhotosEndlessAdapter.getDetailsReturnSizes(getActivity()));
        // The ImageWorker takes care of loading images into our ImageView
        // children asynchronously
        mImageWorker = new ImageFetcher(getActivity(), loadingControl, longest);
        mImageWorker.setLoadingImage(R.drawable.empty_photo);
        mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                ImageCache.LARGE_IMAGES_CACHE_DIR));
        mImageWorker.setImageFadeIn(false);
    }

    @Override
    public void refresh()
    {
        refresh(getView());
    }

    public void refresh(View view)
    {
        mAdapter = new NewestPhotosAdapter(getActivity());
        list = (ListView) view.findViewById(R.id.list_newest_photos);
        list.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mAdapter.forceStopLoadingIfNecessary();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo)
    {
        if (v.getId() == R.id.share_button)
        {
            MenuInflater inflater = getSupportActivity()
                    .getSupportMenuInflater();
            inflater.inflate(R.menu.share, menu);
            super.onCreateContextMenu(menu, v, menuInfo);
        } else
        {
            super.onCreateContextMenu(menu, v, menuInfo);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        int menuItemIndex = item.getItemId();
        switch (menuItemIndex)
        {
            case R.id.menu_share_email:
                shareViaEMail(activePhoto);
                break;
            case R.id.menu_share_twitter:
                shareActivePhotoViaTwitter();
                break;
            case R.id.menu_share_facebook:
                shareActivePhotoViaFacebook();
                break;
        }
        return true;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        FacebookUtils.extendAceessTokenIfNeeded(getActivity());
    }

    private void shareViaEMail(Photo photo)
    {
        ShareUtils.shareViaEMail(photo, getActivity());
    }

    public void shareActivePhotoViaTwitter()
    {
        if (activePhoto != null)
        {
            TwitterUtils.runAfterTwitterAuthentication(
                    new ProgressDialogLoadingControl(getActivity(), true, false,
                            getString(R.string.share_twitter_requesting_authentication)),
                    getSupportActivity(),
                    new TwitterShareRunnable(activePhoto, currentInstanceAccessor));
        }
    }

    public void shareActivePhotoViaFacebook()
    {
        if (activePhoto != null)
        {
            FacebookUtils.runAfterFacebookAuthentication(getSupportActivity(),
                    MainActivity.AUTHORIZE_ACTIVITY_REQUEST_CODE,
                    new FacebookShareRunnable(
                            activePhoto, currentInstanceAccessor));
        }
    }

    @Override
    public void photoDeleted(Photo photo)
    {
        if (mAdapter != null)
        {
            mAdapter.photoDeleted(photo);
        }
    }

    @Override
    public void photoUpdated(Photo photo) {
        if (mAdapter != null)
        {
            mAdapter.photoUpdated(photo);
        }
    }

    public static class UpdateStatusListener extends FacebookBaseDialogListener
    {
        public UpdateStatusListener(Context context)
        {
            super(context);
        }

        @Override
        public void onComplete(Bundle values)
        {
            final String postId = values.getString("post_id");
            if (postId != null)
            {
                GuiUtils.info(R.string.share_facebook_success_message);
            } else
            {
                GuiUtils.info(R.string.share_facebook_no_wall_post_made);
            }
        }

        @Override
        public void onCancel()
        {
            GuiUtils.info(R.string.share_facebook_share_canceled_message);
        }
    }

    private class NewestPhotosAdapter extends PhotosEndlessAdapter
    {
        private Stack<Button> unusedTagButtons = new Stack<Button>();

        public NewestPhotosAdapter(Context context)
        {
            super(context, 25, null, null, TroveboxApi.NEWEST_PHOTO_SORT_ORDER, returnSizes);
        }

        @Override
        public View getView(final Photo photo, View convertView,
                ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = mInflater.inflate(
                        R.layout.list_item_newest_photos, parent, false);
            }

            // load the image in another thread
            final AspectRatioImageView photoView =
                    (AspectRatioImageView) convertView.findViewById(R.id.newest_image);
            photoView.setAspectRatio(aspectRatio);
            photoView.setTag(photo);
            photoView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    TrackerUtils.trackButtonClickEvent("newest_image", HomeFragment.this);
                    Intent intent = new Intent(getActivity(), PhotoDetailsActivity.class);
                    intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_PHOTOS,
                            new PhotosEndlessAdapter.ParametersHolder(mAdapter, photo));
                    startActivity(intent);
                    clearImageWorkerCaches(true);
                }
            });
            PhotoUtils.validateUrlForSizeExistAsyncAndRun(photo, photoSize,
                    new RunnableWithParameter<Photo>() {

                        @Override
                        public void run(Photo photo) {
                            mImageWorker
                                    .loadImage(photo.getUrl(photoSize.toString()), photoView);
                        }
                    }, loadingControl);
            // photoView.setTag(photo.getUrl("700x650xCR"));
            // Drawable dr =
            // iw.loadImage(this, photoView);
            // photoView.setImageDrawable(dr);

            // set title or file's name
            if (photo.getTitle() != null && photo.getTitle().trim().length()
                    > 0)
            {
                ((TextView) convertView.findViewById(R.id.newest_title))
                        .setText
                        (photo.getTitle());
            } else
            {
                ((TextView) convertView.findViewById(R.id.newest_title))
                        .setText(photo
                                .getFilenameOriginal());
            }

            /*
             * set the date
             */
            Resources res = getResources();
            String text = null;

            long milliseconds = new Date().getTime()
                    - photo.getDateTaken().getTime();
            long days = milliseconds / (24 * 60 * 60 * 1000);

            if (days >= 2)
            {
                if (days > 365)
                {
                    // show in years
                    text = days / 365 == 1 ? String
                            .format(
                                    res.getString(R.string.newest_this_photo_was_taken),
                                    days / 365,
                                    res.getString(R.string.year))
                            :
                            String.format(
                                    res.getString(R.string.newest_this_photo_was_taken),
                                    days / 365, res.getString(R.string.years));
                } else
                {
                    // lets show in days
                    text = String
                            .format(res
                                    .getString(R.string.newest_this_photo_was_taken),
                                    days,
                                    res.getString(R.string.days));
                }
            } else
            {
                // lets show in hours
                Long hours = days * 24;
                if (hours < 1)
                {
                    text = String
                            .format(res
                                    .getString(R.string.newest_this_photo_was_taken_less_one_hour));
                } else
                {
                    if (hours == 1)
                    {
                        text = String
                                .format(res
                                        .getString(R.string.newest_this_photo_was_taken),
                                        1, res.getString(R.string.hour));
                    } else
                    {
                        text = String
                                .format(res
                                        .getString(R.string.newest_this_photo_was_taken),
                                        hours, res.getString(R.string.hours));
                    }
                }
            }

            // set the correct text in the textview
            ((TextView) convertView.findViewById(R.id.newest_date))
                    .setText(text);

            // tags
            showTags(photo, convertView);

            View privateButton = convertView.findViewById(R.id.button_private);
            privateButton.setVisibility(photo.isPrivate() ? View.VISIBLE
                    : View.INVISIBLE);

            ImageView geoButton = (ImageView) convertView
                    .findViewById(R.id.geo_button);
            if (photo.getLongitude() != null &&
                    photo.getLatitude() != null &&
                    photo.getLongitude().length() != 0
                    && photo.getLatitude().length() != 0)
            {
                geoButton.setImageResource(R.drawable.button_location_share);
                geoButton.setTag(photo);
                geoButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        TrackerUtils.trackButtonClickEvent("button_location_share",
                                HomeFragment.this);
                        Photo photo = (Photo) view.getTag();
                        Uri uri = Uri.parse("geo:" + photo.getLatitude() + ","
                                + photo.getLongitude());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        try
                        {
                            startActivity(intent);
                        } catch (Exception e)
                        {
                            GuiUtils.error(TAG,
                                    R.string.errorCouldNotUseIntentsToOpenMaps,
                                    e);
                        }
                    }
                });
            } else
            {
                geoButton.setImageResource(R.drawable.button_nolocation_share);
            }
            final ImageView shareButton = (ImageView) convertView
                    .findViewById(R.id.share_button);
            shareButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    TrackerUtils.trackButtonClickEvent("share_button", HomeFragment.this);
                    activePhoto = photo;
                    if (photo.isPrivate())
                    {
                        GuiUtils.alert(R.string.share_private_photo_forbidden);
                    } else
                    {
                        registerForContextMenu(v);
                        v.showContextMenu();
                        unregisterForContextMenu(v);
                    }
                }
            });
            return convertView;
        }

        public void showTags(final Photo photo, View convertView) {
            List<String> tags = photo.getTags();
            int tagsSize = tags == null ? 0 : tags.size();
            ViewGroup tagsView = (ViewGroup) convertView
                    .findViewById(R.id.newest_tag_layout);
            int childCount = tagsView.getChildCount();
            if (tags != null)
            {
                for (int i = 0; i < tagsSize; i++)
                {
                    boolean add = false;
                    if (i < childCount)
                    {
                        convertView = tagsView.getChildAt(i);
                    } else
                    {
                        if (!unusedTagButtons.isEmpty())
                        {
                            CommonUtils.debug(TAG, "Reusing tag button from the stack");
                            convertView = unusedTagButtons.pop();
                        } else
                        {
                            convertView = (Button) mInflater.inflate(
                                    R.layout.tag_btn, tagsView, false);
                        }
                        add = true;
                    }
                    Button tagBtn = (Button) convertView;
                    tagBtn.setText(tags.get(i));
                    if (add)
                    {
                        tagsView.addView(convertView);
                    }
                }
            }
            for (int i = childCount - 1; i >= tagsSize; i--)
            {
                Button tagBtn = (Button) tagsView.getChildAt(i);
                unusedTagButtons.add(tagBtn);
                tagsView.removeViewAt(i);

            }
        }

        @Override
        protected void onStartLoading()
        {
            loadingControl.startLoading();
        }

        @Override
        protected void onStoppedLoading()
        {
            loadingControl.stopLoading();
        }

        @Override
        public LoadResponse loadItems(int page) {
            LoadResponse result = super.loadItems(page);
            // show start now notification in case response returned no items
            // and there are no already loaded items
            showStartNowNotification(startNowHandler,
                    HomeFragment.this, result.items != null && result.items.isEmpty()
                            && getItems().isEmpty());
            return result;
        }

    }

    @Override
    protected boolean isRefreshMenuVisible() {
        return !loadingControl.isLoading();
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
            final Fragment fragment,
            final boolean show)
    {
        GuiUtils.runOnUiThread(
                new Runnable() {

                    @Override
                    public void run() {
                        View view = fragment.getView();
                        if (view != null)
                        {
                            view = view.findViewById(R.id.upload_new_images);
                            if (show)
                            {
                                view.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        startNowHandler.startNow();
                                    }
                                });
                            }
                            view.setVisibility(
                                    show ? View.VISIBLE : View.GONE);
                        }

                    }
                });
    }

    public static interface StartNowHandler
    {
        void startNow();
    }
}
