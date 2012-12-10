
package me.openphoto.android.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import me.openphoto.android.app.bitmapfun.util.ImageCache;
import me.openphoto.android.app.bitmapfun.util.ImageFileSystemFetcher;
import me.openphoto.android.app.bitmapfun.util.ImageResizer;
import me.openphoto.android.app.bitmapfun.util.ImageWorker.ImageWorkerAdapter;
import me.openphoto.android.app.common.CommonFrargmentWithImageWorker;
import me.openphoto.android.app.provider.UploadsProviderAccessor;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.TrackerUtils;
import me.openphoto.android.app.util.concurrent.AsyncTaskEx;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.Switch;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class SyncImageSelectionFragment extends CommonFrargmentWithImageWorker implements
        Refreshable
{
    public static final String TAG = SyncImageSelectionFragment.class.getSimpleName();
    public static final String SELECTED_IMAGES = "SyncImageSelectionFragmentSelectedImages";
    public static final String IMAGE_WORKER_ADAPTER = "SyncImageSelectionFragmentAdapter";

    private LoadingControl loadingControl;
    private CustomImageAdapter mAdapter;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private int mImageThumbBorder;
    private GridView photosGrid;
    NextStepFlow nextStepFlow;
    InitTask initTask = null;
    Switch stateSwitch;
    CustomImageWorkerAdapter customImageWorkerAdapter;
    SelectionController selectionController;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mImageThumbSize = getResources().getDimensionPixelSize(
                R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(
                R.dimen.image_thumbnail_spacing);
        mImageThumbBorder = getResources().getDimensionPixelSize(
                R.dimen.image_thumbnail_border);

        customImageWorkerAdapter = CommonUtils.getSerializableFromBundleIfNotNull(
                IMAGE_WORKER_ADAPTER, savedInstanceState);
        selectionController = CommonUtils.getSerializableFromBundleIfNotNull(SELECTED_IMAGES,
                savedInstanceState);
        if (selectionController == null)
        {
            selectionController = new SelectionController();
        }
        if (customImageWorkerAdapter != null)
        {
            mImageWorker.setAdapter(customImageWorkerAdapter);
        }
        mAdapter = new CustomImageAdapter(getActivity(), (ImageResizer) mImageWorker,
                selectionController);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.sync_image_selection, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_select_all: {
                TrackerUtils
                        .trackOptionsMenuClickEvent("menu_select_all", SyncImageSelectionFragment.this);
                selectAll();
                return true;
            }
            case R.id.menu_select_none: {
                TrackerUtils.trackOptionsMenuClickEvent("menu_select_none",
                        SyncImageSelectionFragment.this);
                selectNone();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_sync_select_photos,
                container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
        init(view);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mAdapter != null)
        {
            mAdapter.setNumColumns(0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SELECTED_IMAGES, selectionController);
        outState.putSerializable(IMAGE_WORKER_ADAPTER, customImageWorkerAdapter);
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
                                    CommonUtils.debug(TAG,
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
                TrackerUtils.trackButtonClickEvent("nextBtn", SyncImageSelectionFragment.this);
                if (isDataLoaded())
                {
                    if (selectionController.hasSelected())
                    {
                        if (nextStepFlow != null)
                        {
                            nextStepFlow.activateNextStep();
                        }
                    } else
                    {
                        GuiUtils.alert(R.string.sync_please_pick_at_least_one_photo);
                    }
                }
            }
        });
        stateSwitch = (Switch) v.findViewById(R.id.uploaded_state_switch);
        stateSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked)
            {
                switchUploadState(isChecked);
            }
        });
        if (isDataLoaded())
        {
            photosGrid.setAdapter(mAdapter);
        }
        if (photosGrid.getAdapter() == null && initTask == null)
        {
            refresh(v);
        }
    }

    @Override
    protected void initImageWorker()
    {
        mImageWorker = new CustomImageFileSystemFetcher(getActivity(),
                loadingControl,
                mImageThumbSize);
        mImageWorker.setLoadingImage(R.drawable.empty_photo);

        mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                ImageCache.LOCAL_THUMBS_CACHE_DIR, false));
    }

    protected void switchUploadState(boolean isChecked)
    {
        if (isDataLoaded())
        {
            customImageWorkerAdapter.setFiltered(!isChecked);
            mAdapter.notifyDataSetChanged();
        }
    }

    protected void selectAll()
    {
        if (isDataLoaded())
        {
            for (int i = 0, size = customImageWorkerAdapter.getSize(); i < size; i++)
            {
                ImageData imageData = (ImageData) customImageWorkerAdapter.getItem(i);
                if (imageData != null &&
                        !selectionController.isSelected(imageData.id)
                        && !customImageWorkerAdapter.isProcessedValue(imageData))
                {
                    selectionController.addToSelected(imageData.id);
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    protected void selectNone()
    {
        if (isDataLoaded())
        {
            selectionController.clearSelection();
            mAdapter.notifyDataSetChanged();
        }
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
        if (initTask == null)
        {
            initTask = new InitTask();
            initTask.execute();
        }
    }

    public boolean isDataLoaded()
    {
        return customImageWorkerAdapter != null;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (isDataLoaded())
        {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (initTask != null)
        {
            initTask.cancel(true);
        }

    }

    public void clear()
    {
        selectionController.clearSelection();
    }

    public List<String> getSelectedFileNames()
    {
        if (customImageWorkerAdapter == null)
        {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        for (int i = 0, size = customImageWorkerAdapter.getSize(); i < size; i++)
        {
            ImageData imageData = (ImageData) customImageWorkerAdapter.getItem(i);
            if (imageData != null && selectionController.isSelected(imageData.id))
            {
                result.add(imageData.data);
            }
        }
        return result;
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

    public void addProcessedValues(List<String> values)
    {
        if (isDataLoaded())
        {
            customImageWorkerAdapter.addProcessedValues(values);
            mAdapter.notifyDataSetChanged();
        }
    }

    static class ImageData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        long id;
        String data;

        public ImageData(long id, String data)
        {
            super();
            this.id = id;
            this.data = data;
        }

        @Override
        public String toString()
        {
            return data;
        }
    }

    private class InitTask extends
            AsyncTaskEx<Void, Void, Boolean>
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
            photosGrid.setAdapter(null);
            customImageWorkerAdapter = null;
            mImageWorker.setAdapter(null);
            selectionController.clearSelection();
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
            loadingControl.stopLoading();
            initTask = null;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            loadingControl.stopLoading();
            initTask = null;
            if (!isCancelled())
            {
                adapter.setFiltered(!stateSwitch.isChecked());
                customImageWorkerAdapter = adapter;
                mImageWorker.setAdapter(adapter);
                if (photosGrid != null)
                {
                    selectionController.clearSelection();
                    photosGrid.setAdapter(mAdapter);
                }
            }
        }

    }

    private static class SelectionController implements Serializable
    {
        private static final long serialVersionUID = 1L;
        Set<Long> selectedIds = new TreeSet<Long>();

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

        void clearSelection()
        {
            selectedIds.clear();
        }

        boolean hasSelected()
        {
            return !selectedIds.isEmpty();
        }
    }

    private class CustomImageAdapter extends ImageAdapter
    {
        SelectionController selectionController;

        public CustomImageAdapter(Context context, ImageResizer imageWorker,
                SelectionController selectionController)
        {
            super(context, imageWorker);
            this.selectionController = selectionController;
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
            ImageData value = (ImageData) getItem(position);
            final long id = value.id;

            selectedOverlay.setVisibility(selectionController.isSelected(id) ?
                    View.VISIBLE : View.INVISIBLE);
            boolean isProcessed = customImageWorkerAdapter
                    .isProcessedValue(value);
            final View uploadedOverlay = view
                    .findViewById(R.id.uploaded_overlay);
            uploadedOverlay.setVisibility(isProcessed ?
                    View.VISIBLE : View.INVISIBLE);
            View imageContainer = view.findViewById(R.id.imageContainer);
            if (isProcessed)
            {
                imageContainer.setOnClickListener(null);
            } else
            {
                imageContainer.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        TrackerUtils.trackButtonClickEvent("imageContainer",
                                SyncImageSelectionFragment.this);
                        boolean selected = selectionController.isSelected(id);
                        if (selected)
                        {
                            selectionController.removeFromSelected(id);
                        } else
                        {
                            selectionController.addToSelected(id);
                        }
                        selectedOverlay.setVisibility(selectionController.isSelected(id) ?
                                View.VISIBLE : View.INVISIBLE);
                    }

                });
            }
            ImageView imageView = (ImageView) view.findViewById(R.id.image);
            imageView.setTag(value);
            // Finally load the image asynchronously into the ImageView, this
            // also takes care of
            // setting a placeholder image while the background thread runs
            mImageWorker.loadImage(position - mNumColumns, imageView);
            return view;
        }

    }

    /**
     * The main adapter that backs the GridView. This is fairly standard except
     * the number of columns in the GridView is used to create a fake top row of
     * empty views as we use a transparent ActionBar and don't want the real top
     * row of images to start off covered by it.
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
            return position < mNumColumns ? -1 : position - mNumColumns;
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
         * height can be set to match.
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

    private class CustomImageFileSystemFetcher extends ImageFileSystemFetcher
    {
        public CustomImageFileSystemFetcher(Context context,
                LoadingControl loadingControl, int imageSize)
        {
            super(context, loadingControl, imageSize);
        }

        public CustomImageFileSystemFetcher(Context context,
                LoadingControl loadingControl, int imageWidth,
                int imageHeight)
        {
            super(context, loadingControl, imageWidth, imageHeight);
        }

        @Override
        protected Bitmap processBitmap(Object data)
        {
            ImageData imageData = (ImageData) data;
            return super.processBitmap(imageData.data);
        }
    }

    private static class CustomImageWorkerAdapter extends
            ImageWorkerAdapter implements Serializable
    {
        private static final long serialVersionUID = 1L;

        List<ImageData> all;
        Set<String> processedValues;

        List<Integer> filteredIndexes;

        boolean filtered = false;

        public CustomImageWorkerAdapter()
        {
            loadGallery();
            loadProcessedValues();
            sort();
        }

        public void loadGallery()
        {
            String[] projection =
            {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = OpenPhotoApplication.getContext().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, // Which columns to return
                    null, // Return all rows
                    null,
                    MediaStore.Images.Media.BUCKET_ID);
            if (cursor != null)
            {
                try
                {
                    int count = cursor.getCount();
                    all = new ArrayList<ImageData>(count);
                    while (cursor.moveToNext())
                    {
                        int ind = 0;
                        all.add(new ImageData(cursor.getLong(ind++), cursor
                                .getString(ind)));
                    }
                } finally
                {

                    cursor.close();
                }
            }else
            {
                all = new ArrayList<ImageData>();
            }
        }

        public void loadProcessedValues()
        {
            UploadsProviderAccessor uploads = new UploadsProviderAccessor(
                    OpenPhotoApplication.getContext());
            List<String> fileNames = uploads
                    .getUploadedOrPendingPhotosFileNames();
            processedValues = new TreeSet<String>(fileNames);
        }

        @Override
        public int getSize()
        {
            return filteredIndexes == null ? all.size() : filteredIndexes
                    .size();
        }

        @Override
        public Object getItem(int num)
        {
            return filteredIndexes == null ? all.get(num) : all
                    .get(filteredIndexes.get(num));
        }

        public void setFiltered(boolean filtered)
        {
            if (filtered)
            {
                filteredIndexes = new ArrayList<Integer>();

                for (int i = 0, size = all.size(); i < size; i++)
                {
                    ImageData value = all.get(i);
                    if (!isProcessedValue(value))
                    {
                        filteredIndexes.add(i);
                    }
                }
            } else
            {
                filteredIndexes = null;
            }
            this.filtered = filtered;
        }

        public boolean isProcessedValue(ImageData value)
        {
            return processedValues.contains(value.data);
        }

        public void addProcessedValues(List<String> values)
        {
            processedValues.addAll(values);
            sort();
            setFiltered(filtered);
        }

        public void clearProcessedValues()
        {
            processedValues.clear();
            sort();
            setFiltered(filtered);
        }

        void sort()
        {
            Collections.sort(all, new Comparator<ImageData>()
            {
                @Override
                public int compare(ImageData lhs, ImageData rhs)
                {
                    boolean leftProcessed = isProcessedValue(lhs);
                    boolean rightProcessed = isProcessedValue(rhs);
                    if (leftProcessed == rightProcessed)
                    {
                        return 0;
                    }
                    return leftProcessed ? -1 : 1;
                }
            });
        }
    }

    public void uploadsCleared()
    {
        if (isDataLoaded())
        {
            customImageWorkerAdapter.clearProcessedValues();
            mAdapter.notifyDataSetChanged();
        }
    }
}
