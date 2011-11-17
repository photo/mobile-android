/**
 * The search screen
 */

package me.openphoto.android.app;

import me.openphoto.android.app.model.Tag;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.TagsResponse;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.ui.widget.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * The search screen
 * 
 * @author Patrick Boos
 */
public class SearchActivity extends Activity implements OnItemClickListener {
    public static final String TAG = SearchActivity.class.getSimpleName();

    private ActionBar mActionBar;

    private TagsAdapter mAdapter;

    /**
     * Called when Search Activity is first loaded
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        mActionBar = (ActionBar) findViewById(R.id.actionbar);

        mAdapter = new TagsAdapter();
        ListView list = (ListView) findViewById(R.id.list_tags);
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(this);

        final EditText search = (EditText) findViewById(R.id.edit_search);
        search.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_ENTER:
                        if (KeyEvent.ACTION_DOWN == actionId) {
                            openGallery(search.getText().toString().trim());
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Tag tag = (Tag) mAdapter.getItem(position);
        openGallery(tag.getTag());
    }

    private void openGallery(String tag) {
        Intent intent = new Intent(this, GalleryActivity.class);
        intent.putExtra(GalleryActivity.EXTRA_TAG, tag);
        startActivity(intent);
    }

    private class TagsAdapter extends EndlessAdapter<Tag> {
        private final IOpenPhotoApi mOpenPhotoApi;

        public TagsAdapter() {
            super(Integer.MAX_VALUE);
            mOpenPhotoApi = Preferences.getApi(SearchActivity.this);
        }

        @Override
        public long getItemId(int position) {
            return ((Tag) getItem(position)).getTag().hashCode();
        }

        @Override
        public View getView(Tag tag, View convertView) {
            if (convertView == null) {
                final LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.tag_line, null);
            }
            ((TextView) convertView.findViewById(R.id.text_tag)).setText(tag.getTag());
            ((TextView) convertView.findViewById(R.id.text_count)).setText(Integer.toString(tag
                    .getCount()));
            return convertView;
        }

        @Override
        public LoadResponse loadItems(int page) {
            try {
                TagsResponse response = mOpenPhotoApi.getTags();
                return new LoadResponse(response.getTags(), false);
            } catch (Exception e) {
                Log.e(TAG, "Could not load next photos in list", e);
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
