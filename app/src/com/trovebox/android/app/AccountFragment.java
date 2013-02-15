
package com.trovebox.android.app;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.trovebox.android.app.bitmapfun.util.ImageFetcher;
import com.trovebox.android.app.common.CommonFrargmentWithImageWorker;
import com.trovebox.android.app.net.ProfileResponse;
import com.trovebox.android.app.net.ProfileResponse.ProfileCounters;
import com.trovebox.android.app.net.ProfileResponseUtils;
import com.trovebox.android.app.net.ReturnSizes;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.RunnableWithParameter;

/**
 * The fragment which displays account information
 * 
 * @author Eugene Popovich
 */
public class AccountFragment extends CommonFrargmentWithImageWorker
{
    public static final String TAG = AccountFragment.class.getSimpleName();

    private LoadingControl loadingControl;

    private ReturnSizes thumbSize;

    private TextView userName;
    private TextView photosCount;
    private TextView tagsCount;
    private TextView albumsCount;
    private TextView storageUsed;
    private TextView server;
    private TextView accountType;
    private View upgradeOffer;
    private ImageView profileImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        init(v, savedInstanceState);
        return v;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);

    }

    @Override
    protected void initImageWorker() {
        int mImageThumbSize = getResources().getDimensionPixelSize(
                R.dimen.profile_thumbnail_size);
        thumbSize = new ReturnSizes(mImageThumbSize, mImageThumbSize, true);
        int profilePicCornerRadius = getResources().getDimensionPixelSize(
                R.dimen.profile_thumbnail_corner_radius);
        mImageWorker = new ImageFetcher(getActivity(), loadingControl, thumbSize.getWidth(),
                thumbSize.getHeight(), profilePicCornerRadius);
    }

    void init(View view, Bundle savedInstanceState)
    {
        TextView upgradeOfferDetails = (TextView) view.findViewById(R.id.upgradeOfferDetails);
        upgradeOfferDetails.setMovementMethod(LinkMovementMethod.getInstance());

        userName = (TextView) view.findViewById(R.id.userName);
        photosCount = (TextView) view.findViewById(R.id.photosCount);
        tagsCount = (TextView) view.findViewById(R.id.tagsCount);
        albumsCount = (TextView) view.findViewById(R.id.albumsCount);
        storageUsed = (TextView) view.findViewById(R.id.storageUsed);
        server = (TextView) view.findViewById(R.id.server);
        accountType = (TextView) view.findViewById(R.id.accountType);
        upgradeOffer = view.findViewById(R.id.upgradeOffer);
        profileImage = (ImageView) view.findViewById(R.id.profilePic);

        initView(null);
        ProfileResponseUtils.runWithProfileResponseAsync(
                new RunnableWithParameter<ProfileResponse>()
                {
                    @Override
                    public void run(ProfileResponse profileResponse) {
                        if (getView() != null)
                        {
                            initView(profileResponse);
                        }
                    }
                }, loadingControl);

    }

    void initView(ProfileResponse response)
    {
        if (response == null)
        {
            userName.setText(null);
            photosCount.setText(null);
            tagsCount.setText(null);
            albumsCount.setText(null);
            storageUsed.setText(null);
            server.setText(null);
            accountType.setText(null);
            upgradeOffer.setVisibility(View.GONE);
        } else
        {
            userName.setText(response.getName());
            ProfileCounters counters = response.getCounters();
            if (counters != null)
            {
                photosCount.setText(CommonUtils.format(counters.getPhotos()));
                tagsCount.setText(CommonUtils.format(counters.getTags()));
                albumsCount.setText(CommonUtils.format(counters.getAlbums()));
                long storageUsedValue = counters.getStorage() / 1024 / 1024;
                storageUsed.setText(CommonUtils.format(storageUsedValue));
            }
            server.setText(response.getId());
            accountType.setText(response.isPaid() ? R.string.profile_account_type_pro
                    : R.string.profile_account_type_free);
            upgradeOffer.setVisibility(response.isPaid() ? View.GONE : View.VISIBLE);
            if (mImageWorker != null && !TextUtils.isEmpty(response.getPhotoUrl()))
            {
                mImageWorker.loadImage(response.getPhotoUrl(), profileImage);
            }
        }
    }


}
