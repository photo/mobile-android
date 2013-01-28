
package com.trovebox.android.test;

import java.io.IOException;

import junit.framework.Assert;
import com.trovebox.android.test.R;

import org.apache.http.client.ClientProtocolException;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.powermock.api.easymock.PowerMock;

import com.trovebox.android.app.PhotoDetailsActivity;
import com.trovebox.android.app.PhotoDetailsActivity.PhotoDetailsUiFragment;
import com.trovebox.android.app.model.Photo;
import com.trovebox.android.app.net.PhotoResponse;
import com.trovebox.android.app.net.ReturnSizes;
import com.trovebox.android.test.net.JSONUtils;

import android.content.Intent;
import android.support.v4.app.Fragment;

public class PhotoDetailsActivityTest extends
        MockedInstrumentationTestCase<PhotoDetailsActivity>
{

    private PhotoDetailsActivity activity;

    public PhotoDetailsActivityTest() {
        super(PhotoDetailsActivity.class);
    }

    /**
     * @see android.test.ActivityInstrumentationTestCase2#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testPreconditions() throws JSONException,
            ClientProtocolException, IllegalStateException, IOException
    {
        // Setup mock calls and their responses
        PowerMock.reset(getApiMock());

        getApiMock().getPhoto(
                (String) EasyMock.anyObject(),
                (ReturnSizes) EasyMock.anyObject()
                );
        PowerMock
                .expectLastCall()
                .andReturn(
                        new PhotoResponse(JSONUtils.getJson(
                                getInstrumentation().getContext(),
                                R.raw.json_photo_get))).times(2);
        PowerMock.replayAll();

        Intent intent = new Intent();
        Photo photo = Photo.fromJson(JSONUtils.getJson(
                getInstrumentation().getContext(),
                R.raw.json_photo));
        intent.putExtra(PhotoDetailsActivity.EXTRA_PHOTO, photo);
        setActivityIntent(intent);
        activity = this.getActivity();

        Fragment fragment = activity.getSupportFragmentManager()
                .findFragmentById(android.R.id.content);
        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof PhotoDetailsUiFragment);
    }

}
