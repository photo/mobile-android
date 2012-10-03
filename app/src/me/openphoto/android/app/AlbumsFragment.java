package me.openphoto.android.app;

import java.util.HashMap;
import java.util.Map;

import me.openphoto.android.app.model.Album;
import me.openphoto.android.app.net.AlbumsResponse;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.ui.lib.ImageStorage;
import me.openphoto.android.app.util.GalleryOpenControl;
import me.openphoto.android.app.util.LoadingControl;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.bugsense.trace.BugSenseHandler;

/**
 * The fragment which displays albums list
 * 
 * @author Eugene Popovich
 * @version
 *          03.10.2012
 *          <br>- created
 */
public class AlbumsFragment extends SherlockFragment implements
		OnItemClickListener
{
	public static final String TAG = AlbumsFragment.class.getSimpleName();

	private LoadingControl loadingControl;
	private GalleryOpenControl galleryOpenControl;

	private AlbumsAdapter mAdapter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_albums, container, false);

		mAdapter = new AlbumsAdapter();
		ListView list = (ListView) v.findViewById(R.id.list_albums);
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(this);

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
		Album album = (Album) mAdapter.getItem(position);
		galleryOpenControl.openGallery(null, album.getId());
	}


	private class AlbumsAdapter extends EndlessAdapter<Album>
	{
		private final IOpenPhotoApi mOpenPhotoApi;
		private final ImageStorage mStorage = new ImageStorage(
				getActivity());

		public AlbumsAdapter()
		{
			super(Integer.MAX_VALUE);
			mOpenPhotoApi = Preferences.getApi(getActivity());
			loadFirstPage();
		}

		@Override
		public long getItemId(int position)
		{
			// return ((Album) getItem(position)).getAlbum().hashCode();
			return position;
		}

		@Override
		public View getView(Album album, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				final LayoutInflater layoutInflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.list_item_album,
						null);
			}
			((TextView) convertView.findViewById(R.id.text_name))
					.setText(album
							.getName());
			((TextView) convertView.findViewById(R.id.text_owner))
					.setText(album
							.getOwner());
			((TextView) convertView.findViewById(R.id.text_count))
					.setText(Integer.toString(album
							.getCount()));
			ImageView image = (ImageView) convertView.findViewById(R.id.cover);
			image.setImageBitmap(null); // TODO maybe a loading image
			if (album.getCover() != null)
			{
				mStorage.displayImageFor(image,
						album.getCover().getUrl("200x200"));
			}
			return convertView;
		}

		@Override
		public LoadResponse loadItems(int page)
		{
			try
			{
				AlbumsResponse response = mOpenPhotoApi.getAlbums();
				return new LoadResponse(response.getAlbums(), false);
			} catch (Exception e)
			{
				Log.e(TAG, "Could not load next albums in list", e);
				Map<String, String> extraData = new HashMap<String, String>();
				extraData.put("message", "Could not load next albums in list");
				BugSenseHandler.log(TAG, extraData, e);
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
