
package com.trovebox.android.test;

import com.trovebox.android.app.net.ITroveboxApi;
import com.trovebox.android.app.util.CommonUtils;
import com.trovebox.android.test.util.MockUtils;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

public abstract class MockedInstrumentationTestCase<T extends Activity> extends
        ActivityInstrumentationTestCase2<T> {

    private ITroveboxApi mApiMock;

    public MockedInstrumentationTestCase(Class<T> theClass) {
        super("com.trovebox.android.app", theClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mApiMock = MockUtils.mockTroveboxApi();
        CommonUtils.TEST_CASE = true;
    }

    @Override
    protected void tearDown() throws Exception {
        MockUtils.unMockTroveboxApi();
        super.tearDown();
    }

    protected ITroveboxApi getApiMock() {
        return mApiMock;
    }
}
