package com.trovebox.android.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ImageFragmentAdapter extends FragmentPagerAdapter {
    private static final int[] Images = new int[] {R.drawable.slide_1_image, R.drawable.slide_2_image, R.drawable.slide_3_image};
    private static final int[] Content = new int[] {R.string.slide_1_text, R.string.slide_2_text,R.string.slide_3_text};
            //private  static final int[] Images = new int[] 
   
    private int mCount = Images.length;

    public ImageFragmentAdapter(FragmentManager fm) {
        super(fm);
    
        
    }
    @Override
    public Fragment getItem(int position) {
        
        return  ImageFragment.newInstance(Images[position], Content[position]);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
   

}
