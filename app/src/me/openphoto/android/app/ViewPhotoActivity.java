/**
 * The general photo viewing screen
 */
package me.openphoto.android.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * The general photo viewing screen
 * 
 * @author pas
 */
public class ViewPhotoActivity extends Activity {
	/**
	 * Called when ViewPhoto Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_photo);
	}

}
