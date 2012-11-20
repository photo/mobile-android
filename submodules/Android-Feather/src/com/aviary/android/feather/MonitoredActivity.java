package com.aviary.android.feather;

import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import com.aviary.android.feather.library.tracking.Tracker;

// TODO: Auto-generated Javadoc
/**
 * The Class MonitoredActivity.
 */
public class MonitoredActivity extends Activity {

	/** The m listeners. */
	private final ArrayList<LifeCycleListener> mListeners = new ArrayList<LifeCycleListener>();

	/**
	 * The listener interface for receiving lifeCycle events. The class that is interested in processing a lifeCycle event implements
	 * this interface, and the object created with that class is registered with a component using the component's
	 * <code>addLifeCycleListener<code> method. When
	 * the lifeCycle event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see LifeCycleEvent
	 */
	public static interface LifeCycleListener {

		/**
		 * Invoked when on activity is created.
		 * 
		 * @param activity
		 *           the activity
		 */
		public void onActivityCreated( MonitoredActivity activity );

		/**
		 * On activity destroyed.
		 * 
		 * @param activity
		 *           the activity
		 */
		public void onActivityDestroyed( MonitoredActivity activity );

		/**
		 * On activity paused.
		 * 
		 * @param activity
		 *           the activity
		 */
		public void onActivityPaused( MonitoredActivity activity );

		/**
		 * On activity resumed.
		 * 
		 * @param activity
		 *           the activity
		 */
		public void onActivityResumed( MonitoredActivity activity );

		/**
		 * On activity started.
		 * 
		 * @param activity
		 *           the activity
		 */
		public void onActivityStarted( MonitoredActivity activity );

		/**
		 * On activity stopped.
		 * 
		 * @param activity
		 *           the activity
		 */
		public void onActivityStopped( MonitoredActivity activity );
	}

	/**
	 * The Class LifeCycleAdapter.
	 */
	public static class LifeCycleAdapter implements LifeCycleListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.aviary.android.feather.MonitoredActivity.LifeCycleListener#onActivityCreated(com.aviary.android.feather.MonitoredActivity
		 * )
		 */
		@Override
		public void onActivityCreated( MonitoredActivity activity ) {}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.aviary.android.feather.MonitoredActivity.LifeCycleListener#onActivityDestroyed(com.aviary.android.feather.MonitoredActivity
		 * )
		 */
		@Override
		public void onActivityDestroyed( MonitoredActivity activity ) {}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.aviary.android.feather.MonitoredActivity.LifeCycleListener#onActivityPaused(com.aviary.android.feather.MonitoredActivity
		 * )
		 */
		@Override
		public void onActivityPaused( MonitoredActivity activity ) {}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.aviary.android.feather.MonitoredActivity.LifeCycleListener#onActivityResumed(com.aviary.android.feather.MonitoredActivity
		 * )
		 */
		@Override
		public void onActivityResumed( MonitoredActivity activity ) {}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.aviary.android.feather.MonitoredActivity.LifeCycleListener#onActivityStarted(com.aviary.android.feather.MonitoredActivity
		 * )
		 */
		@Override
		public void onActivityStarted( MonitoredActivity activity ) {}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.aviary.android.feather.MonitoredActivity.LifeCycleListener#onActivityStopped(com.aviary.android.feather.MonitoredActivity
		 * )
		 */
		@Override
		public void onActivityStopped( MonitoredActivity activity ) {}
	}

	/**
	 * Adds the life cycle listener.
	 * 
	 * @param listener
	 *           the listener
	 */
	public void addLifeCycleListener( LifeCycleListener listener ) {
		if ( mListeners.contains( listener ) ) return;
		mListeners.add( listener );
	}

	/**
	 * Removes the life cycle listener.
	 * 
	 * @param listener
	 *           the listener
	 */
	public void removeLifeCycleListener( LifeCycleListener listener ) {
		mListeners.remove( listener );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		for ( LifeCycleListener listener : mListeners ) {
			listener.onActivityCreated( this );
		}
		Tracker.create( this, getApplicationContext().getPackageName(), "d2703c903", FeatherActivity.SDK_VERSION );
		Tracker.open();
		Tracker.upload();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		for ( LifeCycleListener listener : mListeners ) {
			listener.onActivityDestroyed( this );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		for ( LifeCycleListener listener : mListeners ) {
			listener.onActivityStarted( this );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		for ( LifeCycleListener listener : mListeners ) {
			listener.onActivityStopped( this );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		Tracker.close();
		Tracker.upload();
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		Tracker.open();
		super.onResume();
	}
}
