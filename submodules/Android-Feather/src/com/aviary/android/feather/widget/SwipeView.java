package com.aviary.android.feather.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * acts a an swipe gesture overylay for a view
 */
public class SwipeView extends View {

	public static interface OnSwipeListener {

		// allow classes to implement as they wish when they overlay a SwipeView
		public void onSwipe( boolean leftToRight );
	}

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private OnSwipeListener mOnSwipeListener;
	View.OnTouchListener gestureListener;
	private GestureDetector gestureDetector;

	public SwipeView( Context context ) {
		super( context );
		// TODO Auto-generated constructor stub

		gestureDetector = new GestureDetector( new SwipeDetector() );
		gestureListener = new View.OnTouchListener() {

			public boolean onTouch( View v, MotionEvent event ) {
				return gestureDetector.onTouchEvent( event );
			}
		};

		this.setOnTouchListener( gestureListener );

	}

	public SwipeView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		// TODO Auto-generated constructor stub

		gestureDetector = new GestureDetector( new SwipeDetector() );
		gestureListener = new View.OnTouchListener() {

			public boolean onTouch( View v, MotionEvent event ) {
				return gestureDetector.onTouchEvent( event );
			}
		};

		this.setOnTouchListener( gestureListener );
	}

	public SwipeView( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
		// TODO Auto-generated constructor stub

		gestureDetector = new GestureDetector( new SwipeDetector() );
		gestureListener = new View.OnTouchListener() {

			public boolean onTouch( View v, MotionEvent event ) {
				return gestureDetector.onTouchEvent( event );
			}
		};

		this.setOnTouchListener( gestureListener );
	}

	public OnSwipeListener getOnSwipeListener() {
		return mOnSwipeListener;
	}

	public void setOnSwipeListener( OnSwipeListener swipeDetector ) {
		mOnSwipeListener = swipeDetector;
	}

	public class SwipeDetector extends SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap( MotionEvent e ) {
			return false;
		}

		@Override
		public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
			return false;
		}

		@Override
		public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {
			try {
				if ( Math.abs( e1.getY() - e2.getY() ) > SWIPE_MAX_OFF_PATH ) return false;
				// right to left swipe
				if ( e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs( velocityX ) > SWIPE_THRESHOLD_VELOCITY ) {
					if ( mOnSwipeListener != null ) {
						mOnSwipeListener.onSwipe( false );
					}
				}
				// left to right swipe
				else if ( e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs( velocityX ) > SWIPE_THRESHOLD_VELOCITY ) {
					if ( mOnSwipeListener != null ) {
						mOnSwipeListener.onSwipe( true );
					}
				}

			} catch ( Exception e ) {
				// nothing
			}
			return false;
		}

	}

}
