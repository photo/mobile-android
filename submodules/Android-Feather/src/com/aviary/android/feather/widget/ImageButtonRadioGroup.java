package com.aviary.android.feather.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.log.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageButtonRadioGroup.
 */
public class ImageButtonRadioGroup extends LinearLayout {

	/** The m checked id. */
	private int mCheckedId = -1;

	/** The m last button. */
	private int mFirstButton, mCenterButton, mLastButton;

	/** The m child on checked change listener. */
	private ImageButtonRadioButton.OnCheckedChangeListener mChildOnCheckedChangeListener;

	/** The m protect from checked change. */
	private boolean mProtectFromCheckedChange = false;

	/** The m on checked change listener. */
	private OnCheckedChangeListener mOnCheckedChangeListener;

	/** The m pass through listener. */
	private PassThroughHierarchyChangeListener mPassThroughListener;

	/**
	 * Instantiates a new image button radio group.
	 * 
	 * @param context
	 *           the context
	 */
	public ImageButtonRadioGroup( Context context ) {
		super( context );
		setOrientation( HORIZONTAL );
		init();
	}

	/**
	 * Instantiates a new image button radio group.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public ImageButtonRadioGroup( Context context, AttributeSet attrs ) {
		super( context, attrs );

		TypedArray attributes = context.obtainStyledAttributes( attrs, R.styleable.ImageButtonRadioGroup, /*
																																			 * R.attr.
																																			 * imageButtonRadioButtonStyle
																																			 */0, 0 );

		int value = attributes.getResourceId( R.styleable.ImageButtonRadioGroup_checkedButton, View.NO_ID );
		if ( value != View.NO_ID ) {
			mCheckedId = value;
		}

		mFirstButton = attributes.getResourceId( R.styleable.ImageButtonRadioGroup_firstButton, View.NO_ID );
		mLastButton = attributes.getResourceId( R.styleable.ImageButtonRadioGroup_lastButton, View.NO_ID );
		mCenterButton = attributes.getResourceId( R.styleable.ImageButtonRadioGroup_centerButton, View.NO_ID );

		final int index = attributes.getInt( R.styleable.ImageButtonRadioGroup_checkedButton, HORIZONTAL );
		setOrientation( index );
		attributes.recycle();
		init();
	}

	/**
	 * Inits the.
	 */
	private void init() {
		mChildOnCheckedChangeListener = new CheckedStateTracker();
		mPassThroughListener = new PassThroughHierarchyChangeListener();
		super.setOnHierarchyChangeListener( mPassThroughListener );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.LinearLayout#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected synchronized void onLayout( boolean changed, int l, int t, int r, int b ) {
		super.onLayout( changed, l, t, r, b );

		Log.i( LoggerFactory.LOG_TAG, "onLayout: " + changed );

		if ( changed ) {
			for ( int i = 0; i < getChildCount(); i++ ) {
				View child = getChildAt( i );
				if ( child instanceof ImageButtonRadioButton ) {
					ImageButtonRadioButton button = ( (ImageButtonRadioButton) child );
					if ( i == 0 ) {
						button.setButton( mFirstButton );
					} else if ( i == getChildCount() - 1 ) {
						button.setButton( mLastButton );
					} else {
						button.setButton( mCenterButton );
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#setOnHierarchyChangeListener(android.view.ViewGroup.OnHierarchyChangeListener)
	 */
	@Override
	public void setOnHierarchyChangeListener( OnHierarchyChangeListener listener ) {
		// the user listener is delegated to our pass-through listener
		mPassThroughListener.mOnHierarchyChangeListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onFinishInflate()
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		if ( mCheckedId != -1 ) {
			mProtectFromCheckedChange = true;
			setCheckedStateForView( mCheckedId, true );
			mProtectFromCheckedChange = false;
			setCheckedId( mCheckedId, true );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#addView(android.view.View, int, android.view.ViewGroup.LayoutParams)
	 */
	@Override
	public void addView( View child, int index, ViewGroup.LayoutParams params ) {
		if ( child instanceof ImageButtonRadioButton ) {
			final ImageButtonRadioButton button = (ImageButtonRadioButton) child;
			if ( button.isChecked() ) {
				mProtectFromCheckedChange = true;
				if ( mCheckedId != -1 ) {
					setCheckedStateForView( mCheckedId, false );
				}
				mProtectFromCheckedChange = false;
				setCheckedId( button.getId(), true );
			}
		}

		super.addView( child, index, params );
	}

	/**
	 * Check.
	 * 
	 * @param id
	 *           the id
	 */
	public void check( int id ) {
		if ( id != -1 && ( id == mCheckedId ) ) {
			return;
		}

		if ( mCheckedId != -1 ) {
			setCheckedStateForView( mCheckedId, false );
		}

		if ( id != -1 ) {
			setCheckedStateForView( id, true );
		}
		setCheckedId( id, true );
	}

	/**
	 * Sets the checked id.
	 * 
	 * @param id
	 *           the id
	 * @param isChecked
	 *           the is checked
	 */
	private void setCheckedId( int id, boolean isChecked ) {
		if ( isChecked )
			mCheckedId = id;
		else
			mCheckedId = -1;

		if ( mOnCheckedChangeListener != null ) {
			mOnCheckedChangeListener.onCheckedChanged( this, mCheckedId, isChecked );
		}
	}

	/**
	 * Sets the checked state for view.
	 * 
	 * @param viewId
	 *           the view id
	 * @param checked
	 *           the checked
	 */
	private void setCheckedStateForView( int viewId, boolean checked ) {
		View checkedView = findViewById( viewId );
		if ( checkedView != null && checkedView instanceof ImageButtonRadioButton ) {
			( (ImageButtonRadioButton) checkedView ).setChecked( checked );
		}
	}

	/**
	 * Gets the checked radio button id.
	 * 
	 * @return the checked radio button id
	 */
	public int getCheckedRadioButtonId() {
		return mCheckedId;
	}

	/**
	 * Clear check.
	 */
	public void clearCheck() {
		check( -1 );
	}

	/**
	 * Sets the on checked change listener.
	 * 
	 * @param listener
	 *           the new on checked change listener
	 */
	public void setOnCheckedChangeListener( OnCheckedChangeListener listener ) {
		mOnCheckedChangeListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.LinearLayout#generateLayoutParams(android.util.AttributeSet)
	 */
	@Override
	public LayoutParams generateLayoutParams( AttributeSet attrs ) {
		return new ImageButtonRadioGroup.LayoutParams( getContext(), attrs );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.LinearLayout#checkLayoutParams(android.view.ViewGroup.LayoutParams)
	 */
	@Override
	protected boolean checkLayoutParams( ViewGroup.LayoutParams p ) {
		return p instanceof ImageButtonRadioGroup.LayoutParams;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.LinearLayout#generateDefaultLayoutParams()
	 */
	@Override
	protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
	}

	/**
	 * The Class LayoutParams.
	 */
	public static class LayoutParams extends LinearLayout.LayoutParams {

		/**
		 * Instantiates a new layout params.
		 * 
		 * @param c
		 *           the c
		 * @param attrs
		 *           the attrs
		 */
		public LayoutParams( Context c, AttributeSet attrs ) {
			super( c, attrs );
		}

		/**
		 * Instantiates a new layout params.
		 * 
		 * @param w
		 *           the w
		 * @param h
		 *           the h
		 */
		public LayoutParams( int w, int h ) {
			super( w, h );
		}

		/**
		 * Instantiates a new layout params.
		 * 
		 * @param w
		 *           the w
		 * @param h
		 *           the h
		 * @param initWeight
		 *           the init weight
		 */
		public LayoutParams( int w, int h, float initWeight ) {
			super( w, h, initWeight );
		}

		/**
		 * Instantiates a new layout params.
		 * 
		 * @param p
		 *           the p
		 */
		public LayoutParams( ViewGroup.LayoutParams p ) {
			super( p );
		}

		/**
		 * Instantiates a new layout params.
		 * 
		 * @param source
		 *           the source
		 */
		public LayoutParams( MarginLayoutParams source ) {
			super( source );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.ViewGroup.LayoutParams#setBaseAttributes(android.content.res.TypedArray, int, int)
		 */
		@Override
		protected void setBaseAttributes( TypedArray a, int widthAttr, int heightAttr ) {

			if ( a.hasValue( widthAttr ) ) {
				width = a.getLayoutDimension( widthAttr, "layout_width" );
			} else {
				width = MATCH_PARENT;
			}

			if ( a.hasValue( heightAttr ) ) {
				height = a.getLayoutDimension( heightAttr, "layout_height" );
			} else {
				height = MATCH_PARENT;
			}
		}
	}

	/**
	 * The listener interface for receiving onCheckedChange events. The class that is interested in processing a onCheckedChange
	 * event implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnCheckedChangeListener<code> method. When
	 * the onCheckedChange event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnCheckedChangeEvent
	 */
	public interface OnCheckedChangeListener {

		/**
		 * On checked changed.
		 * 
		 * @param group
		 *           the group
		 * @param checkedId
		 *           the checked id
		 * @param isChecked
		 *           the is checked
		 */
		public void onCheckedChanged( ImageButtonRadioGroup group, int checkedId, boolean isChecked );
	}

	/**
	 * The Class CheckedStateTracker.
	 */
	private class CheckedStateTracker implements com.aviary.android.feather.widget.ImageButtonRadioButton.OnCheckedChangeListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.aviary.android.feather.widget.ImageButtonRadioButton.OnCheckedChangeListener#onCheckedChanged(com.aviary.android.feather
		 * .widget.ImageButtonRadioButton, boolean)
		 */
		@Override
		public void onCheckedChanged( ImageButtonRadioButton buttonView, boolean isChecked ) {
			if ( mProtectFromCheckedChange ) {
				return;
			}

			mProtectFromCheckedChange = true;
			if ( mCheckedId != -1 ) {
				setCheckedStateForView( mCheckedId, false );
			}
			mProtectFromCheckedChange = false;

			int id = buttonView.getId();
			setCheckedId( id, isChecked );
		}
	}

	/**
	 * The listener interface for receiving passThroughHierarchyChange events. The class that is interested in processing a
	 * passThroughHierarchyChange event implements this interface, and the object created with that class is registered with a
	 * component using the component's <code>addPassThroughHierarchyChangeListener<code> method. When
	 * the passThroughHierarchyChange event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see PassThroughHierarchyChangeEvent
	 */
	private class PassThroughHierarchyChangeListener implements ViewGroup.OnHierarchyChangeListener {

		/** The m on hierarchy change listener. */
		private ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.ViewGroup.OnHierarchyChangeListener#onChildViewAdded(android.view.View, android.view.View)
		 */
		@Override
		public void onChildViewAdded( View parent, View child ) {
			if ( parent == ImageButtonRadioGroup.this && child instanceof ImageButtonRadioButton ) {
				int id = child.getId();
				if ( id == View.NO_ID ) {
					id = child.hashCode();
					child.setId( id );
				}
				( (ImageButtonRadioButton) child ).setOnCheckedChangeWidgetListener( mChildOnCheckedChangeListener );
			}

			if ( mOnHierarchyChangeListener != null ) {
				mOnHierarchyChangeListener.onChildViewAdded( parent, child );
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.ViewGroup.OnHierarchyChangeListener#onChildViewRemoved(android.view.View, android.view.View)
		 */
		@Override
		public void onChildViewRemoved( View parent, View child ) {
			if ( parent == ImageButtonRadioGroup.this && child instanceof ImageButtonRadioButton ) {
				( (ImageButtonRadioButton) child ).setOnCheckedChangeWidgetListener( null );
			}

			if ( mOnHierarchyChangeListener != null ) {
				mOnHierarchyChangeListener.onChildViewRemoved( parent, child );
			}
		}
	}
}
