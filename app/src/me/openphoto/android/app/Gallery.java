/**
 * The photo gallery screen
 */
package me.openphoto.android.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * The photo gallery screen
 * 
 * @author pas
 */
public class Gallery extends Activity {
	/**
	 * Called when Gallery Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
	}

}
