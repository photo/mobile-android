
package com.trovebox.android.app;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.trovebox.android.app.bitmapfun.util.ImageFetcher;
import com.trovebox.android.app.common.CommonRefreshableFragmentWithImageWorker;
import com.trovebox.android.app.model.ProfileInformation;
import com.trovebox.android.app.model.ProfileInformation.ProfileCounters;
import com.trovebox.android.app.net.ProfileResponseUtils;
import com.trovebox.android.app.net.ReturnSizes;
import com.trovebox.android.app.purchase.PurchaseController.PurchaseHandler;
import com.trovebox.android.app.purchase.PurchaseControllerUtils.SubscriptionPurchasedHandler;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.RunnableWithParameter;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * The fragment which displays account information
 * 
 * @author Eugene Popovich
 */
public class AccountFragment extends CommonRefreshableFragmentWithImageWorker implements
        SubscriptionPurchasedHandler
{
    public static final String TAG = AccountFragment.class.getSimpleName();
    private static final long KB = 1024l;
    private static final long MB = KB * KB;
    private static final long GB = MB * KB;

    private LoadingControl loadingControl;
    private PurchaseHandler purchaseHandler;

    private ReturnSizes thumbSize;

    private TextView userName;
    private TextView photosCount;
    private TextView tagsCount;
    private TextView albumsCount;
    private TextView storageUsed;
    private TextView storageUsedUnit;
    private TextView email;
    private TextView accountType;
    private View upgradeOffer;
    private ImageView profileImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        init(v, savedInstanceState);
        return v;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadingControl = ((LoadingControl) activity);
        purchaseHandler = ((PurchaseHandler) activity);

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
        storageUsedUnit = (TextView) view.findViewById(R.id.storageUsedUnit);
        email = (TextView) view.findViewById(R.id.email);
        accountType = (TextView) view.findViewById(R.id.accountType);
        upgradeOffer = view.findViewById(R.id.upgradeOffer);
        profileImage = (ImageView) view.findViewById(R.id.profilePic);

        Button upgradeButton = (Button) view.findViewById(R.id.upgradeButton);
        upgradeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TrackerUtils.trackButtonClickEvent("upgradeBtn", AccountFragment.this);
                CommonUtils.debug(TAG, "Upgrade button clicked.");
                purchaseHandler.purchaseMonthlySubscription();
            }
        });

        initView(null);

        refresh(view);
    }

    void refresh(View v)
    {
        ProfileResponseUtils.runWithProfileInformationAsync(
                new RunnableWithParameter<ProfileInformation>()
                {
                    @Override
                    public void run(ProfileInformation profileInformation) {
                        if (getView() != null)
                        {
                            initView(profileInformation);
                        }
                    }
                }, loadingControl);
    }

    void initView(ProfileInformation profileInformation) {
        if (profileInformation == null) {
            userName.setText(null);
            photosCount.setText(null);
            tagsCount.setText(null);
            albumsCount.setText(null);
            storageUsed.setText(null);
            storageUsedUnit.setText(null);
            email.setText(null);
            accountType.setText(null);
            upgradeOffer.setVisibility(View.GONE);
        } else {
            userName.setText(profileInformation.getName());
            ProfileCounters counters = profileInformation.getCounters();
            if (counters != null) {
                photosCount.setText(CommonUtils.format(counters.getPhotos()));
                tagsCount.setText(CommonUtils.format(counters.getTags()));
                albumsCount.setText(CommonUtils.format(counters.getAlbums()));
                initStorageUsedFields(counters.getStorage());
            }
            email.setText(profileInformation.getEmail());
            accountType.setText(profileInformation.isPaid() ? R.string.profile_account_type_pro
                    : R.string.profile_account_type_free);
            upgradeOffer.setVisibility(profileInformation.isPaid() ? View.GONE : View.VISIBLE);
            if (mImageWorker != null && !TextUtils.isEmpty(profileInformation.getPhotoUrl())) {
                mImageWorker.loadImage(profileInformation.getPhotoUrl(), profileImage);
            }
        }
    }

    public void initStorageUsedFields(long storageUsedValue)
    {
        int stringResourceId;
        if (storageUsedValue < MB)
        {
            storageUsedValue = storageUsedValue / KB;
            stringResourceId = R.string.profile_counter_storage_kb_used;
        } else if (storageUsedValue < GB)
        {
            storageUsedValue = storageUsedValue / MB;
            stringResourceId = R.string.profile_counter_storage_mb_used;
        } else
        {
            storageUsedValue = storageUsedValue / GB;
            stringResourceId = R.string.profile_counter_storage_gb_used;
        }
        storageUsed.setText(CommonUtils.format(storageUsedValue));
        storageUsedUnit.setText(stringResourceId);
    }

    @Override
    public void refresh() {
        refresh(getView());
    }

    @Override
    protected boolean isRefreshMenuVisible() {
        return !loadingControl.isLoading();
    }

    @Override
    public void subscriptionPurchased() {
        refreshImmediatelyOrScheduleIfNecessary();
    }
}
