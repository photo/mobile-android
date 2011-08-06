package me.openphoto.android.test;

import me.openphoto.android.app.ViewPhoto;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

public class ViewPhotoTest extends ActivityInstrumentationTestCase2<ViewPhoto> {

	private ViewPhoto activity;
	private TextView view;
	private String resourceString;

	public ViewPhotoTest() {
		super("me.openphoto.android.app", ViewPhoto.class);
	}

	/**
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		activity = this.getActivity();
		view = (TextView) activity
				.findViewById(me.openphoto.android.app.R.id.title);
		resourceString = activity
				.getString(me.openphoto.android.app.R.string.app_name);
	}

	public void testPreconditions() {
		assertNotNull(view);
	}

	public void testText() {
		assertEquals(resourceString, (String) view.getText());
	}
}
