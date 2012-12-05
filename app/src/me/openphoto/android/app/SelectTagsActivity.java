
package me.openphoto.android.app;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.openphoto.android.app.model.Tag;
import me.openphoto.android.app.model.utils.TagUtils;
import me.openphoto.android.app.ui.adapter.MultiSelectTagsAdapter;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.compare.ToStringComparator;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import me.openphoto.android.app.R;

public class SelectTagsActivity extends Activity {

    public static final String TAG = SelectTagsActivity.class.getSimpleName();
    public static final String SELECTED_TAGS = "SELECTED_TAGS";

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
            Set<String> tags = TagUtils.getTags(getActivity().getIntent().getStringExtra(
                    SELECTED_TAGS));
            mAdapter = new TagsAdapter(tags);
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

            data.putExtra(SELECTED_TAGS, selectedTags);
            getActivity().setResult(RESULT_OK, data);
            getActivity().finish();

        }

        private class TagsAdapter extends MultiSelectTagsAdapter {

            private final DataSetObserver mObserver = new DataSetObserver() {
                boolean ignoreChanges = false;
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (ignoreChanges)
                    {
                        return;
                    }
                    ToStringComparator comparator = new ToStringComparator();
                    List<Tag> items = getItems();
                    Collections.sort(items, comparator);
                    Set<String> notProcessedTags = new HashSet<String>(checkedTags);
                    for (Tag tag : items)
                    {
                        if (notProcessedTags.contains(tag.getTag()))
                        {
                            notProcessedTags.remove(tag.getTag());
                        }
                    }
                    if (!notProcessedTags.isEmpty())
                    {
                        for (String tagString : notProcessedTags)
                        {
                            if (!TextUtils.isEmpty(tagString))
                            {
                                Tag tag = Tag.fromTagName(tagString);
                                int ix = Collections.binarySearch(items, tag, comparator);
                                if (ix > 0)
                                {
                                    items.add(ix, tag);
                                } else
                                {
                                    items.add(-ix - 1, tag);
                                }
                            } else
                            {
                                checkedTags.remove(tagString);
                            }
                        }
                        ignoreChanges = true;
                        notifyDataSetChanged();
                        ignoreChanges = false;
                    }
                }
            };

            public TagsAdapter(Set<String> alreadySelectedTags) {
                super(UiFragment.this);
                if (alreadySelectedTags != null)
                {
                    checkedTags.addAll(alreadySelectedTags);
                }
                registerDataSetObserver(mObserver);
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
                initTagCheckbox(tag, checkBox);

                return convertView;
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
