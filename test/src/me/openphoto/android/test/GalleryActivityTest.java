
package me.openphoto.android.test;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import me.openphoto.android.app.GalleryFragment.GalleryAdapterExt;
import me.openphoto.android.app.MainActivity;
import me.openphoto.android.app.net.Paging;
import me.openphoto.android.app.net.PhotosResponse;
import me.openphoto.android.app.net.ReturnSizes;
import me.openphoto.android.app.util.CommonUtils;
import me.openphoto.android.test.net.JSONUtils;

import org.apache.http.client.ClientProtocolException;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.junit.Assert;
import org.powermock.api.easymock.PowerMock;

import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.jayway.android.robotium.solo.Solo;

public class GalleryActivityTest extends
        MockedInstrumentationTestCase<MainActivity>
{

    public GalleryActivityTest()
    {
        super(MainActivity.class);
    }

    public void testLoadsImages() throws ClientProtocolException,
            IllegalStateException,
            IOException, JSONException, InterruptedException
    {
        // Setup mock calls and their responses
        PowerMock.reset(getApiMock());

        getApiMock().getPhotos((ReturnSizes) EasyMock.anyObject(),
                (Collection<String>) EasyMock.anyObject(),
                (String) EasyMock.anyObject(),
                (String) EasyMock.anyObject(),
                (Paging) EasyMock.anyObject());
        PowerMock
                .expectLastCall()
                .andReturn(
                        new PhotosResponse(JSONUtils.getJson(
                                getInstrumentation().getContext(),
                                R.raw.json_photos_get))).times(2);
        PowerMock.replayAll();
        getActivity().runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
                ActionBar actionBar = getActivity().getSupportActionBar();
                actionBar.selectTab(actionBar
                        .getTabAt(MainActivity.GALLERY_INDEX));
            }
        });
        getInstrumentation().waitForIdleSync();
        CountDownLatch signal = new CountDownLatch(1);
        signal.await(5, TimeUnit.SECONDS);

        // Actual test
        Solo solo = new Solo(getInstrumentation(), getActivity());
        Assert.assertTrue(solo.getCurrentListViews().size() == 1);
        ListView listView = solo.getCurrentListViews().get(0);
        Assert.assertNotNull(listView.getAdapter());
        Assert.assertTrue(listView.getAdapter() instanceof GalleryAdapterExt);
        GalleryAdapterExt adapter = (GalleryAdapterExt) listView.getAdapter();
        CommonUtils.debug(GalleryActivityTest.class.getSimpleName(),
                "start compare");
        assertEquals(2, adapter.getSuperCount());

        // check if the mock calls were called correctly
        PowerMock.verifyAll();
    }
}
