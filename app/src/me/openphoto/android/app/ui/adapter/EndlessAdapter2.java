package me.openphoto.android.app.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.commonsware.cwac.endless.EndlessAdapter;

public abstract class EndlessAdapter2 extends EndlessAdapter {

	private boolean mAddLoadViewInAdapter;
	private View mLoadView;
	
	public EndlessAdapter2(ListAdapter wrapped, View loadView, boolean addLoadViewInAdapter) {
		super(wrapped);
		mAddLoadViewInAdapter = addLoadViewInAdapter;
		mLoadView = loadView;
	}

	@Override
	public int getCount() {
		if (mAddLoadViewInAdapter) {
			super.getCount();
		}
		return getWrappedAdapter().getCount();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == getWrappedAdapter().getCount()) {
			return mLoadView;
		} else {
			return super.getView(position, convertView, parent);
		}
	}

}
