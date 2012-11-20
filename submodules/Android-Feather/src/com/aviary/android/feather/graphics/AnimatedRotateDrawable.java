/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aviary.android.feather.graphics;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import com.aviary.android.feather.Constants;
import com.aviary.android.feather.library.utils.ReflectionUtils;
import com.aviary.android.feather.library.utils.ReflectionUtils.ReflectionException;

/**
 * Rotate Drawable.
 */
public class AnimatedRotateDrawable extends Drawable implements Drawable.Callback, Runnable, Animatable {

	private AnimatedRotateState mState;

	private boolean mMutated;

	private float mCurrentDegrees;

	private float mIncrement;

	private boolean mRunning;

	/**
	 * Instantiates a new animated rotate drawable.
	 */
	public AnimatedRotateDrawable() {
		this( null, null );
	}

	/**
	 * Instantiates a new animated rotate drawable.
	 * 
	 * @param res
	 *           the res
	 * @param resId
	 *           the res id
	 */
	public AnimatedRotateDrawable( Resources res, int resId ) {
		this( res, resId, 8, 100 );
	}

	public AnimatedRotateDrawable( Resources res, int resId, int frames, int duration ) {
		this( null, null );
		final float pivotX = 0.5f;
		final float pivotY = 0.5f;
		final boolean pivotXRel = true;
		final boolean pivotYRel = true;

		setFramesCount( frames );
		setFramesDuration( duration );

		Drawable drawable = null;
		if ( resId > 0 ) {
			drawable = res.getDrawable( resId );
		}

		final AnimatedRotateState rotateState = mState;
		rotateState.mDrawable = drawable;
		rotateState.mPivotXRel = pivotXRel;
		rotateState.mPivotX = pivotX;
		rotateState.mPivotYRel = pivotYRel;
		rotateState.mPivotY = pivotY;

		init();

		if ( drawable != null ) {
			drawable.setCallback( this );
		}
	}

	/**
	 * Instantiates a new animated rotate drawable.
	 * 
	 * @param rotateState
	 *           the rotate state
	 * @param res
	 *           the res
	 */
	private AnimatedRotateDrawable( AnimatedRotateState rotateState, Resources res ) {
		mState = new AnimatedRotateState( rotateState, this, res );
		init();
	}

	/**
	 * Initialize
	 */
	private void init() {
		final AnimatedRotateState state = mState;
		mIncrement = 360.0f / state.mFramesCount;
		final Drawable drawable = state.mDrawable;
		if ( drawable != null ) {
			drawable.setFilterBitmap( true );
			if ( drawable instanceof BitmapDrawable ) {
				( (BitmapDrawable) drawable ).setAntiAlias( true );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw( Canvas canvas ) {
		int saveCount = canvas.save();

		final AnimatedRotateState st = mState;
		final Drawable drawable = st.mDrawable;
		final Rect bounds = drawable.getBounds();

		int w = bounds.right - bounds.left;
		int h = bounds.bottom - bounds.top;

		float px = st.mPivotXRel ? ( w * st.mPivotX ) : st.mPivotX;
		float py = st.mPivotYRel ? ( h * st.mPivotY ) : st.mPivotY;

		canvas.rotate( mCurrentDegrees, px + bounds.left, py + bounds.top );

		drawable.draw( canvas );

		canvas.restoreToCount( saveCount );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Animatable#start()
	 */
	@Override
	public void start() {
		if ( !mRunning ) {
			mRunning = true;
			nextFrame();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Animatable#stop()
	 */
	@Override
	public void stop() {
		mRunning = false;
		unscheduleSelf( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Animatable#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return mRunning;
	}

	/**
	 * Next frame.
	 */
	private void nextFrame() {
		unscheduleSelf( this );
		scheduleSelf( this, SystemClock.uptimeMillis() + mState.mFrameDuration );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		mCurrentDegrees += mIncrement;
		if ( mCurrentDegrees > ( 360.0f - mIncrement ) ) {
			mCurrentDegrees = 0.0f;
		}
		invalidateSelf();
		nextFrame();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#setVisible(boolean, boolean)
	 */
	@Override
	public boolean setVisible( boolean visible, boolean restart ) {
		mState.mDrawable.setVisible( visible, restart );
		boolean changed = super.setVisible( visible, restart );
		if ( visible ) {
			if ( changed || restart ) {
				mCurrentDegrees = 0.0f;
				nextFrame();
			}
		} else {
			unscheduleSelf( this );
		}
		return changed;
	}

	/**
	 * Returns the drawable rotated by this RotateDrawable.
	 * 
	 * @return the drawable
	 */
	public Drawable getDrawable() {
		return mState.mDrawable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getChangingConfigurations()
	 */
	@Override
	public int getChangingConfigurations() {
		return super.getChangingConfigurations() | mState.mChangingConfigurations | mState.mDrawable.getChangingConfigurations();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#setAlpha(int)
	 */
	@Override
	public void setAlpha( int alpha ) {
		mState.mDrawable.setAlpha( alpha );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#setColorFilter(android.graphics.ColorFilter)
	 */
	@Override
	public void setColorFilter( ColorFilter cf ) {
		mState.mDrawable.setColorFilter( cf );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getOpacity()
	 */
	@Override
	public int getOpacity() {
		return mState.mDrawable.getOpacity();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable.Callback#invalidateDrawable(android.graphics.drawable.Drawable)
	 */
	@Override
	public void invalidateDrawable( Drawable who ) {
		if ( Constants.ANDROID_SDK > 10 ) {

			Callback callback;
			try {
				callback = (Callback) ReflectionUtils.invokeMethod( this, "getCallback" );
			} catch ( ReflectionException e ) {
				return;
			}
			if ( callback != null ) {
				callback.invalidateDrawable( this );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable.Callback#scheduleDrawable(android.graphics.drawable.Drawable, java.lang.Runnable,
	 * long)
	 */
	@Override
	public void scheduleDrawable( Drawable who, Runnable what, long when ) {
		if ( Constants.ANDROID_SDK > 10 ) {
			Callback callback;
			try {
				callback = (Callback) ReflectionUtils.invokeMethod( this, "getCallback" );
			} catch ( ReflectionException e ) {
				return;
			}
			if ( callback != null ) {
				callback.scheduleDrawable( this, what, when );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable.Callback#unscheduleDrawable(android.graphics.drawable.Drawable, java.lang.Runnable)
	 */

	@Override
	public void unscheduleDrawable( Drawable who, Runnable what ) {
		if ( Constants.ANDROID_SDK > 10 ) {
			Callback callback;
			try {
				callback = (Callback) ReflectionUtils.invokeMethod( this, "getCallback" );
			} catch ( ReflectionException e ) {
				return;
			}
			if ( callback != null ) {
				callback.unscheduleDrawable( this, what );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getPadding(android.graphics.Rect)
	 */
	@Override
	public boolean getPadding( Rect padding ) {
		return mState.mDrawable.getPadding( padding );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#isStateful()
	 */
	@Override
	public boolean isStateful() {
		return mState.mDrawable.isStateful();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#onBoundsChange(android.graphics.Rect)
	 */
	@Override
	protected void onBoundsChange( Rect bounds ) {
		mState.mDrawable.setBounds( bounds.left, bounds.top, bounds.right, bounds.bottom );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getIntrinsicWidth()
	 */
	@Override
	public int getIntrinsicWidth() {
		return mState.mDrawable.getIntrinsicWidth();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getIntrinsicHeight()
	 */
	@Override
	public int getIntrinsicHeight() {
		return mState.mDrawable.getIntrinsicHeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#getConstantState()
	 */
	@Override
	public ConstantState getConstantState() {
		if ( mState.canConstantState() ) {
			mState.mChangingConfigurations = getChangingConfigurations();
			return mState;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#inflate(android.content.res.Resources, org.xmlpull.v1.XmlPullParser,
	 * android.util.AttributeSet)
	 */
	@Override
	public void inflate( Resources r, XmlPullParser parser, AttributeSet attrs ) throws XmlPullParserException, IOException {}

	/**
	 * Sets the frames count.
	 * 
	 * @param framesCount
	 *           the new frames count
	 */
	public void setFramesCount( int framesCount ) {
		mState.mFramesCount = framesCount;
		mIncrement = 360.0f / mState.mFramesCount;
	}

	/**
	 * Sets the frames duration.
	 * 
	 * @param framesDuration
	 *           the new frames duration
	 */
	public void setFramesDuration( int framesDuration ) {
		mState.mFrameDuration = framesDuration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#mutate()
	 */
	@Override
	public Drawable mutate() {
		if ( !mMutated && super.mutate() == this ) {
			mState.mDrawable.mutate();
			mMutated = true;
		}
		return this;
	}

	final static class AnimatedRotateState extends Drawable.ConstantState {

		Drawable mDrawable;
		int mChangingConfigurations;
		boolean mPivotXRel;
		float mPivotX;
		boolean mPivotYRel;
		float mPivotY;
		int mFrameDuration;
		int mFramesCount;
		private boolean mCanConstantState;
		private boolean mCheckedConstantState;

		/**
		 * Instantiates a new animated rotate state.
		 * 
		 * @param source
		 *           the source
		 * @param owner
		 *           the owner
		 * @param res
		 *           the res
		 */
		public AnimatedRotateState( AnimatedRotateState source, AnimatedRotateDrawable owner, Resources res ) {
			if ( source != null ) {
				if ( res != null ) {
					mDrawable = source.mDrawable.getConstantState().newDrawable( res );
				} else {
					mDrawable = source.mDrawable.getConstantState().newDrawable();
				}
				mDrawable.setCallback( owner );
				mPivotXRel = source.mPivotXRel;
				mPivotX = source.mPivotX;
				mPivotYRel = source.mPivotYRel;
				mPivotY = source.mPivotY;
				mFramesCount = source.mFramesCount;
				mFrameDuration = source.mFrameDuration;
				mCanConstantState = mCheckedConstantState = true;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.graphics.drawable.Drawable.ConstantState#newDrawable()
		 */
		@Override
		public Drawable newDrawable() {
			return new AnimatedRotateDrawable( this, null );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.graphics.drawable.Drawable.ConstantState#newDrawable(android.content.res.Resources)
		 */
		@Override
		public Drawable newDrawable( Resources res ) {
			return new AnimatedRotateDrawable( this, res );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.graphics.drawable.Drawable.ConstantState#getChangingConfigurations()
		 */
		@Override
		public int getChangingConfigurations() {
			return mChangingConfigurations;
		}

		/**
		 * Can constant state.
		 * 
		 * @return true, if successful
		 */
		public boolean canConstantState() {
			if ( !mCheckedConstantState ) {
				mCanConstantState = mDrawable.getConstantState() != null;
				mCheckedConstantState = true;
			}

			return mCanConstantState;
		}
	}
}
