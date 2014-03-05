
package com.trovebox.android.test.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.powermock.api.easymock.PowerMock;

import com.trovebox.android.common.net.ITroveboxApi;
import com.trovebox.android.common.net.TroveboxApi;

public class MockUtils {
    /**
     * This will set up the TroveboxApi to be mocked. All request will not go
     * through the server, but through the mock object, which will immediately
     * return results. Make sure to test on the mock object and set what results
     * should be returned on certain calls on the mock object.<br />
     * <br />
     * Make sure that you remove the mock (unMock...) when done testing. Other
     * tests might not want the mock to be in place!
     * 
     * @return The ITroveboxApi object which is the mock used instead of the
     *         normal TroveboxApi.
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static ITroveboxApi mockTroveboxApi() throws SecurityException,
            NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        ITroveboxApi mApiMock = PowerMock.createMock(ITroveboxApi.class);
        Method injectMethod = TroveboxApi.class
                .getDeclaredMethod("injectMock", ITroveboxApi.class);
        injectMethod.setAccessible(true);
        injectMethod.invoke(null, mApiMock);
        return mApiMock;
    }

    /**
     * This will remove the mock. After this call, all calls to TroveboxApi
     * will go through the server.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void unMockTroveboxApi() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method injectMethod = TroveboxApi.class.getDeclaredMethod("injectMock",
                ITroveboxApi.class);
        injectMethod.setAccessible(true);
        injectMethod.invoke(null, new Object[] {
                null
        });
    }
}
