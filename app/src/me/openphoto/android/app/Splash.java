/**
 * The splash/loading screen before you get to the Main screen
 */
package me.openphoto.android.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * The splash/loading screen before you get to the Main screen
 * 
 * @author pas
 */
public class Splash extends Activity {
	/**
	 * Called when Splash Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
	}

}
