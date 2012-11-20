package com.aviary.android.feather.widget.wp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.aviary.android.feather.R;

// TODO: Auto-generated Javadoc
/**
 * The Class WorkspaceIndicator.
 */
public class WorkspaceIndicator extends LinearLayout {

	/** The m res id. */
	int mResId;

	/** The m selected. */
	int mSelected;

	int mResWidth = -1;
	int mResHeight = -1;

	/**
	 * Instantiates a new workspace indicator.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public WorkspaceIndicator( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init( context, attrs, 0 );
	}

	/**
	 * Inits the.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 * @param defStyle
	 *           the def style
	 */
	private void init( Context context, AttributeSet attrs, int defStyle ) {
		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.WorkspaceIndicator, defStyle, 0 );
		setOrientation( LinearLayout.HORIZONTAL );
		mResId = a.getResourceId( R.styleable.WorkspaceIndicator_indicatorId, 0 );

		if ( mResId > 0 ) {
			Drawable d = getContext().getResources().getDrawable( mResId );
			mResWidth = d.getIntrinsicWidth();
			mResHeight = d.getIntrinsicHeight();
		}

		a.recycle();
	}
	
	@Override
	protected void onLayout( boolean changed, int l, int t, int r, int b ) {
		super.onLayout( changed, l, t, r, b );
	}

	/**
	 * Reset view.
	 * 
	 * @param count
	 *           the count
	 */
	void resetView( int count ) {
		removeAllViews();

		if ( mResId != 0 && count > 0 ) {

			int h = getHeight();

			if ( mResWidth > 0 ) {
				float ratio = (float) mResHeight / h;
				if ( mResHeight > h ) {
					mResHeight = h;
					mResWidth = (int) ( mResWidth / ratio );
				}
			} else {
				mResWidth = LinearLayout.LayoutParams.WRAP_CONTENT;
				mResHeight = LinearLayout.LayoutParams.MATCH_PARENT;
			}

			for ( int i = 0; i < count; i++ ) {
				ImageView v = new ImageView( getContext() );
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( mResWidth, mResHeight );
				v.setImageResource( mResId );
				v.setSelected( false );
				v.setPadding( 1, 0, 1, 0 );
				v.setLayoutParams( params );
				addView( v );
			}
		}
	}

	/**
	 * Sets the level.
	 * 
	 * @param mCurrentScreen
	 *           the m current screen
	 * @param mItemCount
	 *           the m item count
	 */
	public void setLevel( int mCurrentScreen, int mItemCount ) {

		if ( getChildCount() != mItemCount ) {
			resetView( mItemCount );
			mSelected = 0;
		}

		if ( mCurrentScreen >= 0 && mCurrentScreen < getChildCount() ) {
			getChildAt( mSelected ).setSelected( false );
			getChildAt( mCurrentScreen ).setSelected( true );
			mSelected = mCurrentScreen;
		}
	}

}
