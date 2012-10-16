package me.openphoto.android.app.facebook;

import me.openphoto.android.app.util.GuiUtils;
import android.app.Activity;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

/**
 * Skeleton base class for RequestListeners, providing default error handling.
 * Applications should handle these error conditions.
 */
public abstract class FacebookBaseDialogListener implements DialogListener {

	public static final String TAG = FacebookBaseDialogListener.class.getSimpleName();
	protected Activity activity;

	public FacebookBaseDialogListener(Activity activity)
	{
		this.activity = activity;
	}
    @Override
    public void onFacebookError(FacebookError e) {
		GuiUtils.error(TAG, null, e, activity);
    }

    @Override
    public void onError(DialogError e) {
		GuiUtils.error(TAG, null, new RuntimeException(e), activity);
    }

    @Override
    public void onCancel() {
    }

}