package com.aviary.android.feather.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import com.aviary.android.feather.R;
import com.aviary.android.feather.utils.TypefaceUtils;

public class TextViewCustomFont extends TextView {

	@SuppressWarnings("unused")
	private final String LOG_TAG = "TextViewCustomFont";

	public TextViewCustomFont( Context context ) {
		super( context );
	}

	public TextViewCustomFont( Context context, AttributeSet attrs ) {
		super( context, attrs );
		setCustomFont( context, attrs );
	}

	public TextViewCustomFont( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
		setCustomFont( context, attrs );
	}

	private void setCustomFont( Context ctx, AttributeSet attrs ) {

		TypedArray array = ctx.obtainStyledAttributes( attrs, R.styleable.TextViewCustomFont );
		String font = array.getString( R.styleable.TextViewCustomFont_font );
		setCustomFont( font );
		array.recycle();
	}

	protected void setCustomFont( String fontname ) {
		if ( null != fontname ) {
			try {
				Typeface font = TypefaceUtils.createFromAsset( getContext().getAssets(), fontname );
				setTypeface( font );
			} catch ( Throwable t ) {}
		}
	}
}
