
package me.openphoto.android.app.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class EndlessAdapter<T> extends BaseAdapter {
    @Override
    public abstract long getItemId(int position);

    public abstract View getView(T item, View convertView);

    /**
     * Called when the next page is loaded. In this method load the items.
     * 
     * @param page Page (first page is page 1)
     * @return a response containing the items and information if there is a
     *         next page
     */
    public abstract LoadResponse loadItems(int page);

    private final AtomicBoolean mKeepOnAppending = new AtomicBoolean(true);

    private final ArrayList<T> mItems;

    private int mCurrentPage = 1;
    private final int mPageSize;

    public EndlessAdapter(int pageSize) {
        this(pageSize, null);
    }

    public EndlessAdapter(int pageSize, ArrayList<T> items) {
        mItems = items != null ? items : new ArrayList<T>();
        mPageSize = pageSize;
        mCurrentPage = 1 + mItems.size() / mPageSize;
    }

    @Override
    public final int getCount() {
        return mItems.size();
    }

    @Override
    public final Object getItem(int position) {
        return mItems.get(position);
    }

    public ArrayList<T> getItems() {
        return mItems;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (position == getCount() - 1) {
            loadNextPage();
        }
        return getView((T) getItem(position), convertView);
    }

    public void loadFirstPage() {
        mItems.clear();
        mCurrentPage = 1;
        loadNextPage();
    }

    public void loadNextPage() {
        if (mKeepOnAppending.getAndSet(false)) {
            new LoadNextTask().execute();
        }
    }

    public int getPageSize() {
        return mPageSize;
    }

    private class LoadNextTask extends AsyncTask<Void, Void, List<T>> {

        @Override
        protected void onPreExecute() {
            onStartLoading();
        }

        @Override
        protected List<T> doInBackground(Void... params) {
            LoadResponse response = loadItems(mCurrentPage++);
            mKeepOnAppending.set(response.hasNext);
            return response.items;
        }

        @Override
        protected void onPostExecute(List<T> result) {
            onStoppedLoading();
            if (result != null) {
                mItems.addAll(result);
            }
            notifyDataSetChanged();
        }

    }

    protected class LoadResponse {
        List<T> items;
        boolean hasNext;

        public LoadResponse(List<T> items, boolean hasNext) {
            this.items = items;
            this.hasNext = hasNext;
        }
    }

    protected abstract void onStartLoading();

    protected abstract void onStoppedLoading();
}
