package com.trovebox.android.app;

import com.trovebox.android.app.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public final class ImageFragment extends Fragment {
	int  imageResourceId;
	int  contentResourceId;
	private static final String KEY_CONTENT = "ImageFragment:imageResourceId";
	int mNum;
	public static Fragment newInstance(int i, int content) {
		ImageFragment f = new ImageFragment();
		// Supply num input as an argument.
	//	Bundle args = new Bundle();
		//args.putInt(KEY_CONTENT, i);
		//f.setArguments(args);
		f.imageResourceId = i;
		f.contentResourceId = content;
		//imageResourceId = i;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		TextView text = new TextView(getActivity());
        text.setGravity(Gravity.CENTER);
 //       text.setText("teste");
        text.setText(getString(contentResourceId));
        //int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
         //       (float) 123.4, getResources().getDisplayMetrics());
      //  int scale = (int)getResources().getDisplayMetrics().density;
       // Log.i("MyActivity", "MyClass.getView() Ñ get item number " +  getResources().getDisplayMetrics().density);
        text.setTextSize(12 * getResources().getDisplayMetrics().density);
        text.setPadding(10, 10, 10, 10);
		
		 
		ImageView image = new ImageView(getActivity());
		image.setImageResource(imageResourceId);

		LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

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
