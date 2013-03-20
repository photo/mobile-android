package com.trovebox.android.test.purchase.util;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.trovebox.android.app.R;
import com.trovebox.android.app.purchase.PurchaseController;
import com.trovebox.android.app.purchase.util.StringXORer;

public class TestStringXORer extends InstrumentationTestCase {
    private static final String TAG = TestStringXORer.class.getSimpleName();
    public void testEncodeDecode()
    {
        String appKey = getInstrumentation().getTargetContext()
                .getString(R.string.application_public_key);
        String cryptKey = PurchaseController.cryptKey;
        Log.i(TAG, "App key: " + appKey);

        String encoded = StringXORer.encode(appKey, cryptKey);
        Log.i(TAG, "Encoded: " + encoded);
        assertNotNull(encoded);
        assertTrue(!appKey.equals(encoded));

        String decoded = StringXORer.decode(encoded, cryptKey);
        Log.i(TAG, "Decoded: " + decoded);
        assertNotNull(decoded);
        assertEquals(appKey, decoded);
    }
}
