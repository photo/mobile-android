/**
 * The photo-has-been-taken-do-we-want-to-use-it screen
 */
package me.openphoto.android.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * The photo-has-been-taken-do-we-want-to-use-it screen
 * 
 * @author pas
 */
public class PhotoTakenActivity extends Activity {
	/**
	 * Called when PhotoTaken Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_taken);
	}

}
