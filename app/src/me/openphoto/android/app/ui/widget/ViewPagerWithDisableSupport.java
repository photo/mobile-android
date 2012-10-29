
package me.openphoto.android.app.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPagerWithDisableSupport extends ViewPager
{
    GesturesEnabledHandler handler;

    public ViewPagerWithDisableSupport(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGesturesEnabled()) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (isGesturesEnabled()) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    boolean isGesturesEnabled()
    {
        return handler == null ? true : handler.isEnabled();
    }
    public void setGesturesEnabledHandler(GesturesEnabledHandler handler) {
        this.handler = handler;
    }

    public static interface GesturesEnabledHandler
    {
        boolean isEnabled();
    }
}
