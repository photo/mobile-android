package com.trovebox.android.app.share;

import java.io.Serializable;

import com.trovebox.android.app.FacebookFragment;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TwitterFragment;
import com.trovebox.android.app.common.CommonFragment;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.RunnableWithResult;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;

/**
 * Sharing functionality utils
 * 
 * @author Eugene Popovich
 */
public class ShareUtils {
    static final String TAG = ShareUtils.class.getSimpleName();
    
    /**
     * Shares the specified photo via email. The email application should be
     * installed on the user device
     * 
     * @param photo
     * @param context
     */
    public static void shareViaEMail(Photo photo, Context context)
    {
        String mailId = "";
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", mailId, null));
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                context.getString(R.string.share_email_default_title));
        String url = photo.getUrl(Photo.URL);
        String bodyText = String.format(context.getString(R.string.share_email_default_body),
                url, url);
        emailIntent.putExtra(
                Intent.EXTRA_TEXT,
                Html.fromHtml(bodyText)
                );
        context.startActivity(Intent.createChooser(emailIntent,
                context.getString(R.string.share_email_send_title)));
    }
    
    /**
     * The runnable which opens twitter share dialog
     */
    public static class TwitterShareRunnable implements Runnable, Serializable
    {
        private static final long serialVersionUID = 1L;

        Photo photo;
        RunnableWithResult<? extends CommonFragment> fragmentInstanceAccessor;

        public TwitterShareRunnable(Photo photo,
                RunnableWithResult<? extends CommonFragment> fragmentInstanceAccessor)
        {
            this.photo = photo;
            this.fragmentInstanceAccessor = fragmentInstanceAccessor;
        }

        @Override
        public void run()
        {
            try
            {
                TwitterFragment twitterDialog = new TwitterFragment();
                twitterDialog.setPhoto(photo);
                twitterDialog.show(fragmentInstanceAccessor.run().getSupportActivity());
            } catch (Exception ex)
            {
                GuiUtils.error(TAG, null, ex);
            }
        }
    }

    /**
     * The runnable which opens facebook share dialog
     */
    public static class FacebookShareRunnable implements Runnable, Serializable
    {
        private static final long serialVersionUID = 1L;

        Photo photo;
        RunnableWithResult<? extends CommonFragment> fragmentInstanceAccessor;

        public FacebookShareRunnable(Photo photo,
                RunnableWithResult<? extends CommonFragment> fragmentInstanceAccessor)
        {
            this.photo = photo;
            this.fragmentInstanceAccessor = fragmentInstanceAccessor;
        }

        @Override
        public void run()
        {
            try
            {
                FacebookFragment facebookDialog = new FacebookFragment();
                facebookDialog.setPhoto(photo);
                facebookDialog.show(fragmentInstanceAccessor.run().getSupportActivity());
            } catch (Exception ex)
            {
                GuiUtils.error(TAG, null, ex);
            }
        }
    }
}
