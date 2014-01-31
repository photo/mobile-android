
package com.trovebox.android.common.ui.widget;

import org.holoeverywhere.widget.ListView;

import uk.co.senab.photoview.VersionedGestureDetector;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * The ListView extension which supports scale gesture
 * 
 * @author Eugene Popovich
 */
public class ScalableListView extends ListView {
    VersionedGestureDetector mVersionedGestureDetector;

    public ScalableListView(Context context) {
        super(context);
    }

    public ScalableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScalableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setVersionedGestureDetector(VersionedGestureDetector versionedGestureDetector) {
        this.mVersionedGestureDetector = versionedGestureDetector;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean result = false;
        if (mVersionedGestureDetector != null) {
            result = mVersionedGestureDetector.onTouchEvent(ev);
        }
        if (mVersionedGestureDetector == null || !mVersionedGestureDetector.isScaling()
                && ev.getPointerCount() <= 1) {
            result = super.dispatchTouchEvent(ev);
        }
        return result;
    }
}
