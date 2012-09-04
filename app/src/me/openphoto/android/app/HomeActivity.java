
package me.openphoto.android.app;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.ui.widget.ActionBar;
import me.openphoto.android.app.util.ImageWorker;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

/**
 * The home activity - screen
 * 
 * @author Patrick Boos
 */
public class HomeActivity extends Activity {
    public static final String TAG = HomeActivity.class.getSimpleName();
    static final private int EXIT_ID = Menu.FIRST;

    private ActionBar mActionBar;
    private NewestPhotosAdapter mAdapter;
    private LayoutInflater mInflater;
    private ImageWorker iw;

    /**
     * Called when Home Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mInflater = LayoutInflater.from(this);
        mActionBar = (ActionBar) getParent().findViewById(R.id.actionbar);
        findViewById(R.id.actionbar).setVisibility(View.GONE);

        mAdapter = new NewestPhotosAdapter(this);
        ListView list = (ListView) findViewById(R.id.list_newest_photos);
        list.setAdapter(mAdapter);

        iw = new ImageWorker(this, mActionBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        /* the context menu currently has only one option */
        menu.add(0, EXIT_ID, 0, R.string.menu_exit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case EXIT_ID:
                finish(); /* terminate the application */
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void alert(String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private class NewestPhotosAdapter extends EndlessAdapter<Photo> {
        private final IOpenPhotoApi mOpenPhotoApi;
        private final Context mContext;

        public NewestPhotosAdapter(Context context) {
            super(Integer.MAX_VALUE);
            mOpenPhotoApi = Preferences.getApi(HomeActivity.this);
            mContext = context;
            loadFirstPage();
        }

        @Override
        public long getItemId(int position) {
            return ((Photo) getItem(position)).getId().hashCode();
        }

        @Override
        public View getView(Photo photo, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_newest_photos, parent, false);
            }

            // load the image in another thread
            ImageView photoView =
                    (ImageView) convertView.findViewById(R.id.newest_image);
            photoView.setTag(photo.getUrl("700x650xCR"));
            Drawable dr =
                    iw.loadImage(this, photoView);
            photoView.setImageDrawable(dr);

            // set title or file's name
            if (photo.getTitle() != null && photo.getTitle().trim().length()
                    > 0) {
                ((TextView) convertView.findViewById(R.id.newest_title)).setText
                        (photo.getTitle());
            } else {
                ((TextView) convertView.findViewById(R.id.newest_title)).setText(photo
                        .getFilenameOriginal());
            }

            /*
             * set the date
             */
            Resources res = getResources();
            String text = null;

            long milliseconds = new Date().getTime() - photo.getDateTaken().getTime();
            long days = milliseconds / (24 * 60 * 60 * 1000);

            if (days >= 2) {
                if (days > 365) {
                    // show in years
                    text = days / 365 == 1 ? String.format(
                            res.getString(R.string.newest_this_photo_was_taken), days / 365,
                            res.getString(R.string.year)) :
                            String.format(res.getString(R.string.newest_this_photo_was_taken),
                                    days / 365, res.getString(R.string.years));
                } else {
                    // lets show in days
                    text = String.format(res.getString(R.string.newest_this_photo_was_taken), days,
                            res.getString(R.string.days));
                }
            } else {
                // lets show in hours
                Long hours = days * 24;
                if (hours < 1) {
                    text = String.format(res
                            .getString(R.string.newest_this_photo_was_taken_less_one_hour));
                } else {
                    if (hours == 1) {
                        text = String.format(res.getString(R.string.newest_this_photo_was_taken),
                                1, res.getString(R.string.hour));
                    } else {
                        text = String.format(res.getString(R.string.newest_this_photo_was_taken),
                                hours, res.getString(R.string.hours));
                    }
                }
            }

            // set the correct text in the textview
            ((TextView) convertView.findViewById(R.id.newest_date))
                    .setText(text);

            // tags
            /*
             * LinearLayout linearLayout = (LinearLayout) convertView
             * .findViewById(R.id.newest_tag_layout); List<String> list = new
             * ArrayList<String>(); list.add("2012"); list.add("Bla Bla");
             * LinearLayout linearLayoutButton = (LinearLayout) convertView
             * .findViewById(R.layout.my_special_button); if (linearLayoutButton
             * == null) linearLayoutButton = (LinearLayout)
             * mInflater.inflate(R.layout.my_special_button, null, false);
             * Button btn = (Button)
             * linearLayoutButton.findViewById(R.id.special_button);
             * btn.setText("tag2"); linearLayout.addView(btn);
             */
            return convertView;
        }

        @Override
        public LoadResponse loadItems(int page) {
            if (Preferences.isLoggedIn(mContext)) {
                try {
                    PhotosResponse response = mOpenPhotoApi.getNewestPhotos(new Paging(page, 25));
                    return new LoadResponse(response.getPhotos(), false);
                } catch (Exception e) {
                    Log.e(TAG, "Could not load next photos in list", e);
                    Map<String, String> extraData = new HashMap<String, String>();
                    extraData.put("message", "Could not load next photos in list for HomeActivity");
                    BugSenseHandler.log(TAG, extraData, e);
                    alert("Could not load next photos in list");
                }
            }
            return new LoadResponse(null, false);
        }

        @Override
        protected void onStartLoading() {
            mActionBar.startLoading();
        }

        @Override
        protected void onStoppedLoading() {
            mActionBar.stopLoading();
        }
    }
}
