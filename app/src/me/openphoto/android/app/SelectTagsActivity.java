
package me.openphoto.android.app;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.openphoto.android.app.model.Tag;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.TagsResponse;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.util.LoadingControl;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.sherlock.SActivity;
import com.bugsense.trace.BugSenseHandler;
import com.facebook.android.R;

public class SelectTagsActivity extends SActivity {

    public static final String TAG = SelectTagsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new UiFragment())
                    .commit();
        }
    }

    public static class UiFragment extends CommonFragment
            implements LoadingControl
    {
        private int mLoaders = 0;

        private TagsAdapter mAdapter;
        private ListView list;

        @Override
        public View onCreateView(LayoutInflater inflater,
                ViewGroup container, Bundle savedInstanceState)
        {
            super.onCreateView(inflater, container, savedInstanceState);
            View v = inflater.inflate(R.layout.activity_select_tags, container, false);
            return v;
        }

        @Override
        public void onViewCreated(View view) {
            super.onViewCreated(view);
            init(view);
        }
        void init(View v)
        {
            mAdapter = new TagsAdapter();
            list = (ListView) v.findViewById(R.id.list_select_tags);

            list.setAdapter(mAdapter);

            Button uploadBtn = (Button) v.findViewById(R.id.finishBtn);
            uploadBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    finishedClicked(v);
                }
            });
        }

        /**
         * Return selected Tags to the upload activity
         * 
         * @param v
         */
        public void finishedClicked(View v) {

            Intent data = new Intent();
            String selectedTags = mAdapter.getSelectedTags().trim();

            data.putExtra("SELECTED_TAGS", selectedTags);
            getActivity().setResult(RESULT_OK, data);
            getActivity().finish();

        }

        private class TagsAdapter extends EndlessAdapter<Tag> implements
                OnCheckedChangeListener {
            private final IOpenPhotoApi mOpenPhotoApi;
            private Set<String> checkedTags = new HashSet<String>();

            public TagsAdapter() {
                super(Integer.MAX_VALUE);
                mOpenPhotoApi = Preferences.getApi(getActivity());
                loadFirstPage();
            }

            @Override
            public long getItemId(int position) {
                return ((Tag) getItem(position)).getTag().hashCode();
            }

            @Override
            public View getView(Tag tag, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    final LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = layoutInflater.inflate(
                            R.layout.list_item_tag_checkbox, null);
                }

                CheckBox checkBox = (CheckBox) convertView
                        .findViewById(R.id.tag_checkbox);
                checkBox.setText(tag.getTag());
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(isChecked(tag.getTag()));
                checkBox.setOnCheckedChangeListener(this);

                return convertView;
            }

            @Override
            public LoadResponse loadItems(int page) {
                if (checkLoggedInAndOnline()) {
                    try {
                        TagsResponse response = mOpenPhotoApi.getTags();
                        return new LoadResponse(response.getTags(), false);
                    } catch (Exception e) {
                        Log.e(TAG, "Could not load next tags in list", e);
                        Map<String, String> extraData = new HashMap<String, String>();
                        extraData.put("message",
                                "Could not load next tags in list");
                        BugSenseHandler.log(TAG, extraData, e);
                    }
                }
                return new LoadResponse(null, false);
            }

            @Override
            protected void onStartLoading() {
                startLoading();
            }

            @Override
            protected void onStoppedLoading() {
                stopLoading();
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {

                String text = (String) buttonView.getText();
                if (isChecked)
                    checkedTags.add(text);
                else
                    checkedTags.remove(text);

            }

            boolean isChecked(String tag)
            {
                if (tag == null)
                {
                    return false;
                }
                return checkedTags.contains(tag);
            }

            public String getSelectedTags() {

                StringBuffer buf = new StringBuffer("");

                if (checkedTags.size() > 0) {

                    for (String tagText : checkedTags) {
                        buf.append(tagText).append(",");
                    }

                    // remove last comma
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }

        }

        @Override
        public void startLoading()
        {
            if (mLoaders++ == 0)
            {
                showLoading(true);
            }
        }

        @Override
        public void stopLoading()
        {
            if (--mLoaders == 0)
            {
                showLoading(false);
            }
        }

        private void showLoading(boolean show)
        {
            if (getView() != null)
            {
                getView().findViewById(R.id.loading).setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }

    }
}
