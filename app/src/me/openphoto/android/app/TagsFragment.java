
package me.openphoto.android.app;

import me.openphoto.android.app.model.Tag;
import me.openphoto.android.app.ui.adapter.MultiSelectTagsAdapter;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GalleryOpenControl;
import me.openphoto.android.app.util.LoadingControl;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.app.Activity;
import com.facebook.android.R;

public class TagsFragment extends CommonFragment
{
    public static final String TAG = TagsFragment.class.getSimpleName();

    private LoadingControl loadingControl;
    private GalleryOpenControl galleryOpenControl;

    private TagsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_tags, container, false);

        init(v);

        return v;
    }

    public void init(View v) {
        mAdapter = new TagsAdapter();
        ListView list = (ListView) v.findViewById(R.id.list_tags);
        list.setAdapter(mAdapter);

        final EditText search = (EditText) v.findViewById(R.id.edit_search);
        search.setOnEditorActionListener(new OnEditorActionListener()
        {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event)
            {
                switch (event.getKeyCode())
                {
                    case KeyEvent.KEYCODE_ENTER:
                        CommonUtils.debug(TAG, "Key code enter");
                        if (KeyEvent.ACTION_DOWN == event.getAction())
                        {
                            CommonUtils.debug(TAG, "Opening gallery");
                            search.post(new Runnable() {

                                @Override
                                public void run() {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                                    galleryOpenControl.openGallery(search.getText()
                                            .toString().trim(), null);
                                }
                            });
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
        Button filterBtn = (Button) v.findViewById(R.id.filterBtn);
        filterBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                galleryOpenControl.openGallery(mAdapter.getSelectedTags(), null);
            }
        });
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);
        galleryOpenControl = ((GalleryOpenControl) activity);

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mAdapter.forceStopLoadingIfNecessary();
    }

    private class TagsAdapter extends MultiSelectTagsAdapter
    {
        public TagsAdapter()
        {
            super(loadingControl);
        }

        @Override
        public View getView(Tag tag, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                final LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item_tag,
                        null);
            }
            CheckBox checkBox = (CheckBox) convertView
                    .findViewById(R.id.tag_checkbox);
            initTagCheckbox(tag, checkBox);

            ((TextView) convertView.findViewById(R.id.text_count))
                    .setText(Integer.toString(tag
                            .getCount()));
            return convertView;
        }

    }

}
