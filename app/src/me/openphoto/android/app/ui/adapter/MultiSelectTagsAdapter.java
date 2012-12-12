
package me.openphoto.android.app.ui.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.openphoto.android.app.OpenPhotoApplication;
import me.openphoto.android.app.Preferences;
import me.openphoto.android.app.model.Tag;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.TagsResponse;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.compare.ToStringComparator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import me.openphoto.android.app.R;

/**
 * @author Eugene Popovich
 */
public abstract class MultiSelectTagsAdapter extends EndlessAdapter<Tag>
        implements OnCheckedChangeListener
{
    static final String TAG = MultiSelectTagsAdapter.class.getSimpleName();
    private final IOpenPhotoApi mOpenPhotoApi;
    protected Set<String> checkedTags = new HashSet<String>();
    private LoadingControl loadingControl;

    public MultiSelectTagsAdapter(LoadingControl loadingControl)
    {
        super(Integer.MAX_VALUE);
        this.loadingControl = loadingControl;
        mOpenPhotoApi = Preferences.getApi(OpenPhotoApplication.getContext());
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
                TagsResponse response = mOpenPhotoApi.getTags();
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
            for (String tagText : sortedTags) {
                if (buf.length() > 0)
                {
                    buf.append(",");
                }
                buf.append(tagText);
            }
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
