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

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @version
 *          02.10.2012
 *          <br>- added clearing tag information in the parent activity
 *          intent when loading images for tag first time.
 */
public class GalleryFragment extends SherlockFragment implements Refreshable,
		OnItemClickListener
{
	public static final String TAG = GalleryFragment.class.getSimpleName();

	public static String EXTRA_TAG = "EXTRA_TAG";

	private LoadingControl loadingControl;
	private GalleryAdapter mAdapter;
	private String mTags;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_gallery, container, false);
		mTags = null;
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
		if (mTags == null)
		{
			mTags = getActivity().getIntent() != null ? getActivity()
					.getIntent()
					.getStringExtra(EXTRA_TAG)
					: null;
			if (mTags != null)
			{
				mAdapter = new GalleryAdapter(mTags);
				getActivity().getIntent().removeExtra(EXTRA_TAG);
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

	private class GalleryAdapter extends PhotosEndlessAdapter
	{
		private final ImageStorage mStorage = new ImageStorage(
				getActivity());

		public GalleryAdapter()
		{
			super(getActivity());
		}

		public GalleryAdapter(String tagFilter)
		{
			super(getActivity(), tagFilter);
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
	}

}
