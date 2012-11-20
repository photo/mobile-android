package com.aviary.android.feather.effects;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import com.aviary.android.feather.effects.AbstractEffectPanel.ContentPanel;
import com.aviary.android.feather.library.services.EffectContext;

/**
 * The Class AbstractContentPanel.
 */
abstract class AbstractContentPanel extends AbstractOptionPanel implements ContentPanel {

	protected OnContentReadyListener mContentReadyListener;
	protected View mDrawingPanel;
	protected ImageViewTouch mImageView;

	/**
	 * Instantiates a new abstract content panel.
	 * 
	 * @param context
	 *           the context
	 */
	public AbstractContentPanel( EffectContext context ) {
		super( context );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.aviary.android.feather.effects.AbstractEffectPanel.ContentPanel#setOnReadyListener(com.aviary.android.feather.effects.
	 * AbstractEffectPanel.OnContentReadyListener)
	 */
	@Override
	public final void setOnReadyListener( OnContentReadyListener listener ) {
		mContentReadyListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel.ContentPanel#getContentView(android.view.LayoutInflater)
	 */
	@Override
	public final View getContentView( LayoutInflater inflater ) {
		mDrawingPanel = generateContentView( inflater );
		return mDrawingPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractEffectPanel.ContentPanel#getContentView()
	 */
	@Override
	public final View getContentView() {
		return mDrawingPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractOptionPanel#onDispose()
	 */
	@Override
	protected void onDispose() {
		mContentReadyListener = null;
		super.onDispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aviary.android.feather.effects.AbstractOptionPanel#setEnabled(boolean)
	 */
	@Override
	public void setEnabled( boolean value ) {
		super.setEnabled( value );
		getContentView().setEnabled( value );
	}

	/**
	 * Call this method when your tool is ready to display its overlay. After this call the main context will remove the main image
	 * and will replace it with the content of this panel
	 */
	protected void contentReady() {
		if ( mContentReadyListener != null && isActive() ) mContentReadyListener.onReady( this );
	}

	/**
	 * Generate content view.
	 * 
	 * @param inflater
	 *           the inflater
	 * @return the view
	 */
	protected abstract View generateContentView( LayoutInflater inflater );

	/**
	 * Return the current content image display matrix.
	 * 
	 * @return the content display matrix
	 */
	@Override
	public Matrix getContentDisplayMatrix() {
		return mImageView.getDisplayMatrix();
	}
}
