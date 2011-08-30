
package me.openphoto.android.test.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.OpenPhotoApi;

import org.powermock.api.easymock.PowerMock;

public class MockUtils {
    /**
     * This will set up the OpenPhotoApi to be mocked. All request will not go
     * through the server, but through the mock object, which will immediately
     * return results. Make sure to test on the mock object and set what results
     * should be returned on certain calls on the mock object.<br />
     * <br />
     * Make sure that you remove the mock (unMock...) when done testing. Other
     * tests might not want the mock to be in place!
     * 
     * @return The IOpenPhotoApi object which is the mock used instead of the
     *         normal OpenPhotoApi.
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static IOpenPhotoApi mockOpenPhotoApi() throws SecurityException,
            NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        IOpenPhotoApi mApiMock = PowerMock.createMock(IOpenPhotoApi.class);
        Method injectMethod = OpenPhotoApi.class
                .getDeclaredMethod("injectMock", IOpenPhotoApi.class);
        injectMethod.setAccessible(true);
        injectMethod.invoke(null, mApiMock);
        return mApiMock;
    }

    /**
     * This will remove the mock. After this call, all calls to OpenPhotoApi
     * will go through the server.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void unMockOpenPhotoApi() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method injectMethod = OpenPhotoApi.class.getDeclaredMethod("injectMock",
                IOpenPhotoApi.class);
        injectMethod.setAccessible(true);
        injectMethod.invoke(null, new Object[] {
                null
        });
    }
}
