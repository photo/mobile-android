/**
 * The settings screen
 */
package me.openphoto.android.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * The settings screen
 * 
 * @author pas
 */
public class Settings extends Activity {
	/**
	 * Called when Settings Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
	}

}
