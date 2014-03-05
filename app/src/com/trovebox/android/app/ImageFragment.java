
package com.trovebox.android.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.trovebox.android.common.fragment.common.CommonFragment;

public final class ImageFragment extends CommonFragment {
    int imageResourceId;
    int contentResourceId;
    private static final String KEY_CONTENT = "ImageFragment:imageResourceId";
    int mNum;

    public static Fragment newInstance(int i, int content) {
        ImageFragment f = new ImageFragment();
        f.imageResourceId = i;
        f.contentResourceId = content;
        return f;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            imageResourceId = savedInstanceState.getInt(KEY_CONTENT);
        }

    }

    @Override
    public View onCreateView(org.holoeverywhere.LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        TextView text = new TextView(getActivity());
        text.setGravity(Gravity.CENTER);
        text.setText(getString(contentResourceId));
        text.setTextSize(10 * getResources().getDisplayMetrics().density);
        int padding = (int) getResources().getDimensionPixelSize(R.dimen.activity_intro_margin);
        text.setPadding(padding, padding, padding, padding);

        ImageView image = new ImageView(getActivity());
        image.setImageResource(imageResourceId);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        layout.setGravity(Gravity.CENTER);
        layout.addView(image);
        layout.addView(text);

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CONTENT, imageResourceId);
    }

}
