
package me.openphoto.android.app.ui.widget;

import me.openphoto.android.app.MainActivity;
import me.openphoto.android.app.R;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActionBar extends LinearLayout implements OnClickListener {

    private int mIconResId;
    private String mTitle;
    private boolean mRefreshActionVisible;

    private LinearLayout mActionsLayout;
    private ActionClickListener mActionClickListener;
    private int mLoaders = 0;

    public ActionBar(Context context) {
        super(context);
    }

    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBar);
        mIconResId =
                a.getResourceId(R.styleable.ActionBar_icon, R.drawable.actionbar_logo);
        mTitle = a.getString(R.styleable.ActionBar_title);
        if (mTitle == null) {
            mTitle = "";
        }
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.widget_actionbar, this);
        mActionsLayout = (LinearLayout) findViewById(R.id.layout_actions);
        setIcon(mIconResId);
        setTitle(mTitle);
        setUpRefreshing();
    }

    private void setUpRefreshing() {
        View refreshActionView = findViewById(R.id.image_refresh);
        refreshActionView.setOnClickListener(this);
        refreshActionView.setTag(R.id.action_refresh);
        setRefreshActionVisible(false);
        showLoading(false);
    }

    public void addActionTextOnly(int textResId, int id) {
        addAction(0, textResId, id);
    }

    public void addActionIconOnly(int drawableResId, int id) {
        addAction(drawableResId, 0, id);
    }

    public void addAction(int drawableResId, int textResId, int id) {
        View action = ((LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.widget_actionbar_action,
                mActionsLayout, false);

        ImageView imageView = (ImageView) action.findViewById(R.id.image);
        if (drawableResId != 0) {
            imageView.setImageResource(drawableResId);
        } else {
            imageView.setVisibility(View.GONE);
        }

        TextView textView = (TextView) action.findViewById(R.id.text);
        if (textResId != 0) {
            textView.setText(textResId);
        } else {
            textView.setVisibility(View.GONE);
        }
        action.setTag(id);
        action.setOnClickListener(this);
        mActionsLayout.addView(action);
    }

    public void removeAction(int deleteActionId) {
        View view = getActionView(deleteActionId);
        if (view != null) {
            mActionsLayout.removeView(view);
        }
    }

    public View getActionView(int actionId) {
        for (int i = 0; i < mActionsLayout.getChildCount(); i++) {
            View action = mActionsLayout.getChildAt(i);
            int viewActionId = ((Integer) action.getTag()).intValue();
            if (viewActionId == actionId) {
                return action;
            }
        }
        return null;
    }

    public void setRefreshActionVisible(boolean visible) {
        mRefreshActionVisible = visible;
        showRefreshAction(visible);
    }

    public void startLoading() {
        if (mLoaders++ == 0) {
            showLoading(true);
            showRefreshAction(false);
        }
    }

    public void stopLoading() {
        if (--mLoaders == 0) {
            showLoading(false);
            showRefreshAction(mRefreshActionVisible);
        }
    }

    private void showRefreshAction(boolean show) {
        findViewById(R.id.image_refresh).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showLoading(boolean show) {
        findViewById(R.id.progress).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setIcon(int drawableResId) {
        ImageView icon = (ImageView) findViewById(R.id.actionbar_icon);
        icon.setImageResource(drawableResId);
        icon.setOnClickListener(this);
    }

    public void setTitle(int stringResId) {
        ((TextView) findViewById(R.id.text)).setText(stringResId);
    }

    public void setTitle(String title) {
        ((TextView) findViewById(R.id.text)).setText(title);
    }

    public void setOnActionClickListener(ActionClickListener listener) {
        mActionClickListener = listener;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.actionbar_icon:
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getContext().startActivity(intent);
                break;

            // In default it must be a action item
            default:
                if (mActionClickListener != null) {
                    mActionClickListener.onActionClick((Integer) view.getTag());
                }
                break;
        }
    }

    public interface ActionClickListener {
        void onActionClick(int id);
    }

}
