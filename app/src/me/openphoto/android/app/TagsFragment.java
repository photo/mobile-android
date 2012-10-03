package me.openphoto.android.app;

import java.util.HashMap;
import java.util.Map;

import me.openphoto.android.app.model.Tag;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.TagsResponse;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.util.GalleryOpenControl;
import me.openphoto.android.app.util.LoadingControl;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

import com.bugsense.trace.BugSenseHandler;

/**
 * @version
 *          03.10.2012
 *          <br>- added internet availability check to the
 *          loadItems method
 *          <br>- added onDestroyView handler to force close
 *          loading task
 *          <br>- changed parent class to CommonFragment
 *          <br>- changed galleryOpenControl.openGallery calls
 *          because of method changed its signature
 * 
 */
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
					Log.e(TAG, "Could not load next photos in list", e);
					Map<String, String> extraData = new HashMap<String, String>();
					extraData.put("message",
							"Could not load next photos in list");
					BugSenseHandler.log(TAG, extraData, e);
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
