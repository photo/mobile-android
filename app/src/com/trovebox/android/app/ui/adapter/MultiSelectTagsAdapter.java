
package com.trovebox.android.app.ui.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.Preferences;
import com.trovebox.android.app.R;
import com.trovebox.android.app.model.Tag;
import com.trovebox.android.app.model.utils.TagUtils;
import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.net.TagsResponse;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.app.util.GuiUtils;
import com.trovebox.android.app.util.LoadingControl;
import com.trovebox.android.app.util.compare.ToStringComparator;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * @author Eugene Popovich
 */
public abstract class MultiSelectTagsAdapter extends EndlessAdapter<Tag>
        implements OnCheckedChangeListener
{
    static final String TAG = MultiSelectTagsAdapter.class.getSimpleName();
    private final ITroveboxApi mTroveboxApi;
    protected Set<String> checkedTags = new HashSet<String>();
    private LoadingControl loadingControl;

    public MultiSelectTagsAdapter(LoadingControl loadingControl)
    {
        super(Integer.MAX_VALUE);
        this.loadingControl = loadingControl;
        mTroveboxApi = Preferences.getApi(TroveboxApplication.getContext());
        loadFirstPage();
    }

    @Override
    public long getItemId(int position)
    {
        return ((Tag) getItem(position)).getTag().hashCode();
    }

    public void initTagCheckbox(Tag tag, CheckBox checkBox) {
        checkBox.setText(tag.getTag());
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(isChecked(tag.getTag()));
        checkBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView,
            boolean isChecked) {

        String text = (String) buttonView.getText();
        if (isChecked)
            checkedTags.add(text);
        else
            checkedTags.remove(text);

    }

    @Override
    public LoadResponse loadItems(int page)
    {
        if (CommonUtils.checkLoggedInAndOnline())
        {
            try
            {
                TagsResponse response = mTroveboxApi.getTags();
                List<Tag> tags = response.getTags();
                if (tags != null)
                {
                    Collections.sort(tags, new ToStringComparator());
                }
                return new LoadResponse(tags, false);
            } catch (Exception e)
            {
                GuiUtils.error(TAG,
                        R.string.errorCouldNotLoadNextTagsInList, e);
            }
        }
        return new LoadResponse(null, false);
    }

    @Override
    protected void onStartLoading()
    {
        if(loadingControl != null)
        {
            loadingControl.startLoading();
        }
    }

    @Override
    protected void onStoppedLoading()
    {
        if(loadingControl != null)
        {
            loadingControl.stopLoading();
        }
    }

    public String getSelectedTags() {

        StringBuffer buf = new StringBuffer("");
        if (checkedTags.size() > 0) {
            List<String> sortedTags = new ArrayList<String>(checkedTags);
            Collections.sort(sortedTags, new ToStringComparator());
            buf.append(TagUtils.getTagsString(sortedTags));
        }
        return buf.toString();
    }

    protected boolean isChecked(String tag)
    {
        if (tag == null)
        {
            return false;
        }
        return checkedTags.contains(tag);
    }
}
