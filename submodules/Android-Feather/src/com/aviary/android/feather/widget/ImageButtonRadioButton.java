package com.aviary.android.feather.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Checkable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aviary.android.feather.R;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageButtonRadioButton.
 */
public class ImageButtonRadioButton extends LinearLayout implements Checkable, OnClickListener {

	/** The m on checked change listener. */
	private OnCheckedChangeListener mOnCheckedChangeListener;

	/** The m on checked change widget listener. */
	private OnCheckedChangeListener mOnCheckedChangeWidgetListener;

	/** The m checked. */
	private boolean mChecked;

	/** The m broadcasting. */
	private boolean mBroadcasting;

	/** The m image view. */
	private ImageButton mImageView;

	/** The m text view. */
	private TextView mTextView;

	/** The m button label. */
	private int mButtonLabel;

	/** The m button icon. */
	private int mButtonIcon;

	/** The m temp checked. */
	private boolean mTempChecked;

	/**
	 * Instantiates a new image button radio button.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public ImageButtonRadioButton( Context context, AttributeSet attrs ) {
		super( context, attrs );
		init( context, attrs );
	}

	/**
	 * Inits the.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	private void init( Context context, AttributeSet attrs ) {

		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.ImageRadioButton );
		mButtonIcon = a.getResourceId( R.styleable.ImageRadioButton_buttonIcon, -1 );
		mButtonLabel = a.getResourceId( R.styleable.ImageRadioButton_buttonText, -1 );
		mTempChecked = a.getBoolean( R.styleable.ImageRadioButton_checked, false );
		a.recycle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onFinishInflate()
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		LayoutInflater.from( getContext() ).inflate( R.layout.feather_image_radiobutton, this );

		mImageView = (ImageButton) findViewById( R.id.image );
		mTextView = (TextView) findViewById( R.id.text );

		if ( mButtonLabel > 0 ) mTextView.setText( mButtonLabel );

		// if( mButtonIcon > 0 )
		// mImageView.setImageResource( mButtonIcon );

		mImageView.setOnClickListener( this );

		setChecked( mTempChecked );
	}

	/**
	 * Sets the button.
	 * 
	 * @param value
	 *           the new button
	 */
	public void setButton( int value ) {
		mImageView.setBackgroundResource( value );
		if ( mButtonIcon > 0 ) mImageView.setImageResource( mButtonIcon );
	}

	/**
	 * Sets the button text.
	 * 
	 * @param text
	 *           the new button text
	 */
	public void setButtonText( String text ) {
		mTextView.setText( text );
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

	/**
	 * Sets the on checked change widget listener.
	 * 
	 * @param listener
	 *           the new on checked change widget listener
	 */
	void setOnCheckedChangeWidgetListener( OnCheckedChangeListener listener ) {
		mOnCheckedChangeWidgetListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Checkable#isChecked()
	 */
	@Override
	public boolean isChecked() {
		return mChecked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Checkable#setChecked(boolean)
	 */
	@Override
	public void setChecked( boolean checked ) {
		if ( mChecked != checked ) {
			mChecked = checked;
			refreshDrawableState();

			// Avoid infinite recursions if setChecked() is called from a listener
			if ( mBroadcasting ) {
				return;
			}

			mBroadcasting = true;
			if ( mOnCheckedChangeListener != null ) {
				mOnCheckedChangeListener.onCheckedChanged( this, mChecked );
			}
			if ( mOnCheckedChangeWidgetListener != null ) {
				mOnCheckedChangeWidgetListener.onCheckedChanged( this, mChecked );
			}

			mImageView.setSelected( mChecked );
			mTextView.setSelected( mChecked );

			mBroadcasting = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Checkable#toggle()
	 */
	@Override
	public void toggle() {
		setChecked( !mChecked );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#performClick()
	 */
	@Override
	public boolean performClick() {
		toggle();
		return super.performClick();
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
	public static interface OnCheckedChangeListener {

		/**
		 * On checked changed.
		 * 
		 * @param buttonView
		 *           the button view
		 * @param isChecked
		 *           the is checked
		 */
		void onCheckedChanged( ImageButtonRadioButton buttonView, boolean isChecked );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick( View arg0 ) {
		performClick();
	}

}
