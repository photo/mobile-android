
package com.trovebox.android.test;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.easymock.EasyMock;
import org.holoeverywhere.widget.ListAdapterWrapper;
import org.json.JSONException;
import org.junit.Assert;
import org.powermock.api.easymock.PowerMock;

import android.widget.ListAdapter;
import android.widget.ListView;

import com.jayway.android.robotium.solo.Solo;
import com.trovebox.android.app.MainActivity;
import com.trovebox.android.common.fragment.gallery.GalleryFragment.GalleryAdapterExt;
import com.trovebox.android.common.net.Paging;
import com.trovebox.android.common.net.PhotoResponse;
import com.trovebox.android.common.net.PhotosResponse;
import com.trovebox.android.common.net.ReturnSizes;
import com.trovebox.android.common.net.TroveboxResponse.RequestType;
import com.trovebox.android.common.util.CommonUtils;
import com.trovebox.android.test.net.JSONUtils;

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
                null,
                (String) EasyMock.anyObject(),
                (Paging) EasyMock.anyObject(),
                null);
        PowerMock
                .expectLastCall()
                .andReturn(
                        new PhotosResponse(JSONUtils.getJson(
                                getInstrumentation().getContext(),
                                R.raw.json_photos_get))).times(2);
        getApiMock().getPhoto(
                (String) EasyMock.anyObject(),
                (ReturnSizes) EasyMock.anyObject(),
                (String) EasyMock.anyObject(), (String) EasyMock.anyObject()
                );
        PowerMock
                .expectLastCall()
                .andReturn(
                        new PhotoResponse(RequestType.UNKNOWN, JSONUtils.getJson(
                                getInstrumentation().getContext(),
                                R.raw.json_photo_get)))
                .anyTimes();
        PowerMock.replayAll();
        getActivity().runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
                getActivity().selectTab(0);
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
        ListAdapter adapter = listView.getAdapter();
        Assert.assertTrue(adapter instanceof ListAdapterWrapper);
        adapter = ((ListAdapterWrapper) adapter).getWrappedAdapter();
        Assert.assertTrue(adapter instanceof GalleryAdapterExt);
        GalleryAdapterExt gadapter = (GalleryAdapterExt) adapter;
        CommonUtils.debug(GalleryActivityTest.class.getSimpleName(),
                "start compare");
        assertEquals(2, gadapter.getSuperCount());

        // check if the mock calls were called correctly
        PowerMock.verifyAll();
    }
}
