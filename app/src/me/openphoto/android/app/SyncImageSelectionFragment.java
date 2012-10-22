package me.openphoto.android.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import me.openphoto.android.app.bitmapfun.util.ImageCache;
import me.openphoto.android.app.bitmapfun.util.ImageCache.ImageCacheParams;
import me.openphoto.android.app.bitmapfun.util.ImageFileSystemFetcher;
import me.openphoto.android.app.bitmapfun.util.ImageResizer;
import me.openphoto.android.app.bitmapfun.util.ImageWorker.ImageWorkerAdapter;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.WazaBe.HoloEverywhere.LayoutInflater;
import com.WazaBe.HoloEverywhere.app.Activity;

public class SyncImageSelectionFragment extends CommonFragment implements Refreshable,
		OnItemClickListener
{
	public static final String TAG = SyncImageSelectionFragment.class.getSimpleName();
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private LoadingControl loadingControl;
	private CustomImageAdapter mAdapter;
	private ImageResizer mImageWorker;
	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private int mImageThumbBorder;
	private GridView photosGrid;
	NextStepFlow nextStepFlow;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mImageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);
		mImageThumbBorder = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_border);

		ImageCacheParams cacheParams = new ImageCacheParams(IMAGE_CACHE_DIR);

		mImageWorker = new ImageFileSystemFetcher(getActivity(),
				mImageThumbSize);
		mImageWorker.setLoadingImage(R.drawable.empty_photo);
		mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
				cacheParams));
		mAdapter = new CustomImageAdapter(getActivity(), mImageWorker);
		new InitTask().execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.fragment_sync_select_photos,
				container, false);
		init(v);
		return v;
	}

	public void init(View v)
	{
		photosGrid = (GridView) v.findViewById(R.id.grid_photos);

		// This listener is used to get the final width of the GridView and then
		// calculate the
		// number of columns and the width of each column. The width of each
		// column is variable
		// as the GridView has stretchMode=columnWidth. The column width is used
		// to set the height
		// of each view so we get nice square thumbnails.
		photosGrid.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener()
				{
					@Override
					public void onGlobalLayout()
					{
						if (mAdapter.getNumColumns() == 0)
						{
							final int numColumns = (int) Math.floor(
									photosGrid.getWidth()
											/ (mImageThumbSize
													+ mImageThumbSpacing + mImageThumbBorder));
							if (numColumns > 0)
							{
								final int columnWidth =
										(photosGrid.getWidth() / numColumns)
												- mImageThumbSpacing;
								mAdapter.setNumColumns(numColumns);
								mAdapter.setItemHeight(columnWidth, columnWidth
										- 2 * mImageThumbBorder);
								if (BuildConfig.DEBUG)
								{
									Log.d(TAG,
											"onCreateView - numColumns set to "
													+ numColumns);
								}
							}
						}
					}
				});
		Button nextStepBtn = (Button) v.findViewById(R.id.nextBtn);
		nextStepBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (nextStepFlow != null)
				{
					nextStepFlow.activateNextStep();
				}
			}
		});
		refresh(v);
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
		// mAdapter = new GalleryAdapter();
		if (mImageWorker.getAdapter() != null)
		{
			photosGrid.setAdapter(mAdapter);
		}
		photosGrid.setOnItemClickListener(this);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		mImageWorker.setExitTasksEarly(false);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		mImageWorker.setExitTasksEarly(true);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id)
	{
		mAdapter.selectedIds.add(id);
		// Intent intent = new Intent(getActivity(),
		// PhotoDetailsActivity.class);
		// intent.putParcelableArrayListExtra(
		// PhotoDetailsActivity.EXTRA_ADAPTER_PHOTOS,
		// mAdapter.getItems());
		// intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_POSITION,
		// position);
		// intent.putExtra(PhotoDetailsActivity.EXTRA_ADAPTER_TAGS, mTags);
		// startActivity(intent);
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
	}

	private class InitTask extends
			AsyncTask<Void, Void, Boolean>
	{
		CustomImageWorkerAdapter adapter;
		@Override
		protected Boolean doInBackground(Void... params)
		{
			try
			{
				adapter = new CustomImageWorkerAdapter();
				return true;
			} catch (Exception e)
			{
				GuiUtils.error(TAG,
						null,
						e);
			}
			return false;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			loadingControl.startLoading();
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			loadingControl.stopLoading();
			mImageWorker.setAdapter(adapter);
			if (photosGrid != null)
			{
				mAdapter.selectedIds.clear();
				photosGrid.setAdapter(mAdapter);
			}
		}

	}

	private class CustomImageAdapter extends ImageAdapter
	{
		Set<Long> selectedIds = new TreeSet<Long>();
		public CustomImageAdapter(Context context, ImageResizer imageWorker)
		{
			super(context, imageWorker);
		}

		@Override
		public View getViewAdditional(int position, View convertView,
				ViewGroup container)
		{
			// Now handle the main ImageView thumbnails
			View view;
			if (convertView == null)
			{ // if it's not recycled, instantiate and initialize
				final LayoutInflater layoutInflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = layoutInflater.inflate(
						R.layout.item_sync_image, null);
				view.setLayoutParams(mImageViewLayoutParams);
			} else
			{ // Otherwise re-use the converted view
				view = convertView;
			}

			// Check the height matches our calculated column width
			if (view.getLayoutParams().height != mItemHeight)
			{
				view.setLayoutParams(mImageViewLayoutParams);
			}
			final View selectedOverlay = view
					.findViewById(R.id.selection_overlay);
			final long id = getItemId(position);
			selectedOverlay.setVisibility(isSelected(id) ?
					View.VISIBLE : View.INVISIBLE);
			View imageContainer = view.findViewById(R.id.imageContainer);
			imageContainer.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					System.out.println("Clicked");
					boolean selected = isSelected(id);
					if (selected)
					{
						removeFromSelected(id);
					} else
					{
						addToSelected(id);
					}
					selectedOverlay.setVisibility(isSelected(id) ?
							View.VISIBLE : View.INVISIBLE);
				}

			});
			ImageView imageView = (ImageView) view.findViewById(R.id.image);
			// Finally load the image asynchronously into the ImageView, this
			// also takes care of
			// setting a placeholder image while the background thread runs
			mImageWorker.loadImage(position - mNumColumns, imageView);
			return view;
		}

		public boolean isSelected(final long id)
		{
			return selectedIds.contains(id);
		}

		public void addToSelected(final long id)
		{
			selectedIds.add(id);
		}

		public void removeFromSelected(final long id)
		{
			selectedIds.remove(id);
		}
	}

	/**
	 * The main adapter that backs the GridView. This is fairly standard except
	 * the number of
	 * columns in the GridView is used to create a fake top row of empty views
	 * as we use a
	 * transparent ActionBar and don't want the real top row of images to start
	 * off covered by it.
	 */
	private static class ImageAdapter extends BaseAdapter
	{

		protected final Context mContext;
		protected int mItemHeight = 0;
		protected int mNumColumns = 0;
		protected int mActionBarHeight = 0;
		protected GridView.LayoutParams mImageViewLayoutParams;
		private ImageResizer mImageWorker;

		public ImageAdapter(Context context, ImageResizer imageWorker)
		{
			super();
			mContext = context;
			this.mImageWorker = imageWorker;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}

		@Override
		public int getCount()
		{
			// Size of adapter + number of columns for top empty row
			return mImageWorker.getAdapter().getSize() + mNumColumns;
		}

		@Override
		public Object getItem(int position)
		{
			return position < mNumColumns ?
					null : mImageWorker.getAdapter().getItem(
							position - mNumColumns);
		}

		@Override
		public long getItemId(int position)
		{
			return position < mNumColumns ? 0 : position - mNumColumns;
		}

		@Override
		public int getViewTypeCount()
		{
			// Two types of views, the normal ImageView and the top row of empty
			// views
			return 2;
		}

		@Override
		public int getItemViewType(int position)
		{
			return (position < mNumColumns) ? 1 : 0;
		}

		@Override
		public boolean hasStableIds()
		{
			return true;
		}

		@Override
		public final View getView(int position, View convertView,
				ViewGroup container)
		{
			// First check if this is the top row
			if (position < mNumColumns)
			{
				if (convertView == null)
				{
					convertView = new View(mContext);
				}
				// Calculate ActionBar height
				if (mActionBarHeight < 0)
				{
					TypedValue tv = new TypedValue();
					if (mContext.getTheme().resolveAttribute(
							android.R.attr.actionBarSize, tv, true))
					{
						mActionBarHeight = TypedValue
								.complexToDimensionPixelSize(
										tv.data, mContext.getResources()
												.getDisplayMetrics());
					} else
					{
						// No ActionBar style (pre-Honeycomb or ActionBar not in
						// theme)
						mActionBarHeight = 0;
					}
				}
				// Set empty view with height of ActionBar
				convertView.setLayoutParams(new AbsListView.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));
				return convertView;
			}

			return getViewAdditional(position, convertView, container);
		}

		public View getViewAdditional(int position, View convertView,
				ViewGroup container)
		{
			// Now handle the main ImageView thumbnails
			ImageView imageView;
			if (convertView == null)
			{ // if it's not recycled, instantiate and initialize
				imageView = new ImageView(mContext);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setLayoutParams(mImageViewLayoutParams);
			} else
			{ // Otherwise re-use the converted view
				imageView = (ImageView) convertView;
			}

			// Check the height matches our calculated column width
			if (imageView.getLayoutParams().height != mItemHeight)
			{
				imageView.setLayoutParams(mImageViewLayoutParams);
			}

			// Finally load the image asynchronously into the ImageView, this
			// also takes care of
			// setting a placeholder image while the background thread runs
			mImageWorker.loadImage(position - mNumColumns, imageView);
			return imageView;
		}

		/**
		 * Sets the item height. Useful for when we know the column width so the
		 * height can be set
		 * to match.
		 * 
		 * @param height
		 */
		public void setItemHeight(int height, int imageHeight)
		{
			if (height == mItemHeight)
			{
				return;
			}
			mItemHeight = height;
			mImageViewLayoutParams =
					new GridView.LayoutParams(LayoutParams.MATCH_PARENT,
							mItemHeight);
			mImageWorker.setImageSize(imageHeight);
			notifyDataSetChanged();
		}

		public void setNumColumns(int numColumns)
		{
			mNumColumns = numColumns;
		}

		public int getNumColumns()
		{
			return mNumColumns;
		}
	}

	private class CustomImageWorkerAdapter extends
			ImageWorkerAdapter
	{
		List<Long> ids = new ArrayList<Long>();

		public CustomImageWorkerAdapter()
		{
			String[] projection =
			{
					MediaStore.Images.Media._ID
			};
			Cursor cursor = getActivity().getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					projection, // Which columns to return
					null, // Return all rows
					null,
					MediaStore.Images.Media.BUCKET_ID);
			try
			{
				while (cursor.moveToNext())
				{
					ids.add(cursor.getLong(0));
				}
			} finally
			{
				cursor.close();
			}
		}

		@Override
		public int getSize()
		{
			return ids.size();
		}

		@Override
		public Object getItem(int num)
		{
			// return Uri.withAppendedPath(
			// MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ""
			// + ids.get(num));
			String[] projection =
			{
					MediaStore.Images.Media.DATA
			};
			Cursor cursor = getActivity().getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					projection, // Which columns to return
					MediaStore.Images.Media._ID + " = "
							+ Long.toString(ids.get(num)),
					null,
					MediaStore.Images.Media.BUCKET_ID);
			try
			{
				if (cursor != null && cursor.moveToFirst())
				{
					return cursor.getString(0);
				}
			} finally
			{
				cursor.close();
			}
			return null;
		}
	}

	private class GalleryAdapter extends QuickAdapter
	{

		public GalleryAdapter()
		{
			super(getActivity(), new GalleryDataSource());
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent)
		{

			final LayoutInflater layoutInflater = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return layoutInflater.inflate(
					R.layout.item_sync_image, null);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			try
			{
				ImageView image = (ImageView) view.findViewById(R.id.image);
				String path = cursor.getString(0);
				System.out.println("Path: " + path);
				// int imageID = cursor.getInt(0);
				// image.setImageURI(Uri.withAppendedPath(
				// MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ""
				// + imageID));
			} catch (Exception ex)
			{
				GuiUtils.error(TAG, null, ex);
			}
		}

	}

	class GalleryDataSource implements QuickAdapter.DataSource
	{

		@Override
		public Cursor getRowIds()
		{
			String[] projection =
			{
					MediaStore.Images.Media._ID
			};
			return getActivity().getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					projection, // Which columns to return
					null, // Return all rows
					null,
					MediaStore.Images.Media.BUCKET_ID);
		}

		@Override
		public Cursor getRowById(long rowId)
		{
			String[] projection =
			{
					MediaStore.Images.Media.DATA
			};
			return getActivity().getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					projection, // Which columns to return
					MediaStore.Images.Media._ID + " = " + Long.toString(rowId), // Return
																				// all
																				// rows
					null,
					MediaStore.Images.Media.BUCKET_ID);
		}

	}

	public static abstract class QuickAdapter extends BaseAdapter
	{

		private final DataSource mDataSource;
		private int mSize = 0;
		private Cursor mRowIds = null;
		private final Context mContext;

		public QuickAdapter(Context context, DataSource dataSource)
		{
			mDataSource = dataSource;
			mContext = context;
			doQuery();
		}

		private void doQuery()
		{
			if (mRowIds != null)
			{
				mRowIds.close();
			}
			mRowIds = mDataSource.getRowIds();
			mSize = mRowIds.getCount();
		}

		@Override
		public int getCount()
		{
			return mSize;
		}

		@Override
		public Object getItem(int position)
		{
			if (mRowIds.moveToPosition(position))
			{
				long rowId = mRowIds.getLong(0);
				Cursor c = mDataSource.getRowById(rowId);
				return c;
			} else
			{
				return null;
			}
		}

		@Override
		public long getItemId(int position)
		{
			if (mRowIds.moveToPosition(position))
			{
				long rowId = mRowIds.getLong(0);
				return rowId;
			} else
			{
				return 0;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			mRowIds.moveToPosition(position);
			long rowId = mRowIds.getLong(0);
			Cursor cursor = mDataSource.getRowById(rowId);
			cursor.moveToFirst();
			View v;
			if (convertView == null)
			{
				v = newView(mContext, cursor, parent);
			} else
			{
				v = convertView;
			}
			bindView(v, mContext, cursor);
			cursor.close();
			return v;
		}

		public abstract View newView(Context context, Cursor cursor,
				ViewGroup parent);

		public abstract void bindView(View view, Context context, Cursor cursor);

		public interface DataSource
		{
			Cursor getRowIds();

			Cursor getRowById(long rowId);
		}

	}

	public NextStepFlow getNextStepFlow()
	{
		return nextStepFlow;
	}

	public void setNextStepFlow(NextStepFlow nextStepFlow)
	{
		this.nextStepFlow = nextStepFlow;
	}

	static interface NextStepFlow
	{
		void activateNextStep();
	}
}
