
package me.openphoto.android.app;

import me.openphoto.android.app.model.Tag;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.TagsResponse;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.util.GalleryOpenControl;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TagsFragment extends CommonFragment implements
        OnItemClickListener
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

        mAdapter = new TagsAdapter();
        ListView list = (ListView) v.findViewById(R.id.list_tags);
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(this);

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
                        if (KeyEvent.ACTION_DOWN == actionId)
                        {
                            galleryOpenControl.openGallery(search.getText()
                                    .toString().trim(), null);
                            return true;
                        }
                        break;
                }
                return false;
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);
        galleryOpenControl = ((GalleryOpenControl) activity);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,
            int position, long id)
    {
        Tag tag = (Tag) mAdapter.getItem(position);
        galleryOpenControl.openGallery(tag.getTag(), null);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mAdapter.forceStopLoadingIfNecessary();
    }

    private class TagsAdapter extends EndlessAdapter<Tag>
    {
        private final IOpenPhotoApi mOpenPhotoApi;

        public TagsAdapter()
        {
            super(Integer.MAX_VALUE);
            mOpenPhotoApi = Preferences.getApi(getActivity());
            loadFirstPage();
        }

        @Override
        public long getItemId(int position)
        {
            return ((Tag) getItem(position)).getTag().hashCode();
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
            ((TextView) convertView.findViewById(R.id.text_tag)).setText(tag
                    .getTag());
            ((TextView) convertView.findViewById(R.id.text_count))
                    .setText(Integer.toString(tag
                            .getCount()));
            return convertView;
        }

        @Override
        public LoadResponse loadItems(int page)
        {
            if (checkOnline())
            {
                try
                {
                    TagsResponse response = mOpenPhotoApi.getTags();
                    return new LoadResponse(response.getTags(), false);
                } catch (Exception e)
                {
					GuiUtils.error(TAG, "Could not load next tags in list", e);
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
