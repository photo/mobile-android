package com.aviary.android.feather.widget;

import android.widget.BaseAdapter;
import com.aviary.android.feather.database.DataSetObservableExtended;
import com.aviary.android.feather.database.DataSetObserverExtended;

public abstract class BaseAdapterExtended extends BaseAdapter {

	private final DataSetObservableExtended mDataSetObservableExtended = new DataSetObservableExtended();

	public void registerDataSetObserverExtended( DataSetObserverExtended observer ) {
		mDataSetObservableExtended.registerObserver( observer );
	}

	public void unregisterDataSetObserverExtended( DataSetObserverExtended observer ) {
		mDataSetObservableExtended.unregisterObserver( observer );
	}

	/**
	 * Notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh
	 * itself.
	 */
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mDataSetObservableExtended.notifyChanged();
	}

	/**
	 * Notifies the attached observers that the underlying data is no longer valid or available. Once invoked this adapter is no
	 * longer valid and should not report further data set changes.
	 */
	public void notifyDataSetInvalidated() {
		super.notifyDataSetInvalidated();
		mDataSetObservableExtended.notifyInvalidated();
	}

	public void notifyDataSetAdded() {
		mDataSetObservableExtended.notifyAdded();
	}

	public void notifyDataSetRemoved() {
		mDataSetObservableExtended.notifyRemoved();
	}
}
