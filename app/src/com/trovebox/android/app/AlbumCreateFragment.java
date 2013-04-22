package com.trovebox.android.app;


import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Dialog;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.trovebox.android.app.common.CommonStyledDialogFragment;
import com.trovebox.android.app.model.utils.AlbumUtils;
import com.trovebox.android.app.util.ProgressDialogLoadingControl;

/**
 * The fragment which represents albums creation functionality
 * 
 * @author Eugene Popovich
 */
public class AlbumCreateFragment extends CommonStyledDialogFragment {
    public static final String TAG = AlbumCreateFragment.class.getSimpleName();

    EditText titleText;
    Button saveBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_create_album, container);
        init(view, savedInstanceState);
        return view;
    }


    void init(View view, Bundle savedInstanceState)
    {
        titleText = (EditText) view.findViewById(R.id.edit_title);

        saveBtn = (Button) view.findViewById(R.id.button_save);

        saveBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                createAlbum();
            }
        });
    }

    private void createAlbum()
    {
        final ProgressDialogLoadingControl loadingControl = new ProgressDialogLoadingControl(
                getActivity(), true, false,
                getString(R.string.creating_album_message)
                );
        Runnable runOnSuccess = new Runnable() {

            @Override
            public void run() {
                // need to self dismiss on successful editing
                Dialog dialog = AlbumCreateFragment.this.getDialog();
                if (dialog != null && dialog.isShowing())
                {
                    AlbumCreateFragment.this.dismiss();
                }
            }
        };
        AlbumUtils.createAlbum(
                titleText.getText().toString(),
                runOnSuccess,
                loadingControl
                );
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog result = super.onCreateDialog(savedInstanceState);
        result.setTitle(R.string.album_create_dialog_title);
        return result;
    }
}
