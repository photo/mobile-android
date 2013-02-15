
package com.trovebox.android.app.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.util.FloatMath;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.trovebox.android.app.util.concurrent.AsyncTaskEx;

public abstract class EndlessAdapter<T> extends BaseAdapter {
    @Override
    public abstract long getItemId(int position);

    public abstract View getView(T item, View convertView, ViewGroup parent);

    /**
     * Called when the next page is loaded. In this method load the items.
     * 
     * @param page Page (first page is page 1)
     * @return a response containing the items and information if there is a
     *         next page
     */
    public abstract LoadResponse loadItems(int page);

    public LoadResponse loadOneMoreItem(int index)
    {
        return null;
    }

    private final AtomicBoolean mKeepOnAppending = new AtomicBoolean(true);

    private final ArrayList<T> mItems;

    private int mCurrentPage = 1;
    private final int mPageSize;

    private LoadNextTask loadNextTask;
    protected int itemsBeforeLoadNextPage = 1;

    public EndlessAdapter(int pageSize) {
        this(pageSize, null);
    }

    public EndlessAdapter(int pageSize, ArrayList<T> items) {
        mItems = items != null ? items : new ArrayList<T>();
        mPageSize = pageSize;
        int loadedPages = (int) FloatMath.ceil((float) mItems.size() / mPageSize);
        if (mItems.size() > 0 && loadedPages * mPageSize > mItems.size())
        {
            mKeepOnAppending.set(false);
        }
        mCurrentPage = 1 + loadedPages;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    public int itemIndex(Object item)
    {
        return mItems.indexOf(item);
    }
    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    public ArrayList<T> getItems() {
        return mItems;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (checkNeedToLoadNextPage(position))
        {
            loadNextPage();
        }
        return getView((T) getItem(position), convertView, parent);
    }

    /**
     * Check whether need to load next page for the specified position. If so
     * then load
     * 
     * @param position
     */
    public boolean checkNeedToLoadNextPage(int position) {
        return (position >= getCount() - itemsBeforeLoadNextPage);
    }

    public void loadFirstPage() {
        mItems.clear();
        mCurrentPage = 1;
        loadNextPage();
    }

    public void loadNextPage() {
        if (mKeepOnAppending.getAndSet(false)) {
            loadNextTask = new LoadNextTask();
            loadNextTask.execute();
        }
    }

    public void loadOneMoreItemForCurrentPageEnd()
    {
        if (mKeepOnAppending.getAndSet(false)) {
            loadNextTask = new LoadOneMoreItemTask();
            loadNextTask.execute();
        }
    }

    public void deleteItemAt(int index)
    {
        mItems.remove(index);
        notifyDataSetChanged();
    }

    public void deleteItemAtAndLoadOneMoreItem(int index)
    {
        deleteItemAt(index);
        loadOneMoreItemForCurrentPageEnd();
    }

    public void updateItemAt(int index, T object)
    {
        mItems.remove(index);
        mItems.add(index, object);
        notifyDataSetChanged();
    }
    public int getPageSize() {
        return mPageSize;
    }

    public void forceStopLoadingIfNecessary()
    {
        if (loadNextTask != null)
        {
            if (loadNextTask.cancel(true))
            {
                onStoppedLoading();
            }
        }
    }

    private class LoadNextTask extends AsyncTaskEx<Void, Void, List<T>> {

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
            loadNextTask = null;
            onStoppedLoading();
            if (result != null) {
                mItems.addAll(result);
            }
            notifyDataSetChanged();
        }

    }

    private class LoadOneMoreItemTask extends LoadNextTask
    {
        @Override
        protected List<T> doInBackground(Void... params) {
            LoadResponse response = loadOneMoreItem(mItems.size() + 1);
            mKeepOnAppending.set(response.hasNext);
            return response.items;
        }
    }

    protected class LoadResponse {
        public List<T> items;
        boolean hasNext;

        public LoadResponse(List<T> items, boolean hasNext) {
            this.items = items;
            this.hasNext = hasNext;
        }
    }

    protected abstract void onStartLoading();

    protected abstract void onStoppedLoading();

    public int getCurrentPage()
    {
        return mCurrentPage;
    }
}
