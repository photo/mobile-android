package me.openphoto.android.app;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.ui.adapter.PhotosEndlessAdapter;
import me.openphoto.android.app.ui.lib.ImageStorage;
import me.openphoto.android.app.util.LoadingControl;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * @version
 *          03.10.2012
 *          <br>- added internet availability check to the
 *          loadItems method
 *          <br>- added onDestroyView handler to force close
 *          loading task
 *          <br>- changed parent class to CommonFragment
 *          <br>- added initial support for album photos filter
 *          <p>
 *          02.10.2012
 *          <br>- added clearing tag information in the parent activity
 *          intent when loading images for tag first time.
 */
public class GalleryFragment extends CommonFragment implements Refreshable,
		OnItemClickListener
{
	public static final String TAG = GalleryFragment.class.getSimpleName();

	public static String EXTRA_TAG = "EXTRA_TAG";
	public static String EXTRA_ALBUM = "EXTRA_ALBUM";

	private LoadingControl loadingControl;
	private GalleryAdapter mAdapter;
	private String mTags;
	private String mAlbum;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_gallery, container, false);
		mTags = null;
		mAlbum = null;
		refresh(v);
		return v;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		loadingControl = ((LoadingControl) activity);

	}

	@Override
	public void refresh()
	{
		refresh(getView());
	}

	void refresh(View v)
	{
		if (mTags == null && mAlbum == null)
		{
			Intent intent = getActivity().getIntent();
			mTags = intent != null ? intent
					.getStringExtra(EXTRA_TAG)
					: null;
			mAlbum = intent != null ? intent.getStringExtra(EXTRA_ALBUM) : null;
			if (mTags != null || mAlbum != null)
			{
				mAdapter = new GalleryAdapter(mTags, mAlbum);
				getActivity().getIntent().removeExtra(EXTRA_TAG);
				getActivity().getIntent().removeExtra(EXTRA_ALBUM);
			} else
			{
				mAdapter = new GalleryAdapter();
			}
		}

		GridView photosGrid = (GridView) v.findViewById(R.id.grid_photos);
		photosGrid.setAdapter(mAdapter);
		photosGrid.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3)
	{
		Intent intent = new Intent(getActivity(), PhotoDetailsActivity.class);
		intent.putParcelableArrayListExtra(
				PhotoDetailsActivity.EXTRA_ADAPTER_PHOTOS,
				mAdapter.getItems());
		intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_POSITION, position);
		intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_TAGS, mTags);
		startActivity(intent);
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		mAdapter.forceStopLoadingIfNecessary();
	}
	private class GalleryAdapter extends PhotosEndlessAdapter
	{
		private final ImageStorage mStorage = new ImageStorage(
				getActivity());

		public GalleryAdapter()
		{
			super(getActivity());
		}

		public GalleryAdapter(String tagFilter, String albumFilter)
		{
			super(getActivity(), tagFilter, albumFilter);
		}

		@Override
		public View getView(Photo photo, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				final LayoutInflater layoutInflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(
						R.layout.item_gallery_image, null);
			}
			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			image.setImageBitmap(null); // TODO maybe a loading image
			mStorage.displayImageFor(image, photo.getUrl(SIZE_SMALL));
			return convertView;
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

		@Override
		public LoadResponse loadItems(
				int page)
		{
			if (checkOnline())
			{
				return super.loadItems(page);
			} else
			{
				return new LoadResponse(null, false);
			}
		}
	}

}
