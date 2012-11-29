
package me.openphoto.android.app;

import java.util.Date;
import java.util.List;
import java.util.Stack;

import me.openphoto.android.app.bitmapfun.util.ImageCache;
import me.openphoto.android.app.bitmapfun.util.ImageFetcher;
import me.openphoto.android.app.bitmapfun.util.ImageWorker;
import me.openphoto.android.app.facebook.FacebookBaseDialogListener;
import me.openphoto.android.app.facebook.FacebookUtils;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.share.ShareUtils;
import me.openphoto.android.app.share.ShareUtils.FacebookShareRunnable;
import me.openphoto.android.app.share.ShareUtils.TwitterShareRunnable;
import me.openphoto.android.app.twitter.TwitterUtils;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.ProgressDialogLoadingControl;
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
import android.widget.AbsListView.RecyclerListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.app.Activity;
import com.actionbarsherlock.view.ContextMenu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.android.R;

public class HomeFragment extends CommonFrargmentWithImageWorker implements Refreshable
{
    public static final String TAG = HomeFragment.class.getSimpleName();

    private LoadingControl loadingControl;
    private NewestPhotosAdapter mAdapter;
    private LayoutInflater mInflater;
    private Photo activePhoto;

    private ListView list;
    private ReturnSizes returnSizes;

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
        float aspectRatio = 14f / 13f;

        returnSizes = new ReturnSizes(longest, (int) (longest / aspectRatio), true);
        // The ImageWorker takes care of loading images into our ImageView
        // children asynchronously
        mImageWorker = new ImageFetcher(getActivity(), loadingControl, longest);
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
        list.setRecyclerListener(new RecyclerListener() {

            @Override
            public void onMovedToScrapHeap(View view) {
                CommonUtils.debug(TAG, "Moved to scrap: " + view);
                ImageView photoView =
                        (ImageView) view.findViewById(R.id.newest_image);
                Photo object = (Photo) photoView.getTag();
                if (object != null)
                {
                    ImageWorker.cancelPotentialWork(object, photoView);
                    // mImageWorker.recycleOldBitmap(object, photoView);
                }
            }
        });
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
                    getActivity(),
                    new TwitterShareRunnable(activePhoto, currentInstanceAccessor));
        }
    }

    public void shareActivePhotoViaFacebook()
    {
        if (activePhoto != null)
        {
            FacebookUtils.runAfterFacebookAuthentication(getActivity(),
                    new FacebookShareRunnable(
                            activePhoto, currentInstanceAccessor));
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

    private class NewestPhotosAdapter extends EndlessAdapter<Photo>
    {
        private final IOpenPhotoApi mOpenPhotoApi;
        private final Context mContext;
        private Stack<Button> unusedTagButtons = new Stack<Button>();

        public NewestPhotosAdapter(Context context)
        {
            super(Integer.MAX_VALUE);
            mOpenPhotoApi = Preferences.getApi(getActivity());
            mContext = context;
            loadFirstPage();
        }

        @Override
        public long getItemId(int position)
        {
            return ((Photo) getItem(position)).getId().hashCode();
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
            ImageView photoView =
                    (ImageView) convertView.findViewById(R.id.newest_image);
            photoView.setTag(photo);
            mImageWorker
                    .loadImage(photo.getUrl(returnSizes.toString()), photoView);
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
        public LoadResponse loadItems(int page)
        {
            if (CommonUtils.checkLoggedInAndOnline())
            {
                try
                {
                    PhotosResponse response = mOpenPhotoApi
                            .getNewestPhotos(returnSizes, new Paging(page, 25));
                    return new LoadResponse(response.getPhotos(), response.hasNextPage());
                } catch (Exception e)
                {
                    GuiUtils.error(
                            TAG,
                            R.string.errorCouldNotLoadNextPhotosInList,
                            e, mContext);
                }
            }
            return new LoadResponse(null, false);
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
    }
}
