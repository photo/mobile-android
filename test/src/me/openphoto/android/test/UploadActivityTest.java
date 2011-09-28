
package me.openphoto.android.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.openphoto.android.app.UploadActivity;
import me.openphoto.android.app.net.UploadMetaData;
import me.openphoto.android.app.net.UploadResponse;
import me.openphoto.android.test.net.JSONUtils;
import me.openphoto.android.test.util.FileUtils;

import org.easymock.EasyMock;
import org.json.JSONException;
import org.powermock.api.easymock.PowerMock;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.jayway.android.robotium.solo.Solo;

public class UploadActivityTest extends MockedInstrumentationTestCase<UploadActivity> {

    public UploadActivityTest() {
        super(UploadActivity.class);
    }

    public void testNormalStartShouldShowDialog() throws IOException {
        Solo solo = new Solo(getInstrumentation(), getActivity());
        solo.sleep(100); // Wait that dialog really is open
        // This will check if the dialog is there
        // Dialog has Title (Upload) and two items (Camera + Gallery)
        assertEquals("Upload", solo.getText(0).getText().toString());
        assertEquals("Camera", solo.getText(1).getText().toString());
        assertEquals("Gallery", solo.getText(2).getText().toString());

        assertFalse("Because of dialog, Upload button should not be found!",
                solo.searchButton("Upload!"));
    }

    public void testShareIntent() throws IOException, IllegalStateException, JSONException,
            InterruptedException {
        InputStream imageStream = getInstrumentation().getContext().getResources()
                .openRawResource(R.raw.android);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/data/me.openphoto.android");
        if (!dir.exists()) {
            assertTrue(dir.mkdirs());
        }
        File file = new File(dir, "test-android.jpg");
        FileUtils.writeToFile(imageStream, file);
        String uri = MediaStore.Images.Media.insertImage(getInstrumentation().getTargetContext()
                .getContentResolver(), file.getAbsolutePath(), "Test",
                "Image used for testing OpenPhoto Andorid application");

        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));
        setActivityIntent(i);

        Solo solo = new Solo(getInstrumentation(), getActivity());

        assertTrue("Dialog should not be there overlapping Upload button!",
                solo.searchButton("Upload!"));

        // Test Upload button press
        PowerMock.reset(getApiMock());
        getApiMock().uploadPhoto((File) EasyMock.notNull(), (UploadMetaData) EasyMock.notNull());
        PowerMock
                .expectLastCall()
                .andReturn(
                        new UploadResponse(JSONUtils.getJson(getInstrumentation().getContext(),
                                R.raw.json_photo_upload))).times(1);
        PowerMock.replayAll();
        solo.clickOnButton("Upload!");
        solo.sleep(500);
        solo.waitForDialogToClose(1000);
        PowerMock.verifyAll();
    }
}
