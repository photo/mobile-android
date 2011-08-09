/**
 * The photo details and meta-data screen
 */
package me.openphoto.android.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * The photo details and meta-data screen
 * 
 * @author pas
 */
public class PhotoDetailsActivity extends Activity {
	/**
	 * Called when PhotoDetails Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_details);
	}

}
