
package me.openphoto.android.app;

import me.openphoto.android.app.model.Tag;
import me.openphoto.android.app.ui.adapter.MultiSelectTagsAdapter;
import me.openphoto.android.app.util.LoadingControl;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.sherlock.SActivity;
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

        private class TagsAdapter extends MultiSelectTagsAdapter {

            public TagsAdapter() {
                super(UiFragment.this);
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
