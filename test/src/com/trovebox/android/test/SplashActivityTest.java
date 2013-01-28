package com.trovebox.android.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.trovebox.android.app.SplashActivity;

public class SplashActivityTest extends ActivityInstrumentationTestCase2<SplashActivity> {

	private SplashActivity activity;
	private TextView view;
	private String resourceString;

	public SplashActivityTest() {
		super("com.trovebox.android.app", SplashActivity.class);
	}

	/**
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		activity = this.getActivity();
		view = (TextView) activity
				.findViewById(com.trovebox.android.app.R.id.splash_title);
		resourceString = activity
                .getString(com.trovebox.android.app.R.string.app_name);
	}

	public void testPreconditions() {
		assertNotNull(view);
	}

	public void testText() {
		assertEquals(resourceString, (String) view.getText());
	}
}
