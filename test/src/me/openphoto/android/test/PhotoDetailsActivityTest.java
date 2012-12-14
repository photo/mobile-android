
package me.openphoto.android.test;

import java.io.IOException;

import junit.framework.Assert;
import me.openphoto.android.app.PhotoDetailsActivity;
import me.openphoto.android.app.PhotoDetailsActivity.PhotoDetailsUiFragment;
import me.openphoto.android.app.model.Photo;
import me.openphoto.android.app.net.PhotoResponse;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.test.net.JSONUtils;

import org.apache.http.client.ClientProtocolException;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.powermock.api.easymock.PowerMock;

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
