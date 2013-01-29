
package com.trovebox.android.test.net;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.trovebox.android.app.TroveboxApplication;
import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.net.TroveboxApi;
import com.trovebox.android.test.util.MockUtils;

import android.test.InstrumentationTestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ITroveboxApi.class)
public class MockTroveboxApiTest extends InstrumentationTestCase
{

    private ITroveboxApi mApiTested;
    private ITroveboxApi mApiMock;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mApiMock = MockUtils.mockTroveboxApi();
        mApiTested = TroveboxApi.createInstance(TroveboxApplication.getContext());
    }

    @Override
    protected void tearDown() throws Exception
    {
        MockUtils.unMockTroveboxApi();
        super.tearDown();
    }

    public void testMocking() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException,
            ClientProtocolException, IllegalStateException, IOException,
            JSONException
    {

        mApiMock.getPhotos();
        PowerMock.expectLastCall().andReturn(null).times(1);
        PowerMock.replayAll();
        mApiTested.getPhotos();
        PowerMock.verifyAll();

        PowerMock.reset(mApiMock);

        mApiMock.getPhotos();
        PowerMock.expectLastCall().andReturn(null).times(1);
        PowerMock.replayAll();
        mApiTested.getPhotos();
        PowerMock.verifyAll();
    }
}
