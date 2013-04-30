
package com.trovebox.android.app.share;

import java.io.Serializable;

import org.holoeverywhere.app.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;

import com.trovebox.android.app.FacebookFragment;
import com.trovebox.android.app.R;
import com.trovebox.android.app.TwitterFragment;
import com.trovebox.android.app.common.CommonFragment;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.model.utils.PhotoUtils;
import com.trovebox.android.app.ui.widget.YesNoDialogFragment;
import com.trovebox.android.app.ui.widget.YesNoDialogFragment.YesNoButtonPressedHandler;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.RunnableWithParameter;
import com.trovebox.android.app.util.RunnableWithResult;

/**
 * Sharing functionality utils
 * 
 * @author Eugene Popovich
 */
public class ShareUtils {
    static final String TAG = ShareUtils.class.getSimpleName();

    /**
     * Show confirmation dialog to confirm sharing of private photo
     * 
     * @param photo the photo to share
     * @param runnable action to run in case of photo is public or user
     *            confirmed share of private photo
     * @param activity
     */
    public static void confirmPrivatePhotoSharingAndRun(Photo photo, final Runnable runnable,
            Activity activity)
    {
        if (photo != null)
        {
            if (photo.isPrivate())
            {
                YesNoDialogFragment dialogFragment = YesNoDialogFragment
                        .newInstance(R.string.share_private_photo_confirmation_question,
                                new YesNoButtonPressedHandler()
                                {
                                    @Override
                                    public void yesButtonPressed(
                                            DialogInterface dialog)
                                    {
                                        runnable.run();
                                    }

                                    @Override
                                    public void noButtonPressed(
                                            DialogInterface dialog)
                                    {
                                        // DO NOTHING
                                    }
                                });
                dialogFragment.show(activity);
            } else
            {
                runnable.run();
            }
        }
    }

    /**
     * Shares the specified photo via email. The email application should be
     * installed on the user device
     * 
     * @param photo
     * @param context
     * @param loadingControl the loading control for token retrieval operation
     */
    public static void shareViaEMail(Photo photo, final Context context,
            LoadingControl loadingControl)
    {
        RunnableWithParameter<Photo> runnable = new RunnableWithParameter<Photo>() {

            @Override
            public void run(Photo photo) {
                String mailId = "";
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", mailId, null));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        CommonUtils.getStringResource(R.string.share_email_default_title));
                String url = PhotoUtils.getShareUrl(photo, photo.isPrivate());
                String bodyText = CommonUtils.getStringResource(
                        R.string.share_email_default_body,
                        url, url);
                emailIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        Html.fromHtml(bodyText)
                        );
                context.startActivity(Intent.createChooser(emailIntent,
                        CommonUtils.getStringResource(R.string.share_email_send_title)));
            }
        };
        if (photo.isPrivate())
        {
            PhotoUtils.validateShareTokenExistsAsyncAndRunAsync(photo,
                    runnable,
                    null,
                    loadingControl);
        } else
        {
            runnable.run(photo);
        }

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
