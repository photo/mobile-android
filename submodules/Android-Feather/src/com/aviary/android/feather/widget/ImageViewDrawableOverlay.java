package com.aviary.android.feather.widget;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import com.aviary.android.feather.library.services.DragControllerService.DragSource;
import com.aviary.android.feather.library.services.drag.DragView;
import com.aviary.android.feather.library.services.drag.DropTarget;
import com.aviary.android.feather.widget.DrawableHighlightView.Mode;

public class ImageViewDrawableOverlay extends ImageViewTouch implements DropTarget {

	/**
	 * The listener interface for receiving onLayout events. The class that is interested in processing a onLayout event implements
	 * this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnLayoutListener<code> method. When
	 * the onLayout event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnLayoutEvent
	 */
	public interface OnLayoutListener {

		/**
		 * On layout changed.
		 * 
		 * @param changed
		 *           the changed
		 * @param left
		 *           the left
		 * @param top
		 *           the top
		 * @param right
		 *           the right
		 * @param bottom
		 *           the bottom
		 */
		void onLayoutChanged( boolean changed, int left, int top, int right, int bottom );
	}

	/**
	 * The listener interface for receiving onDrawableEvent events. The class that is interested in processing a onDrawableEvent
	 * event implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnDrawableEventListener<code> method. When
	 * the onDrawableEvent event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnDrawableEventEvent
	 */
	public static interface OnDrawableEventListener {

		/**
		 * On focus change.
		 * 
		 * @param newFocus
		 *           the new focus
		 * @param oldFocus
		 *           the old focus
		 */
		void onFocusChange( DrawableHighlightView newFocus, DrawableHighlightView oldFocus );

		/**
		 * On down.
		 * 
		 * @param view
		 *           the view
		 */
		void onDown( DrawableHighlightView view );

		/**
		 * On move.
		 * 
		 * @param view
		 *           the view
		 */
		void onMove( DrawableHighlightView view );

		/**
		 * On click.
		 * 
		 * @param view
		 *           the view
		 */
		void onClick( DrawableHighlightView view );
	};

	/** The m motion edge. */
	private int mMotionEdge = DrawableHighlightView.GROW_NONE;

	/** The m overlay views. */
	private List<DrawableHighlightView> mOverlayViews = new ArrayList<DrawableHighlightView>();

	/** The m overlay view. */
	private DrawableHighlightView mOverlayView;

	/** The m layout listener. */
	private OnLayoutListener mLayoutListener;

	/** The m drawable listener. */
	private OnDrawableEventListener mDrawableListener;

	/** The m force single selection. */
	private boolean mForceSingleSelection = true;

	private DropTargetListener mDropTargetListener;

	private Paint mDropPaint;

	private Rect mTempRect = new Rect();

	private boolean mScaleWithContent = false;

	/**
	 * Instantiates a new image view drawable overlay.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public ImageViewDrawableOverlay( Context context, AttributeSet attrs ) {
		super( context, attrs );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch#init()
	 */
	@Override
	protected void init() {
		super.init();
		mTouchSlop = 20 * 20;
		mGestureDetector.setIsLongpressEnabled( false );
	}

	/**
	 * How overlay content will be scaled/moved when zomming/panning the base image
	 * 
	 * @param value
	 *           - if true then the content will be scale according to the image
	 */
	public void setScaleWithContent( boolean value ) {
		mScaleWithContent = value;
	}

	public boolean getScaleWithContent() {
		return mScaleWithContent;
	}

	/**
	 * If true, when the user tap outside the drawable overlay and there is only one active overlay selection is not changed.
	 * 
	 * @param value
	 *           the new force single selection
	 */
	public void setForceSingleSelection( boolean value ) {
		mForceSingleSelection = value;
	}

	public void setDropTargetListener( DropTargetListener listener ) {
		mDropTargetListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch#getGestureListener()
	 */
	@Override
	protected OnGestureListener getGestureListener() {
		return new CropGestureListener();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch#getScaleListener()
	 */
	@Override
	protected OnScaleGestureListener getScaleListener() {
		return new CropScaleListener();
	}

	/**
	 * Sets the on layout listener.
	 * 
	 * @param listener
	 *           the new on layout listener
	 */
	public void setOnLayoutListener( OnLayoutListener listener ) {
		mLayoutListener = listener;
	}

	/**
	 * Sets the on drawable event listener.
	 * 
	 * @param listener
	 *           the new on drawable event listener
	 */
	public void setOnDrawableEventListener( OnDrawableEventListener listener ) {
		mDrawableListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouchBase#setImageBitmap(android.graphics.Bitmap, boolean,
	 * android.graphics.Matrix)
	 */
	@Override
	public void setImageBitmap( final Bitmap bitmap, final boolean reset, Matrix matrix ) {
		clearOverlays();
		super.setImageBitmap( bitmap, reset, matrix );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouchBase#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout( boolean changed, int left, int top, int right, int bottom ) {
		super.onLayout( changed, left, top, right, bottom );

		if ( mLayoutListener != null ) mLayoutListener.onLayoutChanged( changed, left, top, right, bottom );

		if ( getDrawable() != null && changed ) {

			Iterator<DrawableHighlightView> iterator = mOverlayViews.iterator();
			while ( iterator.hasNext() ) {
				DrawableHighlightView view = iterator.next();
				view.getMatrix().set( getImageMatrix() );
				view.invalidate();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouchBase#postTranslate(float, float)
	 */
	@Override
	protected void postTranslate( float deltaX, float deltaY ) {
		super.postTranslate( deltaX, deltaY );

		Iterator<DrawableHighlightView> iterator = mOverlayViews.iterator();
		while ( iterator.hasNext() ) {
			DrawableHighlightView view = iterator.next();
			if ( getScale() != 1 ) {
				float[] mvalues = new float[9];
				getImageMatrix().getValues( mvalues );
				final float scale = mvalues[Matrix.MSCALE_X];

				if ( !mScaleWithContent ) view.getCropRectF().offset( -deltaX / scale, -deltaY / scale );
			}

			view.getMatrix().set( getImageMatrix() );
			view.invalidate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouchBase#postScale(float, float, float)
	 */
	@Override
	protected void postScale( float scale, float centerX, float centerY ) {

		if ( mOverlayViews.size() > 0 ) {
			Iterator<DrawableHighlightView> iterator = mOverlayViews.iterator();

			Matrix oldMatrix = new Matrix( getImageViewMatrix() );
			super.postScale( scale, centerX, centerY );

			while ( iterator.hasNext() ) {
				DrawableHighlightView view = iterator.next();

				if ( !mScaleWithContent ) {
					RectF cropRect = view.getCropRectF();
					RectF rect1 = view.getDisplayRect( oldMatrix, view.getCropRectF() );
					RectF rect2 = view.getDisplayRect( getImageViewMatrix(), view.getCropRectF() );

					float[] mvalues = new float[9];
					getImageViewMatrix().getValues( mvalues );
					final float currentScale = mvalues[Matrix.MSCALE_X];

					cropRect.offset( ( rect1.left - rect2.left ) / currentScale, ( rect1.top - rect2.top ) / currentScale );
					cropRect.right += -( rect2.width() - rect1.width() ) / currentScale;
					cropRect.bottom += -( rect2.height() - rect1.height() ) / currentScale;

					view.getMatrix().set( getImageMatrix() );
					view.getCropRectF().set( cropRect );
				} else {
					view.getMatrix().set( getImageMatrix() );
				}
				view.invalidate();
			}
		} else {
			super.postScale( scale, centerX, centerY );
		}
	}

	/**
	 * Ensure visible.
	 * 
	 * @param hv
	 *           the hv
	 */
	private void ensureVisible( DrawableHighlightView hv, float deltaX, float deltaY ) {
		RectF r = hv.getDrawRect();
		int panDeltaX1 = 0, panDeltaX2 = 0;
		int panDeltaY1 = 0, panDeltaY2 = 0;

		if ( deltaX > 0 ) panDeltaX1 = (int) Math.max( 0, getLeft() - r.left );
		if ( deltaX < 0 ) panDeltaX2 = (int) Math.min( 0, getRight() - r.right );

		if ( deltaY > 0 ) panDeltaY1 = (int) Math.max( 0, getTop() - r.top );

		if ( deltaY < 0 ) panDeltaY2 = (int) Math.min( 0, getBottom() - r.bottom );

		int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
		int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

		if ( panDeltaX != 0 || panDeltaY != 0 ) {
			panBy( panDeltaX, panDeltaY );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	public void onDraw( Canvas canvas ) {
		super.onDraw( canvas );

		for ( int i = 0; i < mOverlayViews.size(); i++ ) {
			canvas.save( Canvas.MATRIX_SAVE_FLAG );
			mOverlayViews.get( i ).draw( canvas );
			canvas.restore();
		}

		if ( null != mDropPaint ) {
			getDrawingRect( mTempRect );
			canvas.drawRect( mTempRect, mDropPaint );
		}
	}

	/**
	 * Clear overlays.
	 */
	public void clearOverlays() {
		setSelectedHighlightView( null );
		while ( mOverlayViews.size() > 0 ) {
			DrawableHighlightView hv = mOverlayViews.remove( 0 );
			hv.dispose();
		}
		mOverlayView = null;
		mMotionEdge = DrawableHighlightView.GROW_NONE;
	}

	/**
	 * Adds the highlight view.
	 * 
	 * @param hv
	 *           the hv
	 * @return true, if successful
	 */
	public boolean addHighlightView( DrawableHighlightView hv ) {
		for ( int i = 0; i < mOverlayViews.size(); i++ ) {
			if ( mOverlayViews.get( i ).equals( hv ) ) return false;
		}
		mOverlayViews.add( hv );
		postInvalidate();

		if ( mOverlayViews.size() == 1 ) {
			setSelectedHighlightView( hv );
		}

		return true;
	}

	/**
	 * Gets the highlight count.
	 * 
	 * @return the highlight count
	 */
	public int getHighlightCount() {
		return mOverlayViews.size();
	}

	/**
	 * Gets the highlight view at.
	 * 
	 * @param index
	 *           the index
	 * @return the highlight view at
	 */
	public DrawableHighlightView getHighlightViewAt( int index ) {
		return mOverlayViews.get( index );
	}

	/**
	 * Removes the hightlight view.
	 * 
	 * @param view
	 *           the view
	 * @return true, if successful
	 */
	public boolean removeHightlightView( DrawableHighlightView view ) {
		for ( int i = 0; i < mOverlayViews.size(); i++ ) {
			if ( mOverlayViews.get( i ).equals( view ) ) {
				DrawableHighlightView hv = mOverlayViews.remove( i );
				if ( hv.equals( mOverlayView ) ) {
					setSelectedHighlightView( null );
				}
				hv.dispose();
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onZoomAnimationCompleted( float scale ) {
		Log.i( LOG_TAG, "onZoomAnimationCompleted: " + scale );
		super.onZoomAnimationCompleted( scale );

		if ( mOverlayView != null ) {
			mOverlayView.setMode( Mode.Move );
			mMotionEdge = DrawableHighlightView.MOVE;
		}
	}

	/**
	 * Return the current selected highlight view.
	 * 
	 * @return the selected highlight view
	 */
	public DrawableHighlightView getSelectedHighlightView() {
		return mOverlayView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;

		mScaleDetector.onTouchEvent( event );

		if ( !mScaleDetector.isInProgress() ) mGestureDetector.onTouchEvent( event );

		switch ( action ) {
			case MotionEvent.ACTION_UP:

				if ( mOverlayView != null ) {
					mOverlayView.setMode( DrawableHighlightView.Mode.None );
				}
				mMotionEdge = DrawableHighlightView.GROW_NONE;

				if ( getScale() < 1f ) {
					zoomTo( 1f, 50 );
				}
				break;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch#onDoubleTapPost(float, float)
	 */
	@Override
	protected float onDoubleTapPost( float scale, float maxZoom ) {
		return super.onDoubleTapPost( scale, maxZoom );
	}

	private boolean onDoubleTap( MotionEvent e ) {
		float scale = getScale();
		float targetScale = scale;
		targetScale = ImageViewDrawableOverlay.this.onDoubleTapPost( scale, getMaxZoom() );
		targetScale = Math.min( getMaxZoom(), Math.max( targetScale, 1 ) );
		mCurrentScaleFactor = targetScale;
		zoomTo( targetScale, e.getX(), e.getY(), DEFAULT_ANIMATION_DURATION );
		invalidate();
		return true;
	}

	/**
	 * Check selection.
	 * 
	 * @param e
	 *           the e
	 * @return the drawable highlight view
	 */
	private DrawableHighlightView checkSelection( MotionEvent e ) {
		Iterator<DrawableHighlightView> iterator = mOverlayViews.iterator();
		DrawableHighlightView selection = null;
		while ( iterator.hasNext() ) {
			DrawableHighlightView view = iterator.next();
			int edge = view.getHit( e.getX(), e.getY() );
			if ( edge != DrawableHighlightView.GROW_NONE ) {
				selection = view;
			}
		}
		return selection;
	}

	/**
	 * Check up selection.
	 * 
	 * @param e
	 *           the e
	 * @return the drawable highlight view
	 */
	private DrawableHighlightView checkUpSelection( MotionEvent e ) {
		Iterator<DrawableHighlightView> iterator = mOverlayViews.iterator();
		DrawableHighlightView selection = null;
		while ( iterator.hasNext() ) {
			DrawableHighlightView view = iterator.next();
			if ( view.getSelected() ) {
				view.onSingleTapConfirmed( e.getX(), e.getY() );
			}
		}
		return selection;
	}

	/**
	 * Sets the selected highlight view.
	 * 
	 * @param newView
	 *           the new selected highlight view
	 */
	public void setSelectedHighlightView( DrawableHighlightView newView ) {

		final DrawableHighlightView oldView = mOverlayView;

		if ( mOverlayView != null && !mOverlayView.equals( newView ) ) {
			mOverlayView.setSelected( false );
		}

		if ( newView != null ) {
			newView.setSelected( true );
		}

		mOverlayView = newView;

		if ( mDrawableListener != null ) {
			mDrawableListener.onFocusChange( newView, oldView );
		}
	}

	/**
	 * The listener interface for receiving cropGesture events. The class that is interested in processing a cropGesture event
	 * implements this interface, and the object created with that class is registered with a component using the component's
	 * <code>addCropGestureListener<code> method. When
	 * the cropGesture event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see CropGestureEvent
	 */
	class CropGestureListener extends GestureDetector.SimpleOnGestureListener {
		
		boolean mScrollStarted;
		float mLastMotionX, mLastMotionY;

		@Override
		public boolean onDown( MotionEvent e ) {
			
			mScrollStarted = false;
			mLastMotionX = e.getX();
			mLastMotionY = e.getY();

			DrawableHighlightView newSelection = checkSelection( e );
			DrawableHighlightView realNewSelection = newSelection;

			if ( newSelection == null && mOverlayViews.size() == 1 && mForceSingleSelection ) {
				newSelection = mOverlayViews.get( 0 );
			}

			setSelectedHighlightView( newSelection );

			if ( realNewSelection != null && mScaleWithContent ) {
				RectF displayRect = realNewSelection.getDisplayRect( realNewSelection.getMatrix(), realNewSelection.getCropRectF() );
				boolean invalidSize = realNewSelection.getContent().validateSize( displayRect );
				if ( !invalidSize ) {

					float minW = realNewSelection.getContent().getMinWidth();
					float minH = realNewSelection.getContent().getMinHeight();

					float minSize = Math.min( minW, minH ) * 1.1f;
					float minRectSize = Math.min( displayRect.width(), displayRect.height() );

					float diff = minSize / minRectSize;

					Log.d( LOG_TAG, "drawable too small!!!" );
					Log.d( LOG_TAG, "min.size: " + minW + "x" + minH );
					Log.d( LOG_TAG, "cur.size: " + displayRect.width() + "x" + displayRect.height() );
					zoomTo( getScale() * diff, displayRect.centerX(), displayRect.centerY(), DEFAULT_ANIMATION_DURATION * 1.5f );
					return true;
				}
			}

			if ( mOverlayView != null ) {
				int edge = mOverlayView.getHit( e.getX(), e.getY() );
				if ( edge != DrawableHighlightView.GROW_NONE ) {
					mMotionEdge = edge;
					mOverlayView
							.setMode( ( edge == DrawableHighlightView.MOVE ) ? DrawableHighlightView.Mode.Move
									: ( edge == DrawableHighlightView.ROTATE ? DrawableHighlightView.Mode.Rotate
											: DrawableHighlightView.Mode.Grow ) );
					if ( mDrawableListener != null ) {
						mDrawableListener.onDown( mOverlayView );
					}
				}
			}

			return super.onDown( e );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapConfirmed(android.view.MotionEvent)
		 */
		@Override
		public boolean onSingleTapConfirmed( MotionEvent e ) {
			checkUpSelection( e );
			return super.onSingleTapConfirmed( e );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapUp(android.view.MotionEvent)
		 */
		@Override
		public boolean onSingleTapUp( MotionEvent e ) {
			if ( mOverlayView != null ) {

				int edge = mOverlayView.getHit( e.getX(), e.getY() );
				if ( ( edge & DrawableHighlightView.MOVE ) == DrawableHighlightView.MOVE ) {
					if ( mDrawableListener != null ) mDrawableListener.onClick( mOverlayView );
					return true;
				}

				mOverlayView.setMode( Mode.None );

				if ( mOverlayViews.size() != 1 ) setSelectedHighlightView( null );
			}

			return super.onSingleTapUp( e );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onDoubleTap(android.view.MotionEvent)
		 */
		@Override
		public boolean onDoubleTap( MotionEvent e ) {

			if ( !mDoubleTapEnabled ) return false;

			return ImageViewDrawableOverlay.this.onDoubleTap( e );
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent,
		 * float, float)
		 */
		@Override
		public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {

			if ( !mScrollEnabled ) return false;

			if ( e1 == null || e2 == null ) return false;
			if ( e1.getPointerCount() > 1 || e2.getPointerCount() > 1 ) return false;
			if ( mScaleDetector.isInProgress() ) return false;
			
			// remove the touch slop lag ( see bug @1084 )
			float x = e2.getX();
			float y = e2.getY();
			
			if( !mScrollStarted ){
            distanceX = 0;
            distanceY = 0;
            mScrollStarted = true;
			} else {
				distanceX = mLastMotionX - x;
				distanceY = mLastMotionY - y;
			}
			
			mLastMotionX = x;
			mLastMotionY = y;

			if ( mOverlayView != null && mMotionEdge != DrawableHighlightView.GROW_NONE ) {
				mOverlayView.onMouseMove( mMotionEdge, e2, -distanceX, -distanceY );

				if ( mDrawableListener != null ) {
					mDrawableListener.onMove( mOverlayView );
				}

				if ( mMotionEdge == DrawableHighlightView.MOVE ) {
					if ( !mScaleWithContent ) {
						ensureVisible( mOverlayView, distanceX, distanceY );
					}
				}
				return true;
			} else {
				scrollBy( -distanceX, -distanceY );
				invalidate();
				return true;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent,
		 * float, float)
		 */
		@Override
		public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {

			if ( !mScrollEnabled ) return false;

			if ( e1.getPointerCount() > 1 || e2.getPointerCount() > 1 ) return false;
			if ( mScaleDetector.isInProgress() ) return false;
			if ( mOverlayView != null && mOverlayView.getMode() != Mode.None ) return false;

			float diffX = e2.getX() - e1.getX();
			float diffY = e2.getY() - e1.getY();

			if ( Math.abs( velocityX ) > 800 || Math.abs( velocityY ) > 800 ) {
				scrollBy( diffX / 2, diffY / 2, 300 );
				invalidate();
			}
			return super.onFling( e1, e2, velocityX, velocityY );
		}
	}

	/**
	 * The listener interface for receiving cropScale events. The class that is interested in processing a cropScale event implements
	 * this interface, and the object created with that class is registered with a component using the component's
	 * <code>addCropScaleListener<code> method. When
	 * the cropScale event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see CropScaleEvent
	 */
	class CropScaleListener extends ScaleListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.sephiroth.android.library.imagezoom.ScaleGestureDetector.SimpleOnScaleGestureListener#onScaleBegin(it.sephiroth.android
		 * .library.imagezoom.ScaleGestureDetector)
		 */
		@Override
		public boolean onScaleBegin( ScaleGestureDetector detector ) {
			if ( !mScaleEnabled ) return false;
			return super.onScaleBegin( detector );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * it.sephiroth.android.library.imagezoom.ScaleGestureDetector.SimpleOnScaleGestureListener#onScaleEnd(it.sephiroth.android
		 * .library.imagezoom.ScaleGestureDetector)
		 */
		@Override
		public void onScaleEnd( ScaleGestureDetector detector ) {
			if ( !mScaleEnabled ) return;
			super.onScaleEnd( detector );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.sephiroth.android.library.imagezoom.ImageViewTouch.ScaleListener#onScale(it.sephiroth.android.library.imagezoom.
		 * ScaleGestureDetector)
		 */
		@Override
		public boolean onScale( ScaleGestureDetector detector ) {
			if ( !mScaleEnabled ) return false;
			return super.onScale( detector );
		}
	}

	@Override
	public void onDrop( DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo ) {
		if ( mDropTargetListener != null ) {
			mDropTargetListener.onDrop( source, x, y, xOffset, yOffset, dragView, dragInfo );
		}
	}

	@Override
	public void onDragEnter( DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo ) {
		mDropPaint = new Paint();
		mDropPaint.setColor( 0xff33b5e5 );
		mDropPaint.setStrokeWidth( 2 );
		mDropPaint.setMaskFilter( new BlurMaskFilter( 4.0f, Blur.NORMAL ) );
		mDropPaint.setStyle( Paint.Style.STROKE );
		invalidate();
	}

	@Override
	public void onDragOver( DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo ) {}

	@Override
	public void onDragExit( DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo ) {
		mDropPaint = null;
		invalidate();
	}

	@Override
	public boolean acceptDrop( DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo ) {
		if ( mDropTargetListener != null ) {
			return mDropTargetListener.acceptDrop( source, x, y, xOffset, yOffset, dragView, dragInfo );
		}
		return false;
	}

	@Override
	public Rect estimateDropLocation( DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo,
			Rect recycle ) {
		return null;
	}
}
