
package com.trovebox.android.app.ui.widget;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.trovebox.android.app.R;

/**
 * Category separator which is used in slider menu
 * 
 * @author Eugene Popovich
 */
public class SliderCategorySeparator extends LinearLayout {
    private final TextView label;

    public SliderCategorySeparator(Context context) {
        this(context, null);
    }

    public SliderCategorySeparator(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sliderCategorySeparatorStyle);
    }

    public SliderCategorySeparator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SliderCategorySeparator,
                defStyleAttr, R.style.trovebox_SliderCategorySeparator);
        LayoutInflater.inflate(context, R.layout.slider_category_separator, this, true);
        label = (TextView) findViewById(android.R.id.text1);
        if (a.hasValue(R.styleable.SliderCategorySeparator_android_text)) {
            setLabel(a.getText(R.styleable.SliderCategorySeparator_android_text));
        }
        if (a.hasValue(R.styleable.SliderCategorySeparator_android_textColor)) {
            setTextColor(a.getColorStateList(R.styleable.SliderCategorySeparator_android_textColor));
        }
        if (a.hasValue(R.styleable.SliderCategorySeparator_android_background)) {
            setBackgroundResource(a.getResourceId(
                    R.styleable.SliderCategorySeparator_android_background, 0));
        }
        a.recycle();
    }

    /**
     * Set the label text
     * 
     * @param label
     */
    public void setLabel(CharSequence label) {
        this.label.setText(label);
    }

    /**
     * Set the label text resource id
     * 
     * @param resId
     */
    public void setLabel(int resId) {
        setLabel(getResources().getText(resId));
    }

    /**
     * Set label text color
     * 
     * @param color
     */
    public void setTextColor(int color)
    {
        this.label.setTextColor(color);
    }

    /**
     * Set label text color
     * 
     * @param color
     */
    public void setTextColor(ColorStateList color)
    {
        this.label.setTextColor(color);
    }

}
