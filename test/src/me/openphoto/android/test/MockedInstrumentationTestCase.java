
package me.openphoto.android.test;

import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.test.util.MockUtils;
import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

public abstract class MockedInstrumentationTestCase<T extends Activity> extends
        ActivityInstrumentationTestCase2<T> {

    private IOpenPhotoApi mApiMock;

    public MockedInstrumentationTestCase(Class<T> theClass) {
        super("me.openphoto.android.app", theClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mApiMock = MockUtils.mockOpenPhotoApi();
    }

    @Override
    protected void tearDown() throws Exception {
        MockUtils.unMockOpenPhotoApi();
        super.tearDown();
    }

    protected IOpenPhotoApi getApiMock() {
        return mApiMock;
    }
}
