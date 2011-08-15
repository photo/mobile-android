
package me.openphoto.android.test.model;

import junit.framework.TestCase;
import me.openphoto.android.app.model.Photo;

import org.json.JSONException;
import org.json.JSONObject;

public class PhotoTest extends TestCase {
    public void testFromJson() {
        String json = "{'tags':['sunnyvale','tavin'],'id':'4t','appId':'current.openphoto.me','pathBase':'\\/base\\/201108\\/1312348300-IMAG0015.jpg','dateUploadedMonth':'08','dateTakenMonth':'08','exifCameraMake':'HTC','dateTaken':'1312348300','title':'Tavin riding the horsey at Murphy Park in Sunnyvale','height':'3264','description':'Nice description','creativeCommons':'BY-NC','dateTakenYear':'2011','dateUploadedDay':'02','longitude':'-122.0302','host':'opmecurrent.s3.amazonaws.com','hash':'7e80413a981708f3e32d11916308c5f07fd7d93c','status':'1','width':'1952','dateTakenDay':'02','permission':'0','pathOriginal':'\\/original\\/201108\\/1312348300-IMAG0015.jpg','size':'1872','dateUploadedYear':'2011','views':'0','latitude':'37.3775','dateUploaded':'1312348302','exifCameraModel':'ADR6300','path200x200':'http:\\/\\/opmecurrent.s3.amazonaws.com\\/custom\\/201108\\/1312348300-IMAG0015_200x200.jpg','Name':'4t','path640x960':'http:\\/\\/opmecurrent.s3.amazonaws.com\\/custom\\/201108\\/1312348300-IMAG0015_640x960.jpg','path200x200xCR':'http:\\/\\/opmecurrent.s3.amazonaws.com\\/custom\\/201108\\/1312348300-IMAG0015_200x200xCR.jpg','path960x960':'http:\\/\\/opmecurrent.s3.amazonaws.com\\/custom\\/201108\\/1312348300-IMAG0015_960x960.jpg','path50x50xCR':'http:\\/\\/opmecurrent.s3.amazonaws.com\\/custom\\/201108\\/1312348300-IMAG0015_50x50xCR.jpg'}";
        Photo photo;
        try {
            photo = Photo.fromJson(new JSONObject(json));
        } catch (JSONException e) {
            throw new AssertionError("This exception should not be thrown!");
        }

        assertNotNull(photo);
        assertEquals("4t", photo.getId());
        assertNotNull(photo.getTags());
        assertEquals(2, photo.getTags().size());
        assertEquals("sunnyvale", photo.getTags().get(0));
        assertEquals("tavin", photo.getTags().get(1));
        assertEquals("Tavin riding the horsey at Murphy Park in Sunnyvale",
                photo.getTitle());
        assertEquals("Nice description", photo.getDescription());
        assertEquals("current.openphoto.me", photo.getAppId());
        assertEquals(
                "http://opmecurrent.s3.amazonaws.com/base/201108/1312348300-IMAG0015.jpg",
                photo.getUrl("base"));
        assertEquals(
                "http://opmecurrent.s3.amazonaws.com/original/201108/1312348300-IMAG0015.jpg",
                photo.getUrl("original"));
        assertEquals(
                "http://opmecurrent.s3.amazonaws.com/custom/201108/1312348300-IMAG0015_960x960.jpg",
                photo.getUrl("960x960"));
        assertEquals(
                "http://opmecurrent.s3.amazonaws.com/custom/201108/1312348300-IMAG0015_50x50xCR.jpg",
                photo.getUrl("50x50xCR"));
    }
}
