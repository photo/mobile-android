
package com.trovebox.android.app;

import android.os.Bundle;

import com.trovebox.android.common.activity.CommonActivityWithIndeterminateProgress;
import com.trovebox.android.common.fragment.upload.UploadManagerFragment;

/**
 * Simple upload manager activity wrapper around UploadManagerFragment
 * 
 * @author Eugene Popovich
 */
public class UploadManagerActivity extends CommonActivityWithIndeterminateProgress {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new UploadManagerFragment()).commit();
        }
    }

}
