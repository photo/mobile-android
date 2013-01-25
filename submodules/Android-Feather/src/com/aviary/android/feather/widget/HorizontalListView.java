package com.aviary.android.feather.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

public abstract class HorizontalListView extends AdapterView<ListAdapter> {
	
	public HorizontalListView( Context context ) {
		super( context );
	}

	public HorizontalListView( Context context, AttributeSet attrs ) {
		super( context, attrs );
	}

	public HorizontalListView( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
	}

	public abstract int getScreenPositionForView( View view );
}
