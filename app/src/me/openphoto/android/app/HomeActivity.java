
package me.openphoto.android.app;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

/**
 * The home activity - screen
 * 
 * @author Patrick Boos
 */
public class HomeActivity extends Activity {
    public static final String TAG = HomeActivity.class.getSimpleName();

    private ActionBar mActionBar;
    private NewestPhotosAdapter mAdapter;

    /**
     * Called when Home Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mActionBar = (ActionBar) getParent().findViewById(R.id.actionbar);

        mAdapter = new NewestPhotosAdapter(this);
        ListView list = (ListView) findViewById(R.id.list_newest_photos);
        list.setAdapter(mAdapter);
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
        public View getView(Photo photo, View convertView) {

            if (convertView == null) {
                final LayoutInflater layoutInflater =
                        (LayoutInflater)
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView =
                        layoutInflater.inflate(R.layout.list_item_newest_photos, null);
            }

            /*
             * set image try { ImageView i = (ImageView)
             * findViewById(R.id.newest_image); Bitmap bitmap =
             * BitmapFactory.decodeStream((InputStream) new URL(photo
             * .getUrl("305x265xCR")) .getContent()); i.setImageBitmap(bitmap);
             * } catch (MalformedURLException e) { e.printStackTrace(); } catch
             * (IOException e) { e.printStackTrace(); }
             */

            // set title or file's name
            if (photo.getTitle() != null && photo.getTitle().trim().length() > 0)
                ((TextView) convertView.findViewById(R.id.newest_title)).setText(photo.getTitle());
            else
                ((TextView) convertView.findViewById(R.id.newest_title)).setText(photo
                        .getFilenameOriginal());

            // set the date
            DateFormat df = DateFormat.getDateTimeInstance();
            ((TextView) convertView.findViewById(R.id.newest_date))
                    .setText(df.format(photo.getDataTaken()));

            return convertView;
        }

        @Override
        public LoadResponse loadItems(int page) {
            try {
                PhotosResponse response = mOpenPhotoApi.getNewestPhotos(new Paging(page, 25));
                return new LoadResponse(response.getPhotos(), false);
            } catch (Exception e) {
                Log.e(TAG, "Could not load next photos in list", e);
                Map<String, String> extraData = new HashMap<String, String>();
                extraData.put("message", "Could not load next photos in list for HomeActivity");
                BugSenseHandler.log(TAG, extraData, e);
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
