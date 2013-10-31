package com.trovebox.android.app;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.trovebox.android.app.bitmapfun.util.ImageFetcher;
import com.trovebox.android.app.common.CommonActivity;
import com.trovebox.android.app.common.CommonFragmentWithImageWorker;
import com.trovebox.android.app.model.Credentials;
import com.trovebox.android.app.model.ProfileInformation;
import com.trovebox.android.app.net.ReturnSizes;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.LoginUtils;
import com.trovebox.android.app.util.SimpleViewLoadingControl;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * An activity to select account to login with
 * 
 * @author Eugene Popovich
 */
public class SelectAccountActivity extends CommonActivity {
    public static final String TAG = SelectAccountActivity.class.getSimpleName();
    public static final String CREDENTIALS = "CREDENTIALS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SelectAccountFragment()).commit();
        }
    }

    public static class SelectAccountFragment extends CommonFragmentWithImageWorker implements
            OnItemClickListener {

        private LoadingControl mLoadingControl;

        private AccountsAdapter mAdapter;

        private ReturnSizes mThumbSize;

        private ArrayList<Credentials> mCredentials;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                mCredentials = savedInstanceState.getParcelableArrayList(CREDENTIALS);
            } else {
                mCredentials = getActivity().getIntent().getParcelableArrayListExtra(CREDENTIALS);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_select_account, container, false);

            refresh(v, savedInstanceState);
            return v;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putParcelableArrayList(CREDENTIALS, mCredentials);
        }

        void refresh(View v, Bundle savedInstanceState) {

            mLoadingControl = new SimpleViewLoadingControl(v.findViewById(R.id.loading));
            mAdapter = new AccountsAdapter(mCredentials);
            ListView list = (ListView) v.findViewById(R.id.lsitAccounts);
            list.setAdapter(mAdapter);
            list.setOnItemClickListener(this);
        }

        @Override
        protected void initImageWorker() {
            int mImageThumbSize = getResources().getDimensionPixelSize(
                    R.dimen.select_profile_thumbnail_size);
            mThumbSize = new ReturnSizes(mImageThumbSize, mImageThumbSize, true);
            int profilePicCornerRadius = getResources().getDimensionPixelSize(
                    R.dimen.select_profile_thumbnail_corner_radius);
            mImageWorker = new ImageFetcher(getActivity(), null, mThumbSize.getWidth(),
                    mThumbSize.getHeight(), profilePicCornerRadius);
            mImageWorker.setLoadingImage(R.drawable.profilepic);
            mImageWorker.setImageFadeIn(false);
            ((ImageFetcher) mImageWorker).setCheckLoggedIn(false);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            TrackerUtils.trackButtonClickEvent("album_item", SelectAccountFragment.this);
            Credentials credentials = mAdapter.getItem(position);
            Activity activity = getSupportActivity();
            credentials.saveCredentials(activity);
            LoginUtils.onLoggedIn(activity, true);
        }

        private class AccountsAdapter extends BaseAdapter {
            LayoutInflater mInflater;
            List<Credentials> mData;

            public AccountsAdapter(List<Credentials> data) {
                super();
                mInflater = LayoutInflater.from(getActivity());
                this.mData = data;
            }

            class ViewHolder {
                ImageView imageView;
                TextView textView;
            }

            @Override
            public int getCount() {
                return mData.size();
            }

            @Override
            public Credentials getItem(int position) {
                return mData.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public final View getView(int position, View convertView, ViewGroup container) {
                final ViewHolder vh;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_account, null);
                    vh = new ViewHolder();
                    vh.imageView = (ImageView) convertView.findViewById(R.id.profilePic);
                    vh.textView = (TextView) convertView.findViewById(R.id.name);
                    convertView.setTag(vh);
                } else {
                    vh = (ViewHolder) convertView.getTag();
                }
                Credentials credentials = getItem(position);
                vh.textView.setText(credentials.getHost());
                ProfileInformation profileInformation = credentials.getProfileInformation();
                if (profileInformation != null
                        && !TextUtils.isEmpty(profileInformation.getPhotoUrl())) {
                    mImageWorker.loadImage(profileInformation.getPhotoUrl(), vh.imageView,
                            mLoadingControl);
                } else {
                    mImageWorker.loadImage(null, vh.imageView);
                }
                return convertView;
            }
        }

        public void setCredentials(ArrayList<Credentials> credentials) {
            mCredentials = credentials;
        }
    }

}
