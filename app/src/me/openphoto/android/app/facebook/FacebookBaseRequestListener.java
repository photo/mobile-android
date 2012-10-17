package me.openphoto.android.app.facebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import me.openphoto.android.app.util.GuiUtils;
import android.content.Context;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

/**
 * Skeleton base class for RequestListeners, providing default error handling.
 * Applications should handle these error conditions.
 */
public abstract class FacebookBaseRequestListener implements RequestListener {
	static final String TAG = FacebookBaseRequestListener.class.getSimpleName();
	Context context;

	public FacebookBaseRequestListener(Context context)
	{
		this.context = context;
	}
    @Override
    public void onFacebookError(FacebookError e, final Object state) {
		processException(e);
    }

    @Override
    public void onFileNotFoundException(FileNotFoundException e, final Object state) {
		processException(e);
    }

    @Override
    public void onIOException(IOException e, final Object state) {
		processException(e);
    }

    @Override
    public void onMalformedURLException(MalformedURLException e, final Object state) {
		processException(e);
    }

	public void processException(Exception e)
	{
		GuiUtils.error(TAG, null, e, context);
	}

}
