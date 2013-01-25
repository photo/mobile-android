package com.aviary.android.feather;

import java.io.IOException;
import java.io.InputStream;
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
	
	protected String mApiKey;

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

		@Override
		public void onActivityCreated( MonitoredActivity activity ) {}

		@Override
		public void onActivityDestroyed( MonitoredActivity activity ) {}

		@Override
		public void onActivityPaused( MonitoredActivity activity ) {}

		@Override
		public void onActivityResumed( MonitoredActivity activity ) {}

		@Override
		public void onActivityStarted( MonitoredActivity activity ) {}

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
		
		Tracker.create( this, getApplicationContext().getPackageName(), getApiKey(), FeatherActivity.SDK_VERSION );
		Tracker.open();
		Tracker.upload();
	}
	
	/**
	 * Return the Application specific API-KEY
	 * @return
	 */
	public String getApiKey() {
		if( null == mApiKey ) {
			try {
				mApiKey = readApiKey();
			} catch ( IOException e ) {
				e.printStackTrace();
				
				// right now it's only a warning and we will use a
				// default api key.
				mApiKey = "MXI5mzSf6Ei6gEQ5eTAOPg";
			}
		}
		return mApiKey;
	}
	
	/**
	 * Read the API-KEY from the assets/aviary-credentials.txt file
	 * @param context
	 * @return
	 * @throws IOException
	 */
	private String readApiKey() throws IOException {
		InputStream stream = getAssets().open( "aviary-credentials.txt" );
		int size = stream.available();
		byte[] buffer = new byte[size];
		stream.read( buffer );
		stream.close();
		return new String( buffer );
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
