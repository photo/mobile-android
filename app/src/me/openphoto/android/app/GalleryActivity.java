/**
 * The photo gallery screen
 */
package me.openphoto.android.app;

import java.util.ArrayList;
import java.util.List;

import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.OpenPhotoApi;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.ReturnSize;
import me.openphoto.android.app.ui.adapter.EndlessAdapter2;
import me.openphoto.android.app.ui.lib.ImageStorage;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

/**
 * The photo gallery screen
 * 
 * @author pas
 */
public class GalleryActivity extends Activity {
	private View mLoadingView;

	/**
	 * Called when Gallery Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		GridView photosGrid = (GridView) findViewById(R.id.grid_photos);
		mLoadingView = findViewById(R.id.progress);
		photosGrid.setAdapter(new PhotosEndlessAdapter(mLoadingView,
				false));
	}

	private class PhotosEndlessAdapter extends EndlessAdapter2 {
		
		private IOpenPhotoApi mOpenPhotoApi;
		private int mPage = 1;
		private PhotosResponse mCached;
		
		public PhotosEndlessAdapter(View loadView,
				boolean addLoadViewInAdapter) {
			super(new PhotosAdapter(), loadView, addLoadViewInAdapter);
			mOpenPhotoApi = OpenPhotoApi.createInstance(Preferences.getServer(GalleryActivity.this));
		}

		@Override
		protected boolean cacheInBackground() throws Exception {
			mLoadingView.setVisibility(View.VISIBLE);
			mCached = mOpenPhotoApi.getPhotos(new ReturnSize(200, 200), new Paging(mPage ++, 30));
			return mCached.getCurrentPage() < mCached.getTotalPages();
		}

		@Override
		protected void appendCachedData() {
			((PhotosAdapter)getWrappedAdapter()).add(mCached.getPhotos());
			mLoadingView.setVisibility(View.GONE);
		}
		
	}
	
	private class PhotosAdapter extends BaseAdapter {

		private ArrayList<Photo> mPhotos = new ArrayList<Photo>();
		private ImageStorage mStorage = new ImageStorage();
		
		@Override
		public int getCount() {
			return mPhotos.size();
		}

		public void add(List<Photo> photos) {
			mPhotos.addAll(photos);
		}

		@Override
		public Object getItem(int position) {
			return mPhotos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return ((Photo)mPhotos.get(position)).getId().hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView image;
			if (convertView == null){
				image = new ImageView(GalleryActivity.this);
				image.setScaleType(ScaleType.FIT_CENTER);
				LayoutParams params = new GridView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				image.setLayoutParams(params);
			} else {
				image = (ImageView) convertView;
			}
			Photo photo = (Photo) getItem(position);
			mStorage.displayImageFor(image, photo.getUrl("200x200"),
					photo.getId() + "_200x200");
			return image;
		}
		
	}
}