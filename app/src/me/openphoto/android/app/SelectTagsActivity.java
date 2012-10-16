package me.openphoto.android.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.openphoto.android.app.model.Tag;
import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.TagsResponse;
import me.openphoto.android.app.ui.adapter.EndlessAdapter;
import me.openphoto.android.app.util.GuiUtils;
import me.openphoto.android.app.util.LoadingControl;
import me.openphoto.android.app.util.Utils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;

public class SelectTagsActivity extends Activity implements OnClickListener,
		LoadingControl {

	public static final String TAG = SelectTagsActivity.class.getSimpleName();

	private TagsAdapter mAdapter;
	private ListView list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_tags);
		mAdapter = new TagsAdapter(this);
		list = (ListView) findViewById(R.id.list_select_tags);

		list.setAdapter(mAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_select_tags, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@SuppressLint("NewApi")
	public void finishedClicked(View v) {

		int checkedItemCount = list.getCheckedItemCount();
		Toast.makeText(this, "checked items: " + checkedItemCount,
				Toast.LENGTH_SHORT).show();
		
		Intent data = new Intent();
		String selectedTags = mAdapter.getSelectedTags().trim();
		
		data.putExtra("SELECTED_TAGS", selectedTags);
		setResult(RESULT_OK, data);
		finish();

	}

	private class TagsAdapter extends EndlessAdapter<Tag> implements
			OnCheckedChangeListener {
		private final IOpenPhotoApi mOpenPhotoApi;
		private Activity parentActivity;
		private ArrayList<String> checkedTags = new ArrayList<String>();

		public TagsAdapter(Activity parentActivity) {
			super(Integer.MAX_VALUE);
			this.parentActivity = parentActivity;
			mOpenPhotoApi = Preferences.getApi(parentActivity);
			loadFirstPage();
		}

		@Override
		public long getItemId(int position) {
			return ((Tag) getItem(position)).getTag().hashCode();
		}

		@Override
		public View getView(Tag tag, View convertView, ViewGroup parent) {
			if (convertView == null) {
				final LayoutInflater layoutInflater = (LayoutInflater) this.parentActivity
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(
						R.layout.list_item_tag_checkbox, null);
			}

			CheckBox checkBox = (CheckBox) convertView
					.findViewById(R.id.tag_checkbox);
			checkBox.setText(tag.getTag());
			checkBox.setOnCheckedChangeListener(this);

			return convertView;
		}

		@Override
		public LoadResponse loadItems(int page) {
			if (checkOnline()) {
				try {
					TagsResponse response = mOpenPhotoApi.getTags();
					return new LoadResponse(response.getTags(), false);
				} catch (Exception e) {
					Log.e(TAG, "Could not load next photos in list", e);
					Map<String, String> extraData = new HashMap<String, String>();
					extraData.put("message",
							"Could not load next photos in list");
					// BugSenseHandler.log(TAG, extraData, e);
				}
			}
			return new LoadResponse(null, false);
		}

		@Override
		protected void onStartLoading() {
			((LoadingControl) parentActivity).startLoading();
		}

		@Override
		protected void onStoppedLoading() {
			((LoadingControl) parentActivity).stopLoading();
		}

		protected boolean checkOnline() {
			boolean result = Utils.isOnline(parentActivity);
			if (!result) {
				GuiUtils.alert(getString(R.string.noInternetAccess),
						parentActivity);
			}
			return result;
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

		public String getSelectedTags() {

			StringBuffer buf = new StringBuffer();

			for (String tagText : checkedTags) {
				buf.append(tagText).append(",");
			}

			// remove last comma
			buf.deleteCharAt(buf.length()-1);
			
			return buf.toString();
		}

	}

	@Override
	public void startLoading() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopLoading() {
		// TODO Auto-generated method stub

	}

}
