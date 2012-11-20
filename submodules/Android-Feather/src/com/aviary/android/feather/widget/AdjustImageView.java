package com.aviary.android.feather.widget;

import it.sephiroth.android.library.imagezoom.easing.Easing;
import it.sephiroth.android.library.imagezoom.easing.Expo;
import it.sephiroth.android.library.imagezoom.easing.Linear;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;
import com.aviary.android.feather.R;
import com.aviary.android.feather.library.graphics.Point2D;
import com.aviary.android.feather.library.log.LoggerFactory;
import com.aviary.android.feather.library.log.LoggerFactory.Logger;
import com.aviary.android.feather.library.log.LoggerFactory.LoggerType;
import com.aviary.android.feather.library.utils.ReflectionUtils;
import com.aviary.android.feather.library.utils.ReflectionUtils.ReflectionException;

// TODO: Auto-generated Javadoc
/**
 * Displays an arbitrary image, such as an icon. The ImageView class can load images from various sources (such as resources or
 * content providers), takes care of computing its measurement from the image so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 * 
 * @attr ref android.R.styleable#ImageView_adjustViewBounds
 * @attr ref android.R.styleable#ImageView_src
 * @attr ref android.R.styleable#ImageView_maxWidth
 * @attr ref android.R.styleable#ImageView_maxHeight
 * @attr ref android.R.styleable#ImageView_tint
 * @attr ref android.R.styleable#ImageView_scaleType
 * @attr ref android.R.styleable#ImageView_cropToPadding
 */
@SuppressLint("NewApi")
@RemoteView
public class AdjustImageView extends View {

	/** The Constant LOG_TAG. */
	static final String LOG_TAG = "rotate";

	// settable by the client
	/** The m uri. */
	private Uri mUri;

	/** The m resource. */
	private int mResource = 0;

	/** The m matrix. */
	private Matrix mMatrix;

	/** The m scale type. */
	private ScaleType mScaleType;

	/** The m adjust view bounds. */
	private boolean mAdjustViewBounds = false;

	/** The m max width. */
	private int mMaxWidth = Integer.MAX_VALUE;

	/** The m max height. */
	private int mMaxHeight = Integer.MAX_VALUE;

	// these are applied to the drawable
	/** The m color filter. */
	private ColorFilter mColorFilter;

	/** The m alpha. */
	private int mAlpha = 255;

	/** The m view alpha scale. */
	private int mViewAlphaScale = 256;

	/** The m color mod. */
	private boolean mColorMod = false;

	/** The m drawable. */
	private Drawable mDrawable = null;

	/** The m state. */
	private int[] mState = null;

	/** The m merge state. */
	private boolean mMergeState = false;

	/** The m level. */
	private int mLevel = 0;

	/** The m drawable width. */
	private int mDrawableWidth;

	/** The m drawable height. */
	private int mDrawableHeight;

	/** The m draw matrix. */
	private Matrix mDrawMatrix = null;

	private Matrix mTempMatrix = new Matrix();

	/** The m rotate matrix. */
	private Matrix mRotateMatrix = new Matrix();

	/** The m flip matrix. */
	private Matrix mFlipMatrix = new Matrix();

	// Avoid allocations...
	/** The m temp src. */
	private RectF mTempSrc = new RectF();

	/** The m temp dst. */
	private RectF mTempDst = new RectF();

	/** The m crop to padding. */
	private boolean mCropToPadding;

	/** The m baseline. */
	private int mBaseline = -1;

	/** The m baseline align bottom. */
	private boolean mBaselineAlignBottom = false;

	/** The m have frame. */
	private boolean mHaveFrame;

	/** The m easing. */
	private Easing mEasing = new Expo();

	/** View is in the reset state. */
	boolean isReset = false;

	/** reset animation time. */
	int resetAnimTime = 200;

	Path mClipPath = new Path();
	Path mInversePath = new Path();
	Rect mViewDrawRect = new Rect();
	RectF mViewInvertRect = new RectF();
	Paint mOutlinePaint = new Paint();
	Paint mOutlineFill = new Paint();
	RectF mDrawRect;
	PointF mCenter = new PointF();
	Path mLinesPath = new Path();
	Paint mLinesPaint = new Paint();
	Paint mLinesPaintShadow = new Paint();
	// Drawable Drawable;
	Drawable mStraightenDrawable;
	int handleWidth, handleHeight;
	final int grid_rows = 8;
	final int grid_cols = 8;

	private boolean mEnableFreeRotate;

	static Logger logger = LoggerFactory.getLogger( "rotate", LoggerType.ConsoleLoggerType );

	/**
	 * Sets the reset anim duration.
	 * 
	 * @param value
	 *           the new reset anim duration
	 */
	public void setResetAnimDuration( int value ) {
		resetAnimTime = value;
	}

	public void setEnableFreeRotate( boolean value ) {
		mEnableFreeRotate = value;
	}

	public boolean isFreeRotateEnabled() {
		return mEnableFreeRotate;
	}

	/**
	 * The listener interface for receiving onReset events. The class that is interested in processing a onReset event implements
	 * this interface, and the object created with that class is registered with a component using the component's
	 * <code>addOnResetListener<code> method. When
	 * the onReset event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnResetEvent
	 */
	public interface OnResetListener {

		/**
		 * On reset complete.
		 */
		void onResetComplete();
	}

	/** The m reset listener. */
	private OnResetListener mResetListener;

	/**
	 * Sets the on reset listener.
	 * 
	 * @param listener
	 *           the new on reset listener
	 */
	public void setOnResetListener( OnResetListener listener ) {
		mResetListener = listener;
	}

	/** The Constant sScaleTypeArray. */
	@SuppressWarnings("unused")
	private static final ScaleType[] sScaleTypeArray = {
		ScaleType.MATRIX, ScaleType.FIT_XY, ScaleType.FIT_START, ScaleType.FIT_CENTER, ScaleType.FIT_END, ScaleType.CENTER,
		ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE };

	/**
	 * Instantiates a new adjust image view.
	 * 
	 * @param context
	 *           the context
	 */
	public AdjustImageView( Context context ) {
		super( context );
		initImageView();
	}

	/**
	 * Instantiates a new adjust image view.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public AdjustImageView( Context context, AttributeSet attrs ) {
		this( context, attrs, 0 );
	}

	/**
	 * Instantiates a new adjust image view.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 * @param defStyle
	 *           the def style
	 */
	public AdjustImageView( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
		initImageView();
	}

	/**
	 * Sets the easing.
	 * 
	 * @param value
	 *           the new easing
	 */
	public void setEasing( Easing value ) {
		mEasing = value;
	}

	int mOutlinePaintAlpha, mOutlineFillAlpha, mLinesAlpha, mLinesShadowAlpha;

	/**
	 * Inits the image view.
	 */
	private void initImageView() {
		mMatrix = new Matrix();
		mScaleType = ScaleType.FIT_CENTER;

		Context context = getContext();
		int highlight_color = context.getResources().getColor( R.color.feather_rotate_highlight_stroke_color );
		int highlight_stroke_internal_color = context.getResources().getColor( R.color.feather_rotate_highlight_grid_stroke_color );
		int highlight_stroke_internal_width = context.getResources()
				.getInteger( R.integer.feather_rotate_highlight_grid_stroke_width );
		int highlight_outside_color = context.getResources().getColor( R.color.feather_rotate_highlight_outside );
		int highlight_stroke_width = context.getResources().getInteger( R.integer.feather_rotate_highlight_stroke_width );

		mOutlinePaint.setStrokeWidth( highlight_stroke_width );
		mOutlinePaint.setStyle( Paint.Style.STROKE );
		mOutlinePaint.setAntiAlias( true );
		mOutlinePaint.setColor( highlight_color );

		mOutlineFill.setStyle( Paint.Style.FILL );
		mOutlineFill.setAntiAlias( false );
		mOutlineFill.setColor( highlight_outside_color );
		mOutlineFill.setDither( false );

		try {
			ReflectionUtils.invokeMethod( mOutlineFill, "setHinting", new Class<?>[] { int.class }, 0 );
		} catch ( ReflectionException e ) {}

		mLinesPaint.setStrokeWidth( highlight_stroke_internal_width );
		mLinesPaint.setAntiAlias( false );
		mLinesPaint.setDither( false );
		mLinesPaint.setStyle( Paint.Style.STROKE );
		mLinesPaint.setColor( highlight_stroke_internal_color );
		try {
			ReflectionUtils.invokeMethod( mLinesPaint, "setHinting", new Class<?>[] { int.class }, 0 );
		} catch ( ReflectionException e ) {}

		mLinesPaintShadow.setStrokeWidth( highlight_stroke_internal_width );
		mLinesPaintShadow.setAntiAlias( true );
		mLinesPaintShadow.setColor( Color.BLACK );
		mLinesPaintShadow.setStyle( Paint.Style.STROKE );
		mLinesPaintShadow.setMaskFilter( new BlurMaskFilter( 2, Blur.NORMAL ) );

		mOutlineFillAlpha = mOutlineFill.getAlpha();
		mOutlinePaintAlpha = mOutlinePaint.getAlpha();
		mLinesAlpha = mLinesPaint.getAlpha();
		mLinesShadowAlpha = mLinesPaintShadow.getAlpha();

		mOutlinePaint.setAlpha( 0 );
		mOutlineFill.setAlpha( 0 );
		mLinesPaint.setAlpha( 0 );
		mLinesPaintShadow.setAlpha( 0 );

		android.content.res.Resources resources = getContext().getResources();
		mStraightenDrawable = resources.getDrawable( R.drawable.feather_straighten_knob );
		double w = mStraightenDrawable.getIntrinsicWidth();
		double h = mStraightenDrawable.getIntrinsicHeight();
		handleWidth = (int) Math.ceil( w / 2.0 );
		handleHeight = (int) Math.ceil( h / 2.0 );

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#verifyDrawable(android.graphics.drawable.Drawable)
	 */
	@Override
	protected boolean verifyDrawable( Drawable dr ) {
		return mDrawable == dr || super.verifyDrawable( dr );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#invalidateDrawable(android.graphics.drawable.Drawable)
	 */
	@Override
	public void invalidateDrawable( Drawable dr ) {
		if ( dr == mDrawable ) {
			/*
			 * we invalidate the whole view in this case because it's very hard to know where the drawable actually is. This is made
			 * complicated because of the offsets and transformations that can be applied. In theory we could get the drawable's bounds
			 * and run them through the transformation and offsets, but this is probably not worth the effort.
			 */
			invalidate();
		} else {
			super.invalidateDrawable( dr );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onSetAlpha(int)
	 */
	@Override
	protected boolean onSetAlpha( int alpha ) {
		if ( getBackground() == null ) {
			int scale = alpha + ( alpha >> 7 );
			if ( mViewAlphaScale != scale ) {
				mViewAlphaScale = scale;
				mColorMod = true;
				applyColorMod();
			}
			return true;
		}
		return false;
	}

	private PointF getCenter() {
		final int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
		final int vheight = getHeight() - getPaddingTop() - getPaddingBottom();
		return new PointF( (float) vwidth / 2, (float) vheight / 2 );
	}

	private RectF getViewRect() {
		final int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
		final int vheight = getHeight() - getPaddingTop() - getPaddingBottom();
		return new RectF( 0, 0, vwidth, vheight );
	}

	private RectF getImageRect() {
		return new RectF( 0, 0, mDrawableWidth, mDrawableHeight );
	}

	private void onTouchStart() {

		if ( mFadeHandlerStarted ) {
			fadeinGrid( 300 );
		} else {
			fadeinOutlines( 600 );
		}
	}

	/**
	 * private void onTouchMove( float x, float y ) { if ( isDown ) { PointF current = new PointF( x, y ); PointF center =
	 * getCenter();
	 * 
	 * float angle = (float) Point2D.angle360( originalAngle - Point2D.angleBetweenPoints( center, current ) );
	 * 
	 * logger.log( "ANGLE: " + angle + " .. " + getAngle90( angle ) );
	 * 
	 * setImageRotation( angle, false ); mRotation = angle; invalidate(); } }
	 */
	private void setImageRotation( double angle, boolean invert ) {
		PointF center = getCenter();

		Matrix tempMatrix = new Matrix( mDrawMatrix );
		RectF src = getImageRect();
		RectF dst = getViewRect();

		tempMatrix.setRotate( (float) angle, center.x, center.y );
		tempMatrix.mapRect( src );
		tempMatrix.setRectToRect( src, dst, scaleTypeToScaleToFit( mScaleType ) );

		float[] scale = getMatrixScale( tempMatrix );
		float fScale = Math.min( scale[0], scale[1] );

		if ( invert ) {
			mRotateMatrix.setRotate( (float) angle, center.x, center.y );
			mRotateMatrix.postScale( fScale, fScale, center.x, center.y );
		} else {
			mRotateMatrix.setScale( fScale, fScale, center.x, center.y );
			mRotateMatrix.postRotate( (float) angle, center.x, center.y );
		}

	}

	private void onTouchUp() {
		invalidate();
		fadeoutGrid( 300 );
	}

	boolean straightenStarted = false;
	double previousStraightenAngle = 0;
	double prevGrowth = 1;
	double currentNewPosition;
	boolean testStraighten = true;
	double prevGrowthAngle = 0;
	float currentGrowth = 0;
	Matrix mStraightenMatrix = new Matrix();
	double previousAngle = 0;
	boolean intersectPoints = true;
	boolean portrait = false;
	int orientation = 0; // the orientation of the screen, whether in landscape or portrait

	public double getGrowthFactor() {
		return prevGrowth;
	}

	public double getStraightenAngle() {
		return previousStraightenAngle;
	}

	/**
	 * 
	 * Calculates the new angle and size of of the image through matrix and geometric operations
	 * 
	 * @param angleDifference
	 *           - difference between previous angle and current angle
	 * @param direction
	 *           - if there is an increase or decrease in angle
	 * @param newPosition
	 *           - the new destination angle
	 */
	private void setStraightenRotation( double newPosition ) {
		
		logger.info( "setStraightenRotation: " + newPosition + ", prev: " + previousStraightenAngle );

		// angle here is the difference between previous angle and new angle
		// you need to take advantage of the third parameter, newPosition
		double growthFactor = 1;

		//newPosition = newPosition / 2;

		currentNewPosition = newPosition;
		PointF center = getCenter();

		mStraightenMatrix.postRotate( (float) -previousStraightenAngle, center.x, center.y );

		mStraightenMatrix.postRotate( (float) newPosition, center.x, center.y );
		previousStraightenAngle = newPosition;

		double divideGrowth = 1 / prevGrowth;
		mStraightenMatrix.postScale( (float) divideGrowth, (float) divideGrowth, center.x, center.y );

		prevGrowthAngle = newPosition;

		if ( portrait ) {
			// this algorithm works slightly differently between landscape and portrait images because of the proportions

			final double sin_rad = Math.sin( Math.toRadians( currentNewPosition ) );
			final double cos_rad = Math.cos( Math.toRadians( currentNewPosition ) );

			float[] testPoint = {
				(float) ( imageCaptureRegion.left + sin_rad * getPaddingLeft() + cos_rad * getPaddingLeft() ),
				(float) ( imageCaptureRegion.top - sin_rad * getPaddingTop() + cos_rad * getPaddingLeft() ),
				(float) ( imageCaptureRegion.right + sin_rad * getPaddingRight() + cos_rad * getPaddingRight() ),
				(float) ( imageCaptureRegion.top - sin_rad * getPaddingTop() + cos_rad * getPaddingLeft() ),
				(float) ( imageCaptureRegion.left + sin_rad * getPaddingLeft() + cos_rad * getPaddingLeft() ),
				(float) ( imageCaptureRegion.bottom - sin_rad * getPaddingBottom() + cos_rad * getPaddingBottom() ),
				(float) ( imageCaptureRegion.right + sin_rad * getPaddingRight() + cos_rad * getPaddingRight() ),
				(float) ( imageCaptureRegion.bottom - sin_rad * getPaddingBottom() + cos_rad * getPaddingBottom() ) };

			mStraightenMatrix.mapPoints( testPoint );

			/**
			 * ax = trueRect.left+getPaddingLeft(); ay = trueRect.top+getPaddingTop(); bx = trueRect.right+getPaddingRight(); by =
			 * trueRect.top+getPaddingTop(); cx = trueRect.left+getPaddingLeft(); cy = trueRect.bottom+getPaddingBottom(); dx =
			 * trueRect.right+getPaddingRight(); dy = trueRect.bottom+getPaddingBottom();
			 */

			float x1 = (float) ( imageCaptureRegion.right + sin_rad * getPaddingRight() + cos_rad * getPaddingRight() );
			float y1 = (float) ( imageCaptureRegion.top - sin_rad * getPaddingTop() + cos_rad * getPaddingTop() );
			float x2 = (float) ( imageCaptureRegion.right + sin_rad * getPaddingRight() + cos_rad * getPaddingRight() );
			float y2 = (float) ( imageCaptureRegion.bottom - sin_rad * getPaddingBottom() + cos_rad * getPaddingBottom() );
			float x3 = testPoint[2];
			float y3 = testPoint[3];
			float x4 = testPoint[6];
			float y4 = testPoint[7];

			double numerator2 = ( x1 * y2 - y1 * x2 ) * ( y3 - y4 ) - ( y1 - y2 ) * ( x3 * y4 - y3 * x4 );
			double denominator2 = ( ( x1 - x2 ) * ( y3 - y4 ) - ( y1 - y2 ) * ( x3 - x4 ) );

			double Px = imageCaptureRegion.right + getPaddingRight();
			double Py = ( numerator2 ) / ( denominator2 ) + getPaddingBottom();

			orientation = getResources().getConfiguration().orientation;
			if ( orientation == Configuration.ORIENTATION_LANDSCAPE && newPosition > 0 ) {
				Py = ( numerator2 ) / ( denominator2 ) + sin_rad * getPaddingBottom();
			}

			double dx = Px - x2;
			double dy = Py - y2;

			if ( newPosition < 0 ) {
				dx = Px - x1;
				dy = Py - y1;
			}

			double distance = Math.sqrt( dx * dx + dy * dy );
			double amountNeededToGrow = ( 2 * distance * ( Math.sin( Math.toRadians( Math.abs( newPosition ) ) ) ) );
			distance = FloatMath.sqrt( ( testPoint[0] - testPoint[2] ) * ( testPoint[0] - testPoint[2] ) );

			if ( newPosition != 0 ) {
				growthFactor = ( distance + amountNeededToGrow ) / distance;

				mStraightenMatrix.postScale( (float) growthFactor, (float) growthFactor, center.x, center.y );

			} else {
				growthFactor = 1;
			}
			// intersectx = (float) Px;
			// intersecty = (float) Py;

		}

		else {

			final double sin_rad = Math.sin( Math.toRadians( currentNewPosition ) );
			final double cos_rad = Math.cos( Math.toRadians( currentNewPosition ) );

			float[] testPoint = {
				(float) ( imageCaptureRegion.left + sin_rad * getPaddingLeft() + cos_rad * getPaddingLeft() ),
				(float) ( imageCaptureRegion.top - sin_rad * getPaddingTop() + cos_rad * getPaddingLeft() ),
				(float) ( imageCaptureRegion.right + sin_rad * getPaddingRight() + cos_rad * getPaddingRight() ),
				(float) ( imageCaptureRegion.top - sin_rad * getPaddingTop() + cos_rad * getPaddingLeft() ),
				(float) ( imageCaptureRegion.left + sin_rad * getPaddingLeft() + cos_rad * getPaddingLeft() ),
				(float) ( imageCaptureRegion.bottom - sin_rad * getPaddingBottom() + cos_rad * getPaddingBottom() ),
				(float) ( imageCaptureRegion.right + sin_rad * getPaddingRight() + cos_rad * getPaddingRight() ),
				(float) ( imageCaptureRegion.bottom - sin_rad * getPaddingBottom() + cos_rad * getPaddingBottom() ) };

			mStraightenMatrix.mapPoints( testPoint );
			/**
			 * ax = testPoint[0]; ay = testPoint[1]; bx = testPoint[2]; by = testPoint[3]; cx = testPoint[4]; cy = testPoint[5]; dx =
			 * testPoint[6]; dy = testPoint[7];
			 */

			float x1 = (float) ( imageCaptureRegion.left + sin_rad * getPaddingLeft() + cos_rad * getPaddingLeft() );
			float y1 = (float) ( imageCaptureRegion.bottom - sin_rad * getPaddingBottom() + cos_rad * getPaddingBottom() );
			float x2 = (float) ( imageCaptureRegion.right + sin_rad * getPaddingRight() + cos_rad * getPaddingRight() );
			float y2 = (float) ( imageCaptureRegion.bottom - sin_rad * getPaddingBottom() + cos_rad * getPaddingBottom() );
			float x3 = testPoint[4];
			float y3 = testPoint[5];
			float x4 = testPoint[6];
			float y4 = testPoint[7];

			double numerator1 = ( x1 * y2 - y1 * x2 ) * ( x3 - x4 ) - ( x1 - x2 ) * ( x3 * y4 - y3 * x4 );
			double denominator1 = ( ( x1 - x2 ) * ( y3 - y4 ) - ( y1 - y2 ) * ( x3 - x4 ) );

			double Px = ( numerator1 ) / ( denominator1 ) + getPaddingLeft();
			double Py = imageCaptureRegion.bottom + getPaddingBottom();
			double dx = Px - x1;
			double dy = Py - y1;

			if ( newPosition < 0 ) {
				dx = Px - x2;
				dy = Py - y2;
			}

			double distance = Math.sqrt( dx * dx + dy * dy );
			double amountNeededToGrow = ( 2 * distance * ( Math.sin( Math.toRadians( Math.abs( newPosition ) ) ) ) );
			distance = FloatMath.sqrt( ( testPoint[5] - testPoint[1] ) * ( testPoint[5] - testPoint[1] ) );

			if ( newPosition != 0 ) {
				growthFactor = ( distance + amountNeededToGrow ) / distance;
				mStraightenMatrix.postScale( (float) growthFactor, (float) growthFactor, center.x, center.y );

			} else {
				growthFactor = 1;
			}
			// intersectx = (float) Px;
			// intersecty = (float) Py;
		}

		// now the resize-grow stuff
		prevGrowth = growthFactor;

	}

	/**
	 * 
	 * The top level call for the straightening of the image
	 * 
	 * @param newPosition
	 *           - the destination angle for the image
	 * @param durationMs
	 *           - animation time
	 * @param direction
	 *           - if there is increase or decrease of angle of rotation
	 */
	public void straighten( final double newPosition, final int durationMs ) {

		if ( mRunning ) {
			return;
		}
		mRunning = true;
		straightenStarted = true;
		invalidate();
		mHandler.post( new Runnable() {

			@Override
			public void run() {
				/**
				 * If, for example, the current rotation position is 2 degrees and then we want the original photo to be rotated 45
				 * degrees, we can simply rotate the current photo 43 degrees...
				 */
				setStraightenRotation( newPosition );
				invalidate();
				mRunning = false;
			}
		} );
	}
	
	/**
	 * 
	 * The top level call for the straightening of the image
	 * 
	 * @param newPosition
	 *           - the destination angle for the image
	 * @param durationMs
	 *           - animation time
	 * @param direction
	 *           - if there is increase or decrease of angle of rotation
	 */
	public void straightenBy( final double newPosition, final int durationMs ) {
		logger.info( "straightenBy: " + newPosition + ", duration: " + durationMs );

		if ( mRunning ) {
			return;
		}
		
		mRunning = true;
		straightenStarted = true;
		
		final long startTime = System.currentTimeMillis();
		final double destRotation = getStraightenAngle() + newPosition;
		final double srcRotation = getStraightenAngle();
		logger.info( "destRotation: " + destRotation );
		invalidate();
		
		mHandler.post( new Runnable() {

			@Override
			public void run() {
				long now = System.currentTimeMillis();
				float currentMs = Math.min( durationMs, now - startTime );
				double new_rotation = (mEasing.easeInOut( currentMs, 0, newPosition, durationMs ));
				logger.log( "straightenBy... new_rotation: " + new_rotation );
				logger.log( "time: " + currentMs );
				
				setStraightenRotation( srcRotation + new_rotation );
				invalidate();
				
				if( currentMs < durationMs ){
					mHandler.post( this );
				} else {
					setStraightenRotation( destRotation );
					invalidate();
					mRunning = false;
					
					if( isReset ){
						straightenStarted = false;
						onReset();
					}
				}
				
			}
		} );
	}	

	private float mLastTouchX;
	private float mPosX;

	Rect testRect = new Rect( 0, 0, 0, 0 );

	final int straightenDuration = 50;
	int previousPosition = 0;
	boolean initTool = true;

	@Override
	public boolean onTouchEvent( MotionEvent ev ) {

		if ( !mEnableFreeRotate ) return true;

		final int action = ev.getAction();

		if ( initStraighten ) {
			resetStraighten();
		}

		switch ( action ) {
			case MotionEvent.ACTION_DOWN: {
				final float x = ev.getX();
				onTouchStart();
				// Remember where we started
				mLastTouchX = x;
				break;
			}

			case MotionEvent.ACTION_MOVE: {
				final float x = ev.getX();
				final float y = ev.getY();

				// Calculate the distance moved
				final float dx = x - mLastTouchX;

				// Move the object
				mPosX += dx;

				// Remember this touch position for the next move event
				mLastTouchX = x;

				Rect bounds = mStraightenDrawable.getBounds();
				Rect straightenMoveRegion = new Rect( (int) imageCaptureRegion.left + getPaddingLeft(), bounds.top - 50,
						(int) imageCaptureRegion.right + getPaddingRight(), bounds.bottom + 70 );
				testRect = new Rect( straightenMoveRegion );

				if ( straightenMoveRegion.contains( (int) x, (int) y ) ) {
					// if the move is within the straighten tool touch bounds
					if ( mPosX > imageCaptureRegion.right ) {
						mPosX = imageCaptureRegion.right;
					}
					if ( mPosX < imageCaptureRegion.left ) {
						mPosX = imageCaptureRegion.left;
					}
					mStraightenDrawable.setBounds( (int) ( mPosX - handleWidth ), (int) ( imageCaptureRegion.bottom - handleHeight ),
							(int) ( mPosX + handleWidth ), (int) ( imageCaptureRegion.bottom + handleHeight ) );

					// now get the angle from the distance
					double midPoint = getCenter().x;
					double maxAngle = ( 45 * imageCaptureRegion.right ) / midPoint - 45;
					double tempAngle = ( 45 * mPosX ) / midPoint - 45;
					double angle = ( 45 * tempAngle ) / maxAngle;

					straighten( angle/2, straightenDuration );
				}

				// Invalidate to request a redraw
				invalidate();
				break;
			}

			case MotionEvent.ACTION_UP: {
				onTouchUp();
				break;
			}

		}

		return true;
	}

	private double getRotationFromMatrix( Matrix matrix ) {
		float[] pts = { 0, 0, 0, -100 };
		matrix.mapPoints( pts );
		double angle = Point2D.angleBetweenPoints( pts[0], pts[1], pts[2], pts[3], 0 );
		return -angle;
	}

	/**
	 * Set this to true if you want the ImageView to adjust its bounds to preserve the aspect ratio of its drawable.
	 * 
	 * @param adjustViewBounds
	 *           Whether to adjust the bounds of this view to presrve the original aspect ratio of the drawable
	 * 
	 * @attr ref android.R.styleable#ImageView_adjustViewBounds
	 */
	public void setAdjustViewBounds( boolean adjustViewBounds ) {
		mAdjustViewBounds = adjustViewBounds;
		if ( adjustViewBounds ) {
			setScaleType( ScaleType.FIT_CENTER );
		}
	}

	/**
	 * An optional argument to supply a maximum width for this view. Only valid if {@link #setAdjustViewBounds(boolean)} has been set
	 * to true. To set an image to be a maximum of 100 x 100 while preserving the original aspect ratio, do the following: 1) set
	 * adjustViewBounds to true 2) set maxWidth and maxHeight to 100 3) set the height and width layout params to WRAP_CONTENT.
	 * 
	 * <p>
	 * Note that this view could be still smaller than 100 x 100 using this approach if the original image is small. To set an image
	 * to a fixed size, specify that size in the layout params and then use {@link #setScaleType(android.widget.ImageView.ScaleType)}
	 * to determine how to fit the image within the bounds.
	 * </p>
	 * 
	 * @param maxWidth
	 *           maximum width for this view
	 * 
	 * @attr ref android.R.styleable#ImageView_maxWidth
	 */
	public void setMaxWidth( int maxWidth ) {
		mMaxWidth = maxWidth;
	}

	/**
	 * An optional argument to supply a maximum height for this view. Only valid if {@link #setAdjustViewBounds(boolean)} has been
	 * set to true. To set an image to be a maximum of 100 x 100 while preserving the original aspect ratio, do the following: 1) set
	 * adjustViewBounds to true 2) set maxWidth and maxHeight to 100 3) set the height and width layout params to WRAP_CONTENT.
	 * 
	 * <p>
	 * Note that this view could be still smaller than 100 x 100 using this approach if the original image is small. To set an image
	 * to a fixed size, specify that size in the layout params and then use {@link #setScaleType(android.widget.ImageView.ScaleType)}
	 * to determine how to fit the image within the bounds.
	 * </p>
	 * 
	 * @param maxHeight
	 *           maximum height for this view
	 * 
	 * @attr ref android.R.styleable#ImageView_maxHeight
	 */
	public void setMaxHeight( int maxHeight ) {
		mMaxHeight = maxHeight;
	}

	/**
	 * Return the view's drawable, or null if no drawable has been assigned.
	 * 
	 * @return the drawable
	 */
	public Drawable getDrawable() {
		return mDrawable;
	}

	/**
	 * Sets a drawable as the content of this ImageView.
	 * 
	 * <p class="note">
	 * This does Bitmap reading and decoding on the UI thread, which can cause a latency hiccup. If that's a concern, consider using
	 * 
	 * @param resId
	 *           the resource identifier of the the drawable {@link #setImageDrawable(android.graphics.drawable.Drawable)} or
	 *           {@link #setImageBitmap(android.graphics.Bitmap)} and {@link android.graphics.BitmapFactory} instead.
	 *           </p>
	 * @attr ref android.R.styleable#ImageView_src
	 */
	public void setImageResource( int resId ) {
		if ( mUri != null || mResource != resId ) {
			updateDrawable( null );
			mResource = resId;
			mUri = null;
			resolveUri();
			requestLayout();
			invalidate();
		}
	}

	/**
	 * Sets the content of this ImageView to the specified Uri.
	 * 
	 * <p class="note">
	 * This does Bitmap reading and decoding on the UI thread, which can cause a latency hiccup. If that's a concern, consider using
	 * 
	 * @param uri
	 *           The Uri of an image {@link #setImageDrawable(android.graphics.drawable.Drawable)} or
	 *           {@link #setImageBitmap(android.graphics.Bitmap)} and {@link android.graphics.BitmapFactory} instead.
	 *           </p>
	 */
	public void setImageURI( Uri uri ) {
		if ( mResource != 0 || ( mUri != uri && ( uri == null || mUri == null || !uri.equals( mUri ) ) ) ) {
			updateDrawable( null );
			mResource = 0;
			mUri = uri;
			resolveUri();
			requestLayout();
			invalidate();
		}
	}

	/**
	 * Sets a drawable as the content of this ImageView.
	 * 
	 * @param drawable
	 *           The drawable to set
	 */
	public void setImageDrawable( Drawable drawable ) {
		if ( mDrawable != drawable ) {
			mResource = 0;
			mUri = null;

			int oldWidth = mDrawableWidth;
			int oldHeight = mDrawableHeight;

			updateDrawable( drawable );

			if ( oldWidth != mDrawableWidth || oldHeight != mDrawableHeight ) {
				requestLayout();
			}
			invalidate();
		}
	}

	/**
	 * Sets a Bitmap as the content of this ImageView.
	 * 
	 * @param bm
	 *           The bitmap to set
	 */
	public void setImageBitmap( Bitmap bm ) {
		// if this is used frequently, may handle bitmaps explicitly
		// to reduce the intermediate drawable object
		setImageDrawable( new BitmapDrawable( getContext().getResources(), bm ) );
	}

	/**
	 * Sets the image state.
	 * 
	 * @param state
	 *           the state
	 * @param merge
	 *           the merge
	 */
	public void setImageState( int[] state, boolean merge ) {
		mState = state;
		mMergeState = merge;
		if ( mDrawable != null ) {
			refreshDrawableState();
			resizeFromDrawable();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#setSelected(boolean)
	 */
	@Override
	public void setSelected( boolean selected ) {
		super.setSelected( selected );
		resizeFromDrawable();
	}

	/**
	 * Sets the image level, when it is constructed from a {@link android.graphics.drawable.LevelListDrawable}.
	 * 
	 * @param level
	 *           The new level for the image.
	 */
	public void setImageLevel( int level ) {
		mLevel = level;
		if ( mDrawable != null ) {
			mDrawable.setLevel( level );
			resizeFromDrawable();
		}
	}

	/**
	 * Options for scaling the bounds of an image to the bounds of this view.
	 */
	public enum ScaleType {
		/**
		 * Scale using the image matrix when drawing. The image matrix can be set using {@link ImageView#setImageMatrix(Matrix)}. From
		 * XML, use this syntax: <code>android:scaleType="matrix"</code>.
		 */
		MATRIX( 0 ),
		/**
		 * Scale the image using {@link Matrix.ScaleToFit#FILL}. From XML, use this syntax: <code>android:scaleType="fitXY"</code>.
		 */
		FIT_XY( 1 ),
		/**
		 * Scale the image using {@link Matrix.ScaleToFit#START}. From XML, use this syntax: <code>android:scaleType="fitStart"</code>
		 * .
		 */
		FIT_START( 2 ),
		/**
		 * Scale the image using {@link Matrix.ScaleToFit#CENTER}. From XML, use this syntax:
		 * <code>android:scaleType="fitCenter"</code>.
		 */
		FIT_CENTER( 3 ),
		/**
		 * Scale the image using {@link Matrix.ScaleToFit#END}. From XML, use this syntax: <code>android:scaleType="fitEnd"</code>.
		 */
		FIT_END( 4 ),
		/**
		 * Center the image in the view, but perform no scaling. From XML, use this syntax: <code>android:scaleType="center"</code>.
		 */
		CENTER( 5 ),
		/**
		 * Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions (width and height) of the image will
		 * be equal to or larger than the corresponding dimension of the view (minus padding). The image is then centered in the view.
		 * From XML, use this syntax: <code>android:scaleType="centerCrop"</code>.
		 */
		CENTER_CROP( 6 ),
		/**
		 * Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions (width and height) of the image will
		 * be equal to or less than the corresponding dimension of the view (minus padding). The image is then centered in the view.
		 * From XML, use this syntax: <code>android:scaleType="centerInside"</code>.
		 */
		CENTER_INSIDE( 7 );

		/**
		 * Instantiates a new scale type.
		 * 
		 * @param ni
		 *           the ni
		 */
		ScaleType( int ni ) {
			nativeInt = ni;
		}

		/** The native int. */
		final int nativeInt;
	}

	/**
	 * Controls how the image should be resized or moved to match the size of this ImageView.
	 * 
	 * @param scaleType
	 *           The desired scaling mode.
	 * 
	 * @attr ref android.R.styleable#ImageView_scaleType
	 */
	public void setScaleType( ScaleType scaleType ) {
		if ( scaleType == null ) {
			throw new NullPointerException();
		}

		if ( mScaleType != scaleType ) {
			mScaleType = scaleType;

			setWillNotCacheDrawing( mScaleType == ScaleType.CENTER );

			requestLayout();
			invalidate();
		}
	}

	/**
	 * Return the current scale type in use by this ImageView.
	 * 
	 * @return the scale type
	 * @see ImageView.ScaleType
	 * @attr ref android.R.styleable#ImageView_scaleType
	 */
	public ScaleType getScaleType() {
		return mScaleType;
	}

	/**
	 * Return the view's optional matrix. This is applied to the view's drawable when it is drawn. If there is not matrix, this
	 * method will return null. Do not change this matrix in place. If you want a different matrix applied to the drawable, be sure
	 * to call setImageMatrix().
	 * 
	 * @return the image matrix
	 */
	public Matrix getImageMatrix() {
		return mMatrix;
	}

	/**
	 * Sets the image matrix.
	 * 
	 * @param matrix
	 *           the new image matrix
	 */
	public void setImageMatrix( Matrix matrix ) {
		// collaps null and identity to just null
		if ( matrix != null && matrix.isIdentity() ) {
			matrix = null;
		}

		// don't invalidate unless we're actually changing our matrix
		if ( matrix == null && !mMatrix.isIdentity() || matrix != null && !mMatrix.equals( matrix ) ) {
			mMatrix.set( matrix );
			configureBounds();
			invalidate();
		}
	}

	/**
	 * Resolve uri.
	 */
	private void resolveUri() {
		if ( mDrawable != null ) {
			return;
		}

		Resources rsrc = getResources();
		if ( rsrc == null ) {
			return;
		}

		Drawable d = null;

		if ( mResource != 0 ) {
			try {
				d = rsrc.getDrawable( mResource );
			} catch ( Exception e ) {
				Log.w( LOG_TAG, "Unable to find resource: " + mResource, e );
				// Don't try again.
				mUri = null;
			}
		} else if ( mUri != null ) {
			String scheme = mUri.getScheme();
			if ( ContentResolver.SCHEME_ANDROID_RESOURCE.equals( scheme ) ) {

			} else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) || ContentResolver.SCHEME_FILE.equals( scheme ) ) {
				try {
					d = Drawable.createFromStream( getContext().getContentResolver().openInputStream( mUri ), null );
				} catch ( Exception e ) {
					Log.w( LOG_TAG, "Unable to open content: " + mUri, e );
				}
			} else {
				d = Drawable.createFromPath( mUri.toString() );
			}

			if ( d == null ) {
				System.out.println( "resolveUri failed on bad bitmap uri: " + mUri );
				// Don't try again.
				mUri = null;
			}
		} else {
			return;
		}

		updateDrawable( d );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onCreateDrawableState(int)
	 */
	@Override
	public int[] onCreateDrawableState( int extraSpace ) {
		if ( mState == null ) {
			return super.onCreateDrawableState( extraSpace );
		} else if ( !mMergeState ) {
			return mState;
		} else {
			return mergeDrawableStates( super.onCreateDrawableState( extraSpace + mState.length ), mState );
		}
	}

	/**
	 * Update drawable.
	 * 
	 * @param d
	 *           the d
	 */
	private void updateDrawable( Drawable d ) {
		if ( mDrawable != null ) {
			mDrawable.setCallback( null );
			unscheduleDrawable( mDrawable );
		}
		mDrawable = d;
		if ( d != null ) {
			d.setCallback( this );
			if ( d.isStateful() ) {
				d.setState( getDrawableState() );
			}
			d.setLevel( mLevel );
			mDrawableWidth = d.getIntrinsicWidth();
			mDrawableHeight = d.getIntrinsicHeight();
			applyColorMod();
			configureBounds();
		} else {
			mDrawableWidth = mDrawableHeight = -1;
		}
	}

	/**
	 * Resize from drawable.
	 */
	private void resizeFromDrawable() {
		Drawable d = mDrawable;
		if ( d != null ) {
			int w = d.getIntrinsicWidth();
			if ( w < 0 ) w = mDrawableWidth;
			int h = d.getIntrinsicHeight();
			if ( h < 0 ) h = mDrawableHeight;
			if ( w != mDrawableWidth || h != mDrawableHeight ) {
				mDrawableWidth = w;
				mDrawableHeight = h;
				requestLayout();
			}
		}
	}

	/** The Constant sS2FArray. */
	private static final Matrix.ScaleToFit[] sS2FArray = {
		Matrix.ScaleToFit.FILL, Matrix.ScaleToFit.START, Matrix.ScaleToFit.CENTER, Matrix.ScaleToFit.END };

	/**
	 * Scale type to scale to fit.
	 * 
	 * @param st
	 *           the st
	 * @return the matrix. scale to fit
	 */
	private static Matrix.ScaleToFit scaleTypeToScaleToFit( ScaleType st ) {
		// ScaleToFit enum to their corresponding Matrix.ScaleToFit values
		return sS2FArray[st.nativeInt - 1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout( boolean changed, int left, int top, int right, int bottom ) {
		super.onLayout( changed, left, top, right, bottom );

		if ( changed ) {
			mHaveFrame = true;

			double oldRotation = mRotation;
			boolean flip_h = getHorizontalFlip();
			boolean flip_v = getVerticalFlip();

			configureBounds();

			if ( flip_h || flip_v ) {
				flip( flip_h, flip_v );
			}

			if ( oldRotation != 0 ) {
				setImageRotation( oldRotation, false );
				mRotation = oldRotation;
			}
			invalidate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
		resolveUri();
		int w;
		int h;

		// Desired aspect ratio of the view's contents (not including padding)
		float desiredAspect = 0.0f;

		// We are allowed to change the view's width
		boolean resizeWidth = false;

		// We are allowed to change the view's height
		boolean resizeHeight = false;

		final int widthSpecMode = MeasureSpec.getMode( widthMeasureSpec );
		final int heightSpecMode = MeasureSpec.getMode( heightMeasureSpec );

		if ( mDrawable == null ) {
			// If no drawable, its intrinsic size is 0.
			mDrawableWidth = -1;
			mDrawableHeight = -1;
			w = h = 0;
		} else {
			w = mDrawableWidth;
			h = mDrawableHeight;

			if ( w <= 0 ) w = 1;
			if ( h <= 0 ) h = 1;

			if ( mDrawableHeight > mDrawableWidth ) {
				portrait = true;
			}

			orientation = getResources().getConfiguration().orientation;

			// We are supposed to adjust view bounds to match the aspect
			// ratio of our drawable. See if that is possible.
			if ( mAdjustViewBounds ) {
				resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
				resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;

				desiredAspect = (float) w / (float) h;
			}
		}

		int pleft = getPaddingLeft();
		int pright = getPaddingRight();
		int ptop = getPaddingTop();
		int pbottom = getPaddingBottom();

		int widthSize;
		int heightSize;

		if ( resizeWidth || resizeHeight ) {
			/*
			 * If we get here, it means we want to resize to match the drawables aspect ratio, and we have the freedom to change at
			 * least one dimension.
			 */

			// Get the max possible width given our constraints
			widthSize = resolveAdjustedSize( w + pleft + pright, mMaxWidth, widthMeasureSpec );

			// Get the max possible height given our constraints
			heightSize = resolveAdjustedSize( h + ptop + pbottom, mMaxHeight, heightMeasureSpec );

			if ( desiredAspect != 0.0f ) {
				// See what our actual aspect ratio is
				float actualAspect = (float) ( widthSize - pleft - pright ) / ( heightSize - ptop - pbottom );

				if ( Math.abs( actualAspect - desiredAspect ) > 0.0000001 ) {

					boolean done = false;

					// Try adjusting width to be proportional to height
					if ( resizeWidth ) {
						int newWidth = (int) ( desiredAspect * ( heightSize - ptop - pbottom ) ) + pleft + pright;
						if ( newWidth <= widthSize ) {
							widthSize = newWidth;
							done = true;
						}
					}

					// Try adjusting height to be proportional to width
					if ( !done && resizeHeight ) {
						int newHeight = (int) ( ( widthSize - pleft - pright ) / desiredAspect ) + ptop + pbottom;
						if ( newHeight <= heightSize ) {
							heightSize = newHeight;
						}
					}
				}
			}
		} else {
			/*
			 * We are either don't want to preserve the drawables aspect ratio, or we are not allowed to change view dimensions. Just
			 * measure in the normal way.
			 */
			w += pleft + pright;
			h += ptop + pbottom;

			w = Math.max( w, getSuggestedMinimumWidth() );
			h = Math.max( h, getSuggestedMinimumHeight() );

			widthSize = resolveSize( w, widthMeasureSpec );
			heightSize = resolveSize( h, heightMeasureSpec );
		}

		setMeasuredDimension( widthSize, heightSize );

		// drawResource();
	}

	/**
	 * Resolve adjusted size.
	 * 
	 * @param desiredSize
	 *           the desired size
	 * @param maxSize
	 *           the max size
	 * @param measureSpec
	 *           the measure spec
	 * @return the int
	 */
	private int resolveAdjustedSize( int desiredSize, int maxSize, int measureSpec ) {
		int result = desiredSize;
		int specMode = MeasureSpec.getMode( measureSpec );
		int specSize = MeasureSpec.getSize( measureSpec );
		switch ( specMode ) {
			case MeasureSpec.UNSPECIFIED:
				/*
				 * Parent says we can be as big as we want. Just don't be larger than max size imposed on ourselves.
				 */
				result = Math.min( desiredSize, maxSize );
				break;
			case MeasureSpec.AT_MOST:
				// Parent says we can be as big as we want, up to specSize.
				// Don't be larger than specSize, and don't be larger than
				// the max size imposed on ourselves.
				result = Math.min( Math.min( desiredSize, specSize ), maxSize );
				break;
			case MeasureSpec.EXACTLY:
				// No choice. Do what we are told.
				result = specSize;
				break;
		}
		return result;
	}

	/**
	 * Configure bounds.
	 */
	private void configureBounds() {
		if ( mDrawable == null || !mHaveFrame ) {
			return;
		}

		int dwidth = mDrawableWidth;
		int dheight = mDrawableHeight;

		int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
		int vheight = getHeight() - getPaddingTop() - getPaddingBottom();

		boolean fits = ( dwidth < 0 || vwidth == dwidth ) && ( dheight < 0 || vheight == dheight );

		if ( dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == mScaleType ) {
			/*
			 * If the drawable has no intrinsic size, or we're told to scaletofit, then we just fill our entire view.
			 */
			mDrawable.setBounds( 0, 0, vwidth, vheight );
			mDrawMatrix = null;
		} else {
			// We need to do the scaling ourself, so have the drawable
			// use its native size.
			mDrawable.setBounds( 0, 0, dwidth, dheight );

			if ( ScaleType.MATRIX == mScaleType ) {
				// Use the specified matrix as-is.
				if ( mMatrix.isIdentity() ) {
					mDrawMatrix = null;
				} else {
					mDrawMatrix = mMatrix;
				}
			} else if ( fits ) {
				// The bitmap fits exactly, no transform needed.
				mDrawMatrix = null;
			} else if ( ScaleType.CENTER == mScaleType ) {
				// Center bitmap in view, no scaling.
				mDrawMatrix = mMatrix;
				mDrawMatrix.setTranslate( (int) ( ( vwidth - dwidth ) * 0.5f + 0.5f ), (int) ( ( vheight - dheight ) * 0.5f + 0.5f ) );
			} else if ( ScaleType.CENTER_CROP == mScaleType ) {
				mDrawMatrix = mMatrix;

				float scale;
				float dx = 0, dy = 0;

				if ( dwidth * vheight > vwidth * dheight ) {
					scale = (float) vheight / (float) dheight;
					dx = ( vwidth - dwidth * scale ) * 0.5f;
				} else {
					scale = (float) vwidth / (float) dwidth;
					dy = ( vheight - dheight * scale ) * 0.5f;
				}

				mDrawMatrix.setScale( scale, scale );
				mDrawMatrix.postTranslate( (int) ( dx + 0.5f ), (int) ( dy + 0.5f ) );
			} else if ( ScaleType.CENTER_INSIDE == mScaleType ) {
				mDrawMatrix = mMatrix;
				float scale;
				float dx;
				float dy;

				if ( dwidth <= vwidth && dheight <= vheight ) {
					scale = 1.0f;
				} else {
					scale = Math.min( (float) vwidth / (float) dwidth, (float) vheight / (float) dheight );
				}

				dx = (int) ( ( vwidth - dwidth * scale ) * 0.5f + 0.5f );
				dy = (int) ( ( vheight - dheight * scale ) * 0.5f + 0.5f );

				mDrawMatrix.setScale( scale, scale );
				mDrawMatrix.postTranslate( dx, dy );
			} else {
				// Generate the required transform.
				mTempSrc.set( 0, 0, dwidth, dheight );
				mTempDst.set( 0, 0, vwidth, vheight );

				mDrawMatrix = mMatrix;
				mDrawMatrix.setRectToRect( mTempSrc, mTempDst, scaleTypeToScaleToFit( mScaleType ) );

				mCurrentScale = getMatrixScale( mDrawMatrix )[0];

				Matrix tempMatrix = new Matrix( mMatrix );
				RectF src = new RectF();
				RectF dst = new RectF();
				src.set( 0, 0, dheight, dwidth );
				dst.set( 0, 0, vwidth, vheight );
				tempMatrix.setRectToRect( src, dst, scaleTypeToScaleToFit( mScaleType ) );

				tempMatrix = new Matrix( mDrawMatrix );
				tempMatrix.invert( tempMatrix );

				float invertScale = getMatrixScale( tempMatrix )[0];

				mDrawMatrix.postScale( invertScale, invertScale, vwidth / 2, vheight / 2 );

				mRotateMatrix.reset();
				mStraightenMatrix.reset();
				mFlipMatrix.reset();
				mFlipType = FlipType.FLIP_NONE.nativeInt;
				mRotation = 0;
				mRotateMatrix.postScale( mCurrentScale, mCurrentScale, vwidth / 2, vheight / 2 );
				// mStraightenMatrix.postScale( mCurrentScale, mCurrentScale, vwidth / 2, vheight / 2 );
				mDrawRect = getImageRect();
				mCenter = getCenter();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#drawableStateChanged()
	 */
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		Drawable d = mDrawable;
		if ( d != null && d.isStateful() ) {
			d.setState( getDrawableState() );
		}
	}

	@Override
	protected void onDraw( Canvas canvas ) {
		super.onDraw( canvas );

		if ( mDrawable == null ) {
			return; // couldn't resolve the URI
		}

		if ( mDrawableWidth == 0 || mDrawableHeight == 0 ) {
			return; // nothing to draw (empty bounds)
		}

		final int mPaddingTop = getPaddingTop();
		final int mPaddingLeft = getPaddingLeft();
		final int mPaddingBottom = getPaddingBottom();
		final int mPaddingRight = getPaddingRight();

		if ( mDrawMatrix == null && mPaddingTop == 0 && mPaddingLeft == 0 ) {
			mDrawable.draw( canvas );
		} else {
			int saveCount = canvas.getSaveCount();
			canvas.save();

			if ( mCropToPadding ) {
				final int scrollX = getScrollX();
				final int scrollY = getScrollY();
				canvas.clipRect( scrollX + mPaddingLeft, scrollY + mPaddingTop, scrollX + getRight() - getLeft() - mPaddingRight,
						scrollY + getBottom() - getTop() - mPaddingBottom );
			}

			canvas.translate( mPaddingLeft, mPaddingTop );

			if ( mFlipMatrix != null ) canvas.concat( mFlipMatrix );
			if ( mRotateMatrix != null ) canvas.concat( mRotateMatrix );
			//
			if ( mStraightenMatrix != null ) canvas.concat( mStraightenMatrix );
			if ( mDrawMatrix != null ) canvas.concat( mDrawMatrix );

			mDrawable.draw( canvas );

			canvas.restoreToCount( saveCount );

			if ( mEnableFreeRotate ) {

				mDrawRect = getImageRect();
				getDrawingRect( mViewDrawRect );

				mClipPath.reset();
				mInversePath.reset();
				mLinesPath.reset();

				float[] points = new float[] {
					mDrawRect.left, mDrawRect.top, mDrawRect.right, mDrawRect.top, mDrawRect.right, mDrawRect.bottom, mDrawRect.left,
					mDrawRect.bottom };

				mTempMatrix.set( mDrawMatrix );
				mTempMatrix.postConcat( mRotateMatrix );
				mTempMatrix.postConcat( mStraightenMatrix );
				mTempMatrix.mapPoints( points );

				mViewInvertRect.set( mViewDrawRect );
				mViewInvertRect.top -= mPaddingLeft;
				mViewInvertRect.left -= mPaddingTop;

				mInversePath.addRect( mViewInvertRect, Path.Direction.CW );

				double sx = Point2D.distance( points[2], points[3], points[0], points[1] );
				double sy = Point2D.distance( points[6], points[7], points[0], points[1] );
				double angle = getAngle90( mRotation );
				RectF rect;
				if ( initStraighten ) {

					if ( angle < 45 ) {
						rect = crop( (float) sx, (float) sy, angle, mDrawableWidth, mDrawableHeight, mCenter, null );
					} else {
						rect = crop( (float) sx, (float) sy, angle, mDrawableHeight, mDrawableWidth, mCenter, null );
					}

					float colStep = (float) rect.height() / grid_cols;
					float rowStep = (float) rect.width() / grid_rows;

					for ( int i = 1; i < grid_cols; i++ ) {
						// mLinesPath.addRect( (int)rect.left, (int)(rect.top + colStep * i), (int)rect.right, (int)(rect.top + colStep *
						// i) + 3, Path.Direction.CW );
						mLinesPath.moveTo( (int) rect.left, (int) ( rect.top + colStep * i ) );
						mLinesPath.lineTo( (int) rect.right, (int) ( rect.top + colStep * i ) );
					}

					for ( int i = 1; i < grid_rows; i++ ) {
						// mLinesPath.addRect( (int)(rect.left + rowStep * i), (int)rect.top, (int)(rect.left + rowStep * i) + 3,
						// (int)rect.bottom, Path.Direction.CW );
						mLinesPath.moveTo( (int) ( rect.left + rowStep * i ), (int) rect.top );
						mLinesPath.lineTo( (int) ( rect.left + rowStep * i ), (int) rect.bottom );
					}
					imageCaptureRegion = rect;
					PointF center = getCenter();
					mStraightenDrawable.setBounds( (int) ( center.x - handleWidth ), (int) ( imageCaptureRegion.bottom - handleHeight ),
							(int) ( center.x + handleWidth ), (int) ( imageCaptureRegion.bottom + handleHeight ) );
					mPosX = center.x;
					initStraighten = false;
				} else {
					rect = imageCaptureRegion;
					float colStep = (float) rect.height() / grid_cols;
					float rowStep = (float) rect.width() / grid_rows;

					for ( int i = 1; i < grid_cols; i++ ) {
						// mLinesPath.addRect( (int)rect.left, (int)(rect.top + colStep * i), (int)rect.right, (int)(rect.top + colStep *
						// i) + 3, Path.Direction.CW );
						mLinesPath.moveTo( (int) rect.left, (int) ( rect.top + colStep * i ) );
						mLinesPath.lineTo( (int) rect.right, (int) ( rect.top + colStep * i ) );
					}

					for ( int i = 1; i < grid_rows; i++ ) {
						// mLinesPath.addRect( (int)(rect.left + rowStep * i), (int)rect.top, (int)(rect.left + rowStep * i) + 3,
						// (int)rect.bottom, Path.Direction.CW );
						mLinesPath.moveTo( (int) ( rect.left + rowStep * i ), (int) rect.top );
						mLinesPath.lineTo( (int) ( rect.left + rowStep * i ), (int) rect.bottom );
					}

				}

				mClipPath.addRect( rect, Path.Direction.CW );

				mInversePath.addRect( rect, Path.Direction.CCW );

				saveCount = canvas.save();
				canvas.translate( mPaddingLeft, mPaddingTop );

				canvas.drawPath( mInversePath, mOutlineFill );

				// canvas.drawPath( mLinesPath, mLinesPaintShadow );
				canvas.drawPath( mLinesPath, mLinesPaint );

				canvas.drawPath( mClipPath, mOutlinePaint );

				// if ( mFlipMatrix != null ) canvas.concat( mFlipMatrix );
				// if ( mRotateMatrix != null ) canvas.concat( mRotateMatrix );
				// if ( mDrawMatrix != null ) canvas.concat( mDrawMatrix );

				// mResizeDrawable.setBounds( (int) mDrawRect.right - handleWidth, (int) mDrawRect.bottom - handleHeight, (int)
				// mDrawRect.right + handleWidth, (int) mDrawRect.bottom + handleHeight );
				// mResizeDrawable.draw( canvas );

				canvas.restoreToCount( saveCount );

				saveCount = canvas.save();
				canvas.translate( mPaddingLeft, mPaddingTop );
				// mResizeDrawable.setBounds( (int)(points[4] - handleWidth), (int)(points[5] - handleHeight), (int)(points[4] +
				// handleWidth), (int)(points[5] + handleHeight) );
				// mResizeDrawable.draw( canvas );

				mStraightenDrawable.draw( canvas );

				canvas.restoreToCount( saveCount );

			}
			/**
			 * if(intersectPoints){ //intersectPaint.setColor(Color.YELLOW); //canvas.drawCircle(intersectx, intersecty, 10,
			 * intersectPaint); //canvas.drawLine(trueRect.left + getPaddingLeft(), trueRect.bottom + getPaddingBottom(),
			 * trueRect.right + getPaddingRight(), trueRect.bottom+ getPaddingBottom(), intersectPaint); //canvas.drawRect(testRect,
			 * intersectPaint); }
			 */

		}
	}

	float ax = 0;
	float ay = 0;
	float bx = 0;
	float by = 0;
	float cx = 0;
	float cy = 0;
	float dx = 0;
	float dy = 0;

	float intersectx = 0;
	float intersecty = 0;

	Paint intersectPaint = new Paint();
	RectF imageCaptureRegion = null;
	boolean initStraighten = true;
	Matrix rotateCopy;
	boolean firstDraw = true;
	Handler mFadeHandler = new Handler();
	boolean mFadeHandlerStarted;

	public void setInitStraighten( boolean value ) {
		initStraighten = value;
	}

	protected void fadeinGrid( final int durationMs ) {

		final long startTime = System.currentTimeMillis();
		final float startAlpha = mLinesPaint.getAlpha();
		final float startAlphaShadow = mLinesPaintShadow.getAlpha();
		final Linear easing = new Linear();

		mFadeHandler.post( new Runnable() {

			@Override
			public void run() {
				long now = System.currentTimeMillis();

				float currentMs = Math.min( durationMs, now - startTime );
				float new_alpha_lines = (float) easing.easeNone( currentMs, startAlpha, mLinesAlpha, durationMs );
				float new_alpha_lines_shadow = (float) easing.easeNone( currentMs, startAlphaShadow, mLinesShadowAlpha, durationMs );

				mLinesPaint.setAlpha( (int) new_alpha_lines );
				mLinesPaintShadow.setAlpha( (int) new_alpha_lines_shadow );
				invalidate();

				if ( currentMs < durationMs ) {
					mFadeHandler.post( this );
				} else {
					mLinesPaint.setAlpha( mLinesAlpha );
					mLinesPaintShadow.setAlpha( mLinesShadowAlpha );
					invalidate();
				}
			}
		} );
	}

	protected void fadeoutGrid( final int durationMs ) {

		final long startTime = System.currentTimeMillis();
		final float startAlpha = mLinesPaint.getAlpha();
		final float startAlphaShadow = mLinesPaintShadow.getAlpha();
		final Linear easing = new Linear();

		mFadeHandler.post( new Runnable() {

			@Override
			public void run() {
				long now = System.currentTimeMillis();

				float currentMs = Math.min( durationMs, now - startTime );

				float new_alpha_lines = (float) easing.easeNone( currentMs, 0, startAlpha, durationMs );
				float new_alpha_lines_shadow = (float) easing.easeNone( currentMs, 0, startAlphaShadow, durationMs );

				mLinesPaint.setAlpha( (int) startAlpha - (int) new_alpha_lines );
				mLinesPaintShadow.setAlpha( (int) startAlphaShadow - (int) new_alpha_lines_shadow );
				invalidate();

				if ( currentMs < durationMs ) {
					mFadeHandler.post( this );
				} else {
					mLinesPaint.setAlpha( 0 );
					mLinesPaintShadow.setAlpha( 0 );
					invalidate();
				}
			}
		} );
	}

	protected void fadeinOutlines( final int durationMs ) {
		if ( mFadeHandlerStarted ) return;
		mFadeHandlerStarted = true;

		final long startTime = System.currentTimeMillis();
		final Linear easing = new Linear();

		mFadeHandler.post( new Runnable() {

			@Override
			public void run() {
				long now = System.currentTimeMillis();

				float currentMs = Math.min( durationMs, now - startTime );

				float new_alpha_fill = (float) easing.easeNone( currentMs, 0, mOutlineFillAlpha, durationMs );
				float new_alpha_paint = (float) easing.easeNone( currentMs, 0, mOutlinePaintAlpha, durationMs );
				float new_alpha_lines = (float) easing.easeNone( currentMs, 0, mLinesAlpha, durationMs );
				float new_alpha_lines_shadow = (float) easing.easeNone( currentMs, 0, mLinesShadowAlpha, durationMs );

				mOutlineFill.setAlpha( (int) new_alpha_fill );
				mOutlinePaint.setAlpha( (int) new_alpha_paint );
				mLinesPaint.setAlpha( (int) new_alpha_lines );
				mLinesPaintShadow.setAlpha( (int) new_alpha_lines_shadow );
				invalidate();

				if ( currentMs < durationMs ) {
					mFadeHandler.post( this );
				} else {

					mOutlineFill.setAlpha( mOutlineFillAlpha );
					mOutlinePaint.setAlpha( mOutlinePaintAlpha );
					mLinesPaint.setAlpha( mLinesAlpha );
					mLinesPaintShadow.setAlpha( mLinesShadowAlpha );
					invalidate();
				}
			}
		} );
	}

	protected void fadeoutOutlines( final int durationMs ) {

		final long startTime = System.currentTimeMillis();
		final Linear easing = new Linear();

		final int alpha1 = mOutlineFill.getAlpha();
		final int alpha2 = mOutlinePaint.getAlpha();
		final int alpha3 = mLinesPaint.getAlpha();
		final int alpha4 = mLinesPaintShadow.getAlpha();

		mFadeHandler.post( new Runnable() {

			@Override
			public void run() {
				long now = System.currentTimeMillis();

				float currentMs = Math.min( durationMs, now - startTime );

				float new_alpha_fill = (float) easing.easeNone( currentMs, alpha1, 0, durationMs );
				float new_alpha_paint = (float) easing.easeNone( currentMs, alpha2, 0, durationMs );
				float new_alpha_lines = (float) easing.easeNone( currentMs, alpha3, 0, durationMs );
				float new_alpha_lines_shadow = (float) easing.easeNone( currentMs, alpha4, 0, durationMs );

				mOutlineFill.setAlpha( (int) new_alpha_fill );
				mOutlinePaint.setAlpha( (int) new_alpha_paint );
				mLinesPaint.setAlpha( (int) new_alpha_lines );
				mLinesPaintShadow.setAlpha( (int) new_alpha_lines_shadow );
				invalidate();

				if ( currentMs < durationMs ) {
					mFadeHandler.post( this );
				} else {
					hideOutlines();
				}
			}
		} );
	}

	protected void hideOutlines() {
		mFadeHandlerStarted = false;
		mOutlineFill.setAlpha( 0 );
		mOutlinePaint.setAlpha( 0 );
		mLinesPaint.setAlpha( 0 );
		mLinesPaintShadow.setAlpha( 0 );
		invalidate();
	}

	static double getAngle90( double value ) {

		double rotation = Point2D.angle360( value );
		double angle = rotation;

		if ( rotation >= 270 ) {
			angle = 360 - rotation;
		} else if ( rotation >= 180 ) {
			angle = rotation - 180;
		} else if ( rotation > 90 ) {
			angle = 180 - rotation;
		}
		return angle;
	}

	RectF crop( float originalWidth, float originalHeight, double angle, float targetWidth, float targetHeight, PointF center,
			Canvas canvas ) {
		double radians = Point2D.radians( angle );

		PointF[] original = new PointF[] {
			new PointF( 0, 0 ), new PointF( originalWidth, 0 ), new PointF( originalWidth, originalHeight ),
			new PointF( 0, originalHeight ) };

		Point2D.translate( original, -originalWidth / 2, -originalHeight / 2 );

		PointF[] rotated = new PointF[original.length];
		System.arraycopy( original, 0, rotated, 0, original.length );
		Point2D.rotate( rotated, radians );

		if ( angle >= 0 ) {
			PointF[] ray = new PointF[] { new PointF( 0, 0 ), new PointF( -targetWidth / 2, -targetHeight / 2 ) };
			PointF[] bound = new PointF[] { rotated[0], rotated[3] };

			// Top Left intersection.
			PointF intersectTL = Point2D.intersection( ray, bound );

			PointF[] ray2 = new PointF[] { new PointF( 0, 0 ), new PointF( targetWidth / 2, -targetHeight / 2 ) };
			PointF[] bound2 = new PointF[] { rotated[0], rotated[1] };

			// Top Right intersection.
			PointF intersectTR = Point2D.intersection( ray2, bound2 );

			// Pick the intersection closest to the origin
			PointF intersect = new PointF( Math.max( intersectTL.x, -intersectTR.x ), Math.max( intersectTL.y, intersectTR.y ) );

			RectF newRect = new RectF( intersect.x, intersect.y, -intersect.x, -intersect.y );
			newRect.offset( center.x, center.y );

			if ( canvas != null ) { // debug

				Point2D.translate( rotated, center.x, center.y );
				Point2D.translate( ray, center.x, center.y );
				Point2D.translate( ray2, center.x, center.y );

				Paint paint = new Paint( Paint.ANTI_ALIAS_FLAG );
				paint.setColor( 0x66FFFF00 );
				paint.setStyle( Paint.Style.STROKE );
				paint.setStrokeWidth( 2 );
				// draw rotated
				drawRect( rotated, canvas, paint );

				paint.setColor( Color.GREEN );
				drawLine( ray, canvas, paint );

				paint.setColor( Color.BLUE );
				drawLine( ray2, canvas, paint );

				paint.setColor( Color.CYAN );
				drawLine( bound, canvas, paint );

				paint.setColor( Color.WHITE );
				drawLine( bound2, canvas, paint );

				paint.setColor( Color.GRAY );
				canvas.drawRect( newRect, paint );
			}
			return newRect;

		} else {
			throw new IllegalArgumentException( "angle cannot be < 0" );
		}
	}

	void drawLine( PointF[] line, Canvas canvas, Paint paint ) {
		canvas.drawLine( line[0].x, line[0].y, line[1].x, line[1].y, paint );
	}

	void drawRect( PointF[] rect, Canvas canvas, Paint paint ) {
		// draw rotated
		Path path = new Path();
		path.moveTo( rect[0].x, rect[0].y );
		path.lineTo( rect[1].x, rect[1].y );
		path.lineTo( rect[2].x, rect[2].y );
		path.lineTo( rect[3].x, rect[3].y );
		path.lineTo( rect[0].x, rect[0].y );
		canvas.drawPath( path, paint );
	}

	/**
	 * <p>
	 * Return the offset of the widget's text baseline from the widget's top boundary.
	 * </p>
	 * 
	 * @return the offset of the baseline within the widget's bounds or -1 if baseline alignment is not supported.
	 */
	@Override
	public int getBaseline() {
		if ( mBaselineAlignBottom ) {
			return getMeasuredHeight();
		} else {
			return mBaseline;
		}
	}

	/**
	 * <p>
	 * Set the offset of the widget's text baseline from the widget's top boundary. This value is overridden by the
	 * 
	 * @param baseline
	 *           The baseline to use, or -1 if none is to be provided. {@link #setBaselineAlignBottom(boolean)} property.
	 *           </p>
	 * @see #setBaseline(int)
	 * @attr ref android.R.styleable#ImageView_baseline
	 */
	public void setBaseline( int baseline ) {
		if ( mBaseline != baseline ) {
			mBaseline = baseline;
			requestLayout();
		}
	}

	/**
	 * Set whether to set the baseline of this view to the bottom of the view. Setting this value overrides any calls to setBaseline.
	 * 
	 * @param aligned
	 *           If true, the image view will be baseline aligned with based on its bottom edge.
	 * 
	 * @attr ref android.R.styleable#ImageView_baselineAlignBottom
	 */
	public void setBaselineAlignBottom( boolean aligned ) {
		if ( mBaselineAlignBottom != aligned ) {
			mBaselineAlignBottom = aligned;
			requestLayout();
		}
	}

	/**
	 * Return whether this view's baseline will be considered the bottom of the view.
	 * 
	 * @return the baseline align bottom
	 * @see #setBaselineAlignBottom(boolean)
	 */
	public boolean getBaselineAlignBottom() {
		return mBaselineAlignBottom;
	}

	/**
	 * Set a tinting option for the image.
	 * 
	 * @param color
	 *           Color tint to apply.
	 * @param mode
	 *           How to apply the color. The standard mode is {@link PorterDuff.Mode#SRC_ATOP}
	 * 
	 * @attr ref android.R.styleable#ImageView_tint
	 */
	public final void setColorFilter( int color, PorterDuff.Mode mode ) {
		setColorFilter( new PorterDuffColorFilter( color, mode ) );
	}

	/**
	 * Set a tinting option for the image. Assumes {@link PorterDuff.Mode#SRC_ATOP} blending mode.
	 * 
	 * @param color
	 *           Color tint to apply.
	 * @attr ref android.R.styleable#ImageView_tint
	 */
	public final void setColorFilter( int color ) {
		setColorFilter( color, PorterDuff.Mode.SRC_ATOP );
	}

	/**
	 * Clear color filter.
	 */
	public final void clearColorFilter() {
		setColorFilter( null );
	}

	/**
	 * Apply an arbitrary colorfilter to the image.
	 * 
	 * @param cf
	 *           the colorfilter to apply (may be null)
	 */
	public void setColorFilter( ColorFilter cf ) {
		if ( mColorFilter != cf ) {
			mColorFilter = cf;
			mColorMod = true;
			applyColorMod();
			invalidate();
		}
	}

	/**
	 * Sets the alpha.
	 * 
	 * @param alpha
	 *           the new alpha
	 */
	public void setAlpha( int alpha ) {
		alpha &= 0xFF; // keep it legal
		if ( mAlpha != alpha ) {
			mAlpha = alpha;
			mColorMod = true;
			applyColorMod();
			invalidate();
		}
	}

	/**
	 * Apply color mod.
	 */
	private void applyColorMod() {
		// Only mutate and apply when modifications have occurred. This should
		// not reset the mColorMod flag, since these filters need to be
		// re-applied if the Drawable is changed.
		if ( mDrawable != null && mColorMod ) {
			mDrawable = mDrawable.mutate();
			mDrawable.setColorFilter( mColorFilter );
			mDrawable.setAlpha( mAlpha * mViewAlphaScale >> 8 );
		}
	}

	/** The m handler. */
	protected Handler mHandler = new Handler();

	/** The m rotation. */
	protected double mRotation = 0;

	/** The m current scale. */
	protected float mCurrentScale = 0;

	/** The m running. */
	protected boolean mRunning = false;

	/**
	 * Rotate90.
	 * 
	 * @param cw
	 *           the cw
	 * @param durationMs
	 *           the duration ms
	 */
	public void rotate90( boolean cw, int durationMs ) {
		final double destRotation = ( cw ? 90 : -90 );
		rotateBy( destRotation, durationMs );
		hideOutlines();
		portrait = !portrait;
	}

	public boolean getStraightenStarted() {
		return straightenStarted;
	}

	/**
	 * Rotate to.
	 * 
	 * @param cw
	 *           the cw
	 * @param durationMs
	 *           the duration ms
	 */
	protected void rotateBy( final double deltaRotation, final int durationMs ) {

		if ( mRunning ) {
			return;
		}

		mRunning = true;
		final long startTime = System.currentTimeMillis();

		final double destRotation = mRotation + deltaRotation;
		final double srcRotation = mRotation;

		setImageRotation( mRotation, false );
		invalidate();

		mHandler.post( new Runnable() {

			@SuppressWarnings("unused")
			float old_scale = 0;
			@SuppressWarnings("unused")
			float old_rotation = 0;

			@Override
			public void run() {
				long now = System.currentTimeMillis();

				float currentMs = Math.min( durationMs, now - startTime );
				float new_rotation = (float) mEasing.easeInOut( currentMs, 0, deltaRotation, durationMs );

				mRotation = Point2D.angle360( srcRotation + new_rotation );
				setImageRotation( mRotation, false );

				old_rotation = new_rotation;
				initStraighten = true;
				invalidate();

				if ( currentMs < durationMs ) {
					mHandler.post( this );
				} else {
					mRotation = Point2D.angle360( destRotation );
					setImageRotation( mRotation, true );
					initStraighten = true;
					invalidate();
					printDetails();

					mRunning = false;

					if ( isReset ) {
						onReset();
					}
				}
			}
		} );

		if ( straightenStarted && !isReset ) {
			initStraighten = true;
			resetStraighten();
			invalidate();
		}
	}

	private void resetStraighten() {
		mStraightenMatrix.reset();
		straightenStarted = false;
		previousStraightenAngle = 0;
		prevGrowth = 1;
		prevGrowthAngle = 0;
		currentNewPosition = 0;
		testStraighten = true;
		currentGrowth = 0;
		previousAngle = 0;
	}

	/**
	 * Prints the details.
	 */
	public void printDetails() {
		Log.i( LOG_TAG, "details:" );
		Log.d( LOG_TAG, " flip horizontal: "
				+ ( ( mFlipType & FlipType.FLIP_HORIZONTAL.nativeInt ) == FlipType.FLIP_HORIZONTAL.nativeInt ) );
		Log.d( LOG_TAG, " flip vertical: " + ( ( mFlipType & FlipType.FLIP_VERTICAL.nativeInt ) == FlipType.FLIP_VERTICAL.nativeInt ) );
		Log.d( LOG_TAG, " rotation: " + mRotation );
		Log.d( LOG_TAG, "--------" );
	}

	/**
	 * Flip.
	 * 
	 * @param horizontal
	 *           the horizontal
	 * @param durationMs
	 *           the duration ms
	 */
	public void flip( boolean horizontal, int durationMs ) {
		flipTo( horizontal, durationMs );
		hideOutlines();
	}

	/** The m camera enabled. */
	private boolean mCameraEnabled;

	/**
	 * Sets the camera enabled.
	 * 
	 * @param value
	 *           the new camera enabled
	 */
	public void setCameraEnabled( final boolean value ) {
		if ( android.os.Build.VERSION.SDK_INT >= 14 && value )
			mCameraEnabled = value;
		else
			mCameraEnabled = false;
	}

	/**
	 * Flip to.
	 * 
	 * @param horizontal
	 *           the horizontal
	 * @param durationMs
	 *           the duration ms
	 */
	protected void flipTo( final boolean horizontal, final int durationMs ) {

		if ( mRunning ) {
			return;
		}

		mRunning = true;

		final long startTime = System.currentTimeMillis();
		final int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
		final int vheight = getHeight() - getPaddingTop() - getPaddingBottom();
		final float centerx = vwidth / 2;
		final float centery = vheight / 2;

		final Camera camera = new Camera();

		mHandler.post( new Runnable() {

			@Override
			public void run() {
				long now = System.currentTimeMillis();

				double currentMs = Math.min( durationMs, now - startTime );

				if ( mCameraEnabled ) {
					float degrees = (float) ( 0 + ( ( -180 - 0 ) * ( currentMs / durationMs ) ) );

					camera.save();
					if ( horizontal ) {
						camera.rotateY( degrees );
					} else {
						camera.rotateX( degrees );
					}
					camera.getMatrix( mFlipMatrix );
					camera.restore();
					mFlipMatrix.preTranslate( -centerx, -centery );
					mFlipMatrix.postTranslate( centerx, centery );
				} else {

					double new_scale = mEasing.easeInOut( currentMs, 1, -2, durationMs );
					if ( horizontal )
						mFlipMatrix.setScale( (float) new_scale, 1, centerx, centery );
					else
						mFlipMatrix.setScale( 1, (float) new_scale, centerx, centery );
				}

				invalidate();

				if ( currentMs < durationMs ) {
					mHandler.post( this );
				} else {

					if ( horizontal ) {
						mFlipType ^= FlipType.FLIP_HORIZONTAL.nativeInt;
						mDrawMatrix.postScale( -1, 1, centerx, centery );
					} else {
						mFlipType ^= FlipType.FLIP_VERTICAL.nativeInt;
						mDrawMatrix.postScale( 1, -1, centerx, centery );
					}

					mRotateMatrix.postRotate( (float) ( -mRotation * 2 ), centerx, centery );
					mRotation = Point2D.angle360( getRotationFromMatrix( mRotateMatrix ) );

					mFlipMatrix.reset();

					invalidate();
					printDetails();

					mRunning = false;

					if ( isReset ) {
						onReset();
					}
				}
			}
		} );

		if ( straightenStarted && !isReset ) {
			initStraighten = true;
			resetStraighten();
			invalidate();
		}
	}

	private void flip( boolean horizontal, boolean vertical ) {

		invalidate();
		PointF center = getCenter();

		if ( horizontal ) {
			mFlipType ^= FlipType.FLIP_HORIZONTAL.nativeInt;
			mDrawMatrix.postScale( -1, 1, center.x, center.y );
		}

		if ( vertical ) {
			mFlipType ^= FlipType.FLIP_VERTICAL.nativeInt;
			mDrawMatrix.postScale( 1, -1, center.x, center.y );
		}

		mRotateMatrix.postRotate( (float) ( -mRotation * 2 ), center.x, center.y );
		mRotation = Point2D.angle360( getRotationFromMatrix( mRotateMatrix ) );
		mFlipMatrix.reset();
	}

	/** The m matrix values. */
	protected final float[] mMatrixValues = new float[9];

	/**
	 * Gets the value.
	 * 
	 * @param matrix
	 *           the matrix
	 * @param whichValue
	 *           the which value
	 * @return the value
	 */
	protected float getValue( Matrix matrix, int whichValue ) {
		matrix.getValues( mMatrixValues );
		return mMatrixValues[whichValue];
	}

	/**
	 * Gets the matrix scale.
	 * 
	 * @param matrix
	 *           the matrix
	 * @return the matrix scale
	 */
	protected float[] getMatrixScale( Matrix matrix ) {
		float[] result = new float[2];
		result[0] = getValue( matrix, Matrix.MSCALE_X );
		result[1] = getValue( matrix, Matrix.MSCALE_Y );
		return result;
	}

	/** The m flip type. */
	protected int mFlipType = FlipType.FLIP_NONE.nativeInt;

	/**
	 * The Enum FlipType.
	 */
	public enum FlipType {

		/** The FLI p_ none. */
		FLIP_NONE( 1 << 0 ),
		/** The FLI p_ horizontal. */
		FLIP_HORIZONTAL( 1 << 1 ),
		/** The FLI p_ vertical. */
		FLIP_VERTICAL( 1 << 2 );

		/**
		 * Instantiates a new flip type.
		 * 
		 * @param ni
		 *           the ni
		 */
		FlipType( int ni ) {
			nativeInt = ni;
		}

		/** The native int. */
		public final int nativeInt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#getRotation()
	 */
	public float getRotation() {
		return (float) mRotation;
	}

	/**
	 * Gets the horizontal flip.
	 * 
	 * @return the horizontal flip
	 */
	public boolean getHorizontalFlip() {
		if ( mFlipType != FlipType.FLIP_NONE.nativeInt ) {
			return ( mFlipType & FlipType.FLIP_HORIZONTAL.nativeInt ) == FlipType.FLIP_HORIZONTAL.nativeInt;
		}
		return false;
	}

	/**
	 * Gets the vertical flip.
	 * 
	 * @return the vertical flip
	 */
	public boolean getVerticalFlip() {
		if ( mFlipType != FlipType.FLIP_NONE.nativeInt ) {
			return ( mFlipType & FlipType.FLIP_VERTICAL.nativeInt ) == FlipType.FLIP_VERTICAL.nativeInt;
		}
		return false;
	}

	/**
	 * Gets the flip type.
	 * 
	 * @return the flip type
	 */
	public int getFlipType() {
		return mFlipType;
	}

	/**
	 * Checks if is running.
	 * 
	 * @return true, if is running
	 */
	public boolean isRunning() {
		return mRunning;
	}

	/**
	 * Reset the image to the original state.
	 */
	public void reset() {
		isReset = true;
		onReset();
	}

	/**
	 * On reset.
	 */
	private void onReset() {
		if ( isReset ) {
			double rotation = (double) getRotation();
			double straightenRotation = getStraightenAngle();
			boolean resetStraighten = getStraightenStarted();
			straightenStarted = false;
			
			rotation = rotation%360;
			if( rotation > 180 ){
				rotation = rotation - 360;
			}
			
			final boolean hflip = getHorizontalFlip();
			final boolean vflip = getVerticalFlip();
			boolean handled = false;
			initStraighten = false;
			invalidate();

			if ( rotation != 0 || resetStraighten ) {
				if( resetStraighten ){
					straightenBy( -straightenRotation, resetAnimTime );
				} else {
					rotateBy( -rotation, resetAnimTime );
				}
				handled = true;
			}

			if ( hflip ) {
				flip( true, resetAnimTime );
				handled = true;
			}

			if ( vflip ) {
				flip( false, resetAnimTime );
				handled = true;
			}

			if ( !handled ) {
				fireOnResetComplete();
			}
		}
	}

	/**
	 * Fire on reset complete.
	 */
	private void fireOnResetComplete() {
		if ( mResetListener != null ) {
			mResetListener.onResetComplete();
		}
	}

	/**
	 *
	 */
	@Override
	protected void onConfigurationChanged( Configuration newConfig ) {
		// TODO
		// During straighten, we must bring it to the start if orientation is changed
		orientation = getResources().getConfiguration().orientation;
		initStraighten = true;
		invalidate();
		if ( straightenStarted ) {
			initStraighten = true;
			resetStraighten();
			invalidate();
		}
	}
}