
package com.trovebox.android.common.fragment.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.PopupMenu;
import org.holoeverywhere.widget.PopupMenu.OnMenuItemClickListener;
import org.holoeverywhere.widget.ProgressBar;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.trovebox.android.common.CommonConfigurationUtils;
import com.trovebox.android.common.R;
import com.trovebox.android.common.activity.common.CommonActivity;
import com.trovebox.android.common.bitmapfun.util.ImageCache;
import com.trovebox.android.common.bitmapfun.util.ImageFileSystemFetcher;
import com.trovebox.android.common.bitmapfun.util.ImageResizer;
import com.trovebox.android.common.bitmapfun.util.ImageWorker.ImageWorkerAdapter;
import com.trovebox.android.common.fragment.common.CommonRefreshableFragmentWithImageWorker;
import com.trovebox.android.common.fragment.upload.UploadManagerFragment.PhotoUploadWrapper.UploadStatus;
import com.trovebox.android.common.provider.PhotoUpload;
import com.trovebox.android.common.provider.UploadsProviderAccessor;
import com.trovebox.android.common.service.UploaderServiceUtils;
import com.trovebox.android.common.service.UploaderServiceUtils.PhotoUploadHandler;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.common.util.GuiUtils;
import com.trovebox.android.common.util.ImageUtils;
import com.trovebox.android.common.util.LoadingControl;
import com.trovebox.android.common.util.RunnableWithParameter;
import com.trovebox.android.common.util.SimpleAsyncTaskEx;
import com.trovebox.android.common.util.TrackerUtils;

public class UploadManagerFragment extends CommonRefreshableFragmentWithImageWorker implements
        PhotoUploadHandler, OnItemClickListener {
    public static final String TAG = UploadManagerFragment.class.getSimpleName();

    public static final String IMAGE_WORKER_ADAPTER = CommonConfigurationUtils
            .getApplicationContext().getPackageName() + ".UploadManagerFragmentAdapter";

    private LoadingControl loadingControl;
    private ListView mUploadsList;
    private int mImageThumbSize;
    CustomImageWorkerAdapter mCustomImageWorkerAdapter;
    UploadsAdapter mAdapter;
    InitTask mInitTask;
    PhotoUploadWrapper mSelectedItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mImageThumbSize = getResources().getDimensionPixelSize(
                R.dimen.upload_manager_image_thumbnail_size);

        mCustomImageWorkerAdapter = CommonUtils.getParcelableFromBundleIfNotNull(
                IMAGE_WORKER_ADAPTER, savedInstanceState);
        if (mCustomImageWorkerAdapter != null) {
            mImageWorker.setAdapter(mCustomImageWorkerAdapter);
        }
        mAdapter = new UploadsAdapter(getActivity(), (ImageResizer) mImageWorker, loadingControl);

        CommonActivity activity = (CommonActivity) getActivity();
        activity.addRegisteredReceiver(UploaderServiceUtils
                .getAndRegisterOnPhotoUploadRemovedActionBroadcastReceiver(TAG, this, activity));
        activity.addRegisteredReceiver(UploaderServiceUtils
                .getAndRegisterOnPhotoUploadUpdatedActionBroadcastReceiver(TAG, this, activity));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_upload_manager, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_WORKER_ADAPTER, mCustomImageWorkerAdapter);
    }

    public void init(View v) {
        mUploadsList = (ListView) v.findViewById(R.id.listUploads);
        mUploadsList.setAdapter(null);
        mUploadsList.setOnItemClickListener(this);
        if (isDataLoaded()) {
            mUploadsList.setAdapter(mAdapter);
        } else {
            if (mInitTask == null) {
                refresh(v);
            }
        }
    }

    @Override
    protected void initImageWorker() {
        mImageWorker = new ImageFileSystemFetcher(getActivity(), loadingControl, mImageThumbSize);
        mImageWorker.setLoadingImage(R.drawable.empty_photo);

        mImageWorker.setImageCache(ImageCache.findOrCreateCache(getActivity(),
                ImageCache.LOCAL_THUMBS_CACHE_DIR, 1500, true, false));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);

    }

    @Override
    public void refresh() {
        refresh(getView());
    }

    void refresh(View v) {
        if (mInitTask == null) {
            mInitTask = new InitTask();
            mInitTask.execute();
        }
    }

    public boolean isDataLoaded() {
        return mCustomImageWorkerAdapter != null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isDataLoaded()) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mInitTask != null) {
            mInitTask.cancel(true);
        }
    }

    public void uploadsCleared() {
        if (isDataLoaded()) {
            mCustomImageWorkerAdapter.all.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected boolean isRefreshMenuVisible() {
        return !loadingControl.isLoading();
    }

    @Override
    public void photoUploadUpdated(PhotoUpload photoUpload, int progress) {
        if (isDataLoaded()) {
            mCustomImageWorkerAdapter.photUploadUpdated(photoUpload, progress);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void photoUploadRemoved(PhotoUpload photoUpload) {
        if (isDataLoaded()) {
            mCustomImageWorkerAdapter.photUploadRemoved(photoUpload);
            mAdapter.notifyDataSetChanged();
        }
    }

    OnMenuItemClickListener mOnMenuItemClickListener = new OnMenuItemClickListener() {
        UploadsProviderAccessor uploads = new UploadsProviderAccessor(
                CommonConfigurationUtils.getApplicationContext());

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.menu_cancel || item.getItemId() == R.id.menu_clear) {
                uploads.delete(mSelectedItem.photoUpload.getId());
                UploaderServiceUtils.sendPhotoUploadRemovedBroadcast(mSelectedItem.photoUpload);
            }
            return true;
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        TrackerUtils.trackButtonClickEvent("upload_manager_item", UploadManagerFragment.this);
        mSelectedItem = mAdapter.getItem(position);
        PopupMenu contextMenu = new PopupMenu(getActivity(), view);
        contextMenu.setOnMenuItemClickListener(mOnMenuItemClickListener);
        MenuInflater inflater = getSupportActivity().getSupportMenuInflater();
        Menu menu = contextMenu.getMenu();
        inflater.inflate(R.menu.upload_manager_item, menu);
        UploadStatus uploadStatus = mSelectedItem.getUploadStatus();
        if (uploadStatus == UploadStatus.DONE) {
            menu.findItem(R.id.menu_cancel).setVisible(false);
        } else {
            menu.findItem(R.id.menu_clear).setVisible(false);
        }
        contextMenu.show();
    }

    private class InitTask extends SimpleAsyncTaskEx {
        CustomImageWorkerAdapter mImageWorkerAdapter;

        InitTask() {
            super(loadingControl);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mImageWorkerAdapter = new CustomImageWorkerAdapter();
                return true;
            } catch (Exception e) {
                GuiUtils.error(TAG, e);
            }
            return false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mUploadsList.setAdapter(null);
            mCustomImageWorkerAdapter = null;
            mImageWorker.setAdapter(null);
        }

        @Override
        public void stopLoading() {
            super.stopLoading();
            mInitTask = null;
        }

        @Override
        protected void onSuccessPostExecute() {
            if (!isCancelled()) {
                mCustomImageWorkerAdapter = mImageWorkerAdapter;
                mImageWorker.setAdapter(mImageWorkerAdapter);
                if (mUploadsList != null) {
                    mUploadsList.setAdapter(mAdapter);
                }
            }
        }
    }

    public static class CustomImageWorkerAdapter extends ImageWorkerAdapter implements Parcelable {
        public List<PhotoUploadWrapper> all;

        public CustomImageWorkerAdapter() {
            loadProcessedValues();
            sort();
        }

        public CustomImageWorkerAdapter(List<PhotoUploadWrapper> all) {
            this.all = all;
        }

        public void loadProcessedValues() {
            UploadsProviderAccessor uploads = new UploadsProviderAccessor(
                    CommonConfigurationUtils.getApplicationContext());
            List<PhotoUpload> data = uploads.getAllUploads();
            all = new ArrayList<PhotoUploadWrapper>();
            for (PhotoUpload pu : data) {
                all.add(new PhotoUploadWrapper(pu));
            }
        }

        @Override
        public int getSize() {
            return all.size();
        }

        @Override
        public Object getItem(int num) {
            return all.get(num);
        }

        void sort() {
        }

        public void photUploadRemoved(PhotoUpload photoUpload) {
            for (int i = 0, size = all.size(); i < size; i++) {
                PhotoUploadWrapper puw = all.get(i);
                if (puw.photoUpload.getId() == photoUpload.getId()) {
                    all.remove(i);
                    break;
                }
            }
        }

        public void photUploadUpdated(PhotoUpload photoUpload, int progress) {
            PhotoUploadWrapper puw = null;
            for (int i = 0, size = all.size(); i < size; i++) {
                puw = all.get(i);
                if (puw.photoUpload.getId() == photoUpload.getId()) {
                    break;
                }
                puw = null;
            }
            if (puw != null) {
                puw.photoUpload = photoUpload;
                puw.progress = progress;
            } else {
                puw = new PhotoUploadWrapper(photoUpload);
                puw.progress = progress;
                all.add(puw);
            }
        }

        /*****************************
         * PARCELABLE IMPLEMENTATION *
         *****************************/
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeList(all);
        }

        public static final Parcelable.Creator<CustomImageWorkerAdapter> CREATOR = new Parcelable.Creator<CustomImageWorkerAdapter>() {
            @Override
            public CustomImageWorkerAdapter createFromParcel(Parcel in) {
                return new CustomImageWorkerAdapter(in);
            }

            @Override
            public CustomImageWorkerAdapter[] newArray(int size) {
                return new CustomImageWorkerAdapter[size];
            }
        };

        @SuppressWarnings("unchecked")
        private CustomImageWorkerAdapter(Parcel in) {
            all = in.readArrayList(getClass().getClassLoader());
        }
    }

    private static class UploadsAdapter extends BaseAdapter {

        private ImageResizer mImageWorker;
        LayoutInflater layoutInflater;
        LoadingControl mLoadingControl;

        public UploadsAdapter(Context context, ImageResizer imageWorker,
                LoadingControl loadingControl) {
            super();
            mLoadingControl = loadingControl;
            this.mImageWorker = imageWorker;
            this.layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mImageWorker.getAdapter().getSize();
        }

        @Override
        public PhotoUploadWrapper getItem(int position) {
            return (PhotoUploadWrapper) mImageWorker.getAdapter().getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public final View getView(int position, View convertView, ViewGroup container) {
            return getViewAdditional(position, convertView, container);
        }

        public View getViewAdditional(int position, View convertView, ViewGroup container) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_item_upload_manager, null);
                vh = new ViewHolder();
                vh.thumb = (ImageView) convertView.findViewById(R.id.thumb);
                vh.uploadTitleText = (TextView) convertView.findViewById(R.id.uploadTitleText);
                vh.uploadStatusText = (TextView) convertView.findViewById(R.id.uploadStatusText);
                vh.uploadStatusIcon = (ImageView) convertView.findViewById(R.id.uploadStatusIcon);
                vh.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            final PhotoUploadWrapper puw = getItem(position);
            vh.puw = puw;
            vh.uploadTitleText.setText(null);
            vh.progress.setVisibility(puw.progress == -1 ? View.GONE : View.VISIBLE);
            if (puw.progress != -1) {
                vh.progress.setProgress(puw.progress);
            }
            String uploadStatusDescription;
            UploadStatus uploadStatus = puw.getUploadStatus();
            switch (uploadStatus) {
                case DONE:
                    uploadStatusDescription = CommonUtils
                            .getStringResource(R.string.upload_status_uploaded);
                    break;
                case ERROR:
                    uploadStatusDescription = CommonUtils.getStringResource(
                            R.string.upload_status_error, puw.photoUpload.getError());
                    break;
                case PENDING:
                    uploadStatusDescription = CommonUtils
                            .getStringResource(R.string.upload_status_pending);
                    break;
                case UPLOADING:
                    uploadStatusDescription = CommonUtils.getStringResource(
                            R.string.upload_status_uploading, puw.progress);
                    break;
                default:
                    uploadStatusDescription = null;
                    break;

            }
            if (uploadStatus == UploadStatus.DONE) {
                vh.uploadStatusIcon.setImageResource(R.drawable.ic_checked);
            } else {
                vh.uploadStatusIcon.setImageResource(R.drawable.ic_synced);
            }
            vh.uploadStatusText.setText(uploadStatusDescription);

            validatePathExistAsyncAndRun(vh, new RunnableWithParameter<ViewHolder>() {

                @Override
                public void run(ViewHolder vh) {
                    if (vh.puw == puw) {
                        String uploadTilte;
                        if (puw.getPath() == null) {
                            uploadTilte = puw.photoUpload.getUri().toString();
                        } else {
                            File f = new File(puw.getPath());
                            uploadTilte = f.getName();
                        }
                        vh.uploadTitleText.setText(uploadTilte);
                        mImageWorker.loadImage(puw.getPath(), vh.thumb);
                    }
                }
            }, mLoadingControl);
            return convertView;
        }

        static class ViewHolder {
            ImageView thumb;
            TextView uploadTitleText;
            TextView uploadStatusText;
            ImageView uploadStatusIcon;
            ProgressBar progress;
            PhotoUploadWrapper puw;
        }

        public static void validatePathExistAsyncAndRun(ViewHolder viewHolder,
                RunnableWithParameter<ViewHolder> runnable, LoadingControl loadingControl) {
            if (viewHolder.puw.pathChecked) {
                CommonUtils.debug(TAG, "Path exists. Running action.");
                runnable.run(viewHolder);
            } else {
                CommonUtils.debug(TAG, "Path doesn't exist. Running path retrieval task.");
                new RetrievePathTask(viewHolder, runnable, loadingControl).execute();
            }
        }

        private static class RetrievePathTask extends SimpleAsyncTaskEx {
            private ViewHolder mViewHolder;
            private RunnableWithParameter<ViewHolder> mRunnable;
            private PhotoUploadWrapper mPhotoUploadWrapper;
            private String mPath;

            public RetrievePathTask(ViewHolder viewHolder,
                    RunnableWithParameter<ViewHolder> runnable, LoadingControl loadingControl) {
                super(loadingControl);
                this.mViewHolder = viewHolder;
                this.mPhotoUploadWrapper = viewHolder.puw;
                this.mRunnable = runnable;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    mPath = ImageUtils.getRealPathFromURI(
                            CommonConfigurationUtils.getApplicationContext(),
                            mPhotoUploadWrapper.photoUpload.getPhotoUri());
                    return true;
                } catch (Exception e) {
                    GuiUtils.error(TAG, e);
                }
                return false;
            }

            @Override
            protected void onSuccessPostExecute() {
                mPhotoUploadWrapper.setPath(mPath);
                mRunnable.run(mViewHolder);
            }

        }
    }

    static class PhotoUploadWrapper implements Parcelable {
        static enum UploadStatus {
            DONE, PENDING, UPLOADING, ERROR
        }

        PhotoUpload photoUpload;
        String path;
        boolean pathChecked;
        int progress = -1;

        public PhotoUploadWrapper(PhotoUpload photoUpload) {
            this.photoUpload = photoUpload;
        }

        public void setPath(String path) {
            this.path = path;
            pathChecked = true;
        }

        public String getPath() {
            return path;
        }

        public UploadStatus getUploadStatus() {
            UploadStatus uploadStatus;
            if (photoUpload.getUploaded() == 0) {
                if (TextUtils.isEmpty(photoUpload.getError())) {
                    uploadStatus = progress == -1 ? UploadStatus.PENDING : UploadStatus.UPLOADING;
                } else {
                    uploadStatus = UploadStatus.ERROR;
                }
            } else {
                uploadStatus = UploadStatus.DONE;
            }
            return uploadStatus;
        }

        /*****************************
         * PARCELABLE IMPLEMENTATION *
         *****************************/
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(photoUpload, flags);
            out.writeString(path);
            out.writeByte((byte) (pathChecked ? 1 : 0));
        }

        public static final Parcelable.Creator<PhotoUploadWrapper> CREATOR = new Parcelable.Creator<PhotoUploadWrapper>() {
            @Override
            public PhotoUploadWrapper createFromParcel(Parcel in) {
                return new PhotoUploadWrapper(in);
            }

            @Override
            public PhotoUploadWrapper[] newArray(int size) {
                return new PhotoUploadWrapper[size];
            }
        };

        private PhotoUploadWrapper(Parcel in) {
            photoUpload = in.readParcelable(getClass().getClassLoader());
            path = in.readString();
            pathChecked = in.readByte() == 1;
        }
    }
}
