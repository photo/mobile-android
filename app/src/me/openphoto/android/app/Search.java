/**
 * The search screen
 */
package me.openphoto.android.app;

import android.app.Activity;
import android.os.Bundle;

/**
 * The search screen
 * 
 * @author pas
 */
public class Search extends Activity {
	/**
	 * Called when Search Activity is first loaded
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
	}

}
