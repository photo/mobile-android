
package me.openphoto.android.test.net;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.OpenPhotoApi;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import android.test.InstrumentationTestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IOpenPhotoApi.class)
public class MockOpenPhotoApiTest extends InstrumentationTestCase {

    private IOpenPhotoApi mApiTested;
    private IOpenPhotoApi mApiMock;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mApiMock = PowerMock.createMock(IOpenPhotoApi.class);
        Method injectMethod = OpenPhotoApi.class
                .getDeclaredMethod("injectMock", IOpenPhotoApi.class);
        injectMethod.setAccessible(true);
        injectMethod.invoke(null, mApiMock);

        mApiTested = OpenPhotoApi.createInstance(OpenPhotoApiConstants.OPENPHOTO_BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        Method injectMethod = OpenPhotoApi.class.getDeclaredMethod("injectMock",
                IOpenPhotoApi.class);
        injectMethod.setAccessible(true);
        injectMethod.invoke(null, new Object[] {
                null
        });
        super.tearDown();
    }

    public void testMocking() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            ClientProtocolException, IllegalStateException, IOException, JSONException {

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
