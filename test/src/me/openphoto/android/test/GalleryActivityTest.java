
package me.openphoto.android.test;

import java.io.IOException;

import me.openphoto.android.app.GalleryActivity;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.ReturnSize;
import me.openphoto.android.test.net.JSONUtils;

import org.apache.http.client.ClientProtocolException;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.powermock.api.easymock.PowerMock;

import com.jayway.android.robotium.solo.Solo;

public class GalleryActivityTest extends MockedInstrumentationTestCase<GalleryActivity> {

    public GalleryActivityTest() {
        super(GalleryActivity.class);
    }

    public void testLoadsImages() throws ClientProtocolException, IllegalStateException,
            IOException, JSONException {
        // Setup mock calls and their responses
        PowerMock.reset(getApiMock());
        getApiMock().getPhotos((ReturnSize) EasyMock.anyObject(), (Paging) EasyMock.anyObject());
        PowerMock
                .expectLastCall()
                .andReturn(
                        new PhotosResponse(JSONUtils.getJson(getInstrumentation().getContext(),
                                R.raw.json_photos_get))).times(1);
        PowerMock.replayAll();

        // Actual test
        Solo solo = new Solo(getInstrumentation(), getActivity());
        assertEquals(2, solo.getCurrentGridViews().get(0).getCount());

        // check if the mock calls were called correctly
        PowerMock.verifyAll();
    }
}
