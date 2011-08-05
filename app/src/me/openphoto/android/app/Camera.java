/**
 * The embedded camera screen
 */
package me.openphoto.android.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * The embedded camera screen
 * 
 * @author pas
 */
public class Camera extends Activity {
	/**
	 * Called when Camera Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
	}

}
