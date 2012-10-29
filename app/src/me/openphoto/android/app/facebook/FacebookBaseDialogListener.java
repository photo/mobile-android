
package me.openphoto.android.app.facebook;

import me.openphoto.android.app.util.GuiUtils;
import android.content.Context;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

/**
 * Skeleton base class for RequestListeners, providing default error handling.
 * Applications should handle these error conditions.
 */
public abstract class FacebookBaseDialogListener implements DialogListener {

    public static final String TAG = FacebookBaseDialogListener.class.getSimpleName();
    protected Context context;

    public FacebookBaseDialogListener(Context context)
    {
        this.context = context;
    }

    @Override
    public void onFacebookError(FacebookError e) {
        GuiUtils.error(TAG, null, e, context);
    }

    @Override
    public void onError(DialogError e) {
        GuiUtils.error(TAG, null, new RuntimeException(e), context);
    }

    @Override
    public void onCancel() {
    }

}
