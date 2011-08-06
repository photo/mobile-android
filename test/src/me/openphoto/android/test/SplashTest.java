package me.openphoto.android.test;

import me.openphoto.android.app.Splash;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

public class SplashTest extends ActivityInstrumentationTestCase2<Splash> {

	private Splash activity;
	private TextView view;
	private String resourceString;

	public SplashTest() {
		super("me.openphoto.android.app", Splash.class);
	}

	/**
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		activity = this.getActivity();
		view = (TextView) activity
				.findViewById(me.openphoto.android.app.R.id.splash_title);
		resourceString = activity
				.getString(me.openphoto.android.app.R.string.openphoto);
	}

	public void testPreconditions() {
		assertNotNull(view);
	}

	public void testText() {
		assertEquals(resourceString, (String) view.getText());
	}
}
