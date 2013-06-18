package com.trovebox.android.app;


import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.widget.Switch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.trovebox.android.app.common.CommonStyledDialogFragment;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.model.utils.PhotoUtils;
import com.trovebox.android.app.model.utils.TagUtils;
import com.trovebox.android.app.util.ProgressDialogLoadingControl;
import com.trovebox.android.app.util.TrackerUtils;

/**
 * The view which represents photo details editing functionality
 * 
 * @author Eugene Popovich
 */
public class PhotoDetailsEditFragment extends CommonStyledDialogFragment {
    public static final String TAG = PhotoDetailsEditFragment.class.getSimpleName();
    public static final String PHOTO = "PhotoDetailsPhoto";
    private static final int REQUEST_TAGS = 2;

    Photo photo;

    Switch privateSwitch;

    EditText titleText;
    EditText descriptionText;
    EditText tagsText;

    Button selectTagsBtn;
    Button saveBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_edit_photo_details, container);
        init(view, savedInstanceState);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PHOTO, photo);
    }

    @Override
    public void onActivityResultUI(int requestCode, int resultCode, Intent data) {
        super.onActivityResultUI(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAGS:
                if (resultCode == Activity.RESULT_OK && data.getExtras() != null) {
                    String selectedTags = data.getExtras().getString(
                            SelectTagsActivity.SELECTED_TAGS);
                    tagsText.setText(selectedTags);
                }
                break;
            default:
                break;
        }
    }
    void init(View view, Bundle savedInstanceState)
    {
        if (savedInstanceState != null)
        {
            photo = savedInstanceState.getParcelable(PHOTO);
        }
        if (photo == null)
        {
            throw new IllegalStateException("Photo reference should be not null");
        }
        privateSwitch = (Switch) view.findViewById(R.id.private_switch);

        titleText = (EditText) view.findViewById(R.id.edit_title);
        descriptionText = (EditText) view.findViewById(R.id.edit_description);
        tagsText = (EditText) view.findViewById(R.id.edit_tags);

        selectTagsBtn = (Button) view.findViewById(R.id.select_tags);

        selectTagsBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TrackerUtils.trackButtonClickEvent("select_tags", getActivity());
                Intent i = new Intent(getActivity(), SelectTagsActivity.class);
                i.putExtra(SelectTagsActivity.SELECTED_TAGS, tagsText.getText().toString());
                startActivityForResult(i, REQUEST_TAGS);
            }
        });

        saveBtn = (Button) view.findViewById(R.id.button_save);

        saveBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                updatePhotoDetails();
            }
        });
        if (savedInstanceState == null)
        {
            initEditors(photo);
        }
    }

    /**
     * Set the photo to edit
     * 
     * @param photo
     */
    public void setPhoto(Photo photo)
    {
        this.photo = photo;
    }

    private void initEditors(Photo photo)
    {
        titleText.setText(photo.getTitle());
        descriptionText.setText(photo.getDescription());
        tagsText.setText(TagUtils.getTagsString(photo.getTags()));
        privateSwitch.setChecked(photo.isPrivate());
    }

    private void updatePhotoDetails()
    {
        final ProgressDialogLoadingControl loadingControl = new ProgressDialogLoadingControl(
                getActivity(), true, false,
                getString(R.string.updating_photo_message)
                );
        Runnable runOnSuccess = new Runnable() {

            @Override
            public void run() {
                // need to self dismiss on successful editing
                Dialog dialog = PhotoDetailsEditFragment.this.getDialog();
                if (dialog != null && dialog.isShowing())
                {
                    PhotoDetailsEditFragment.this.dismissAllowingStateLoss();
                }
            }
        };
        PhotoUtils.updatePhoto(photo,
                titleText.getText().toString(),
                descriptionText.getText().toString(),
                TagUtils.getTags(tagsText.getText().toString()),
                privateSwitch.isChecked(),
                runOnSuccess,
                loadingControl
                );
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog result = super.onCreateDialog(savedInstanceState);
        result.setTitle(R.string.photo_edit_dialog_title);
        return result;
    }
}
