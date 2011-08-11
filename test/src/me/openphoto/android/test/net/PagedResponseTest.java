
package me.openphoto.android.test.net;

import junit.framework.TestCase;
import me.openphoto.android.app.net.PagedResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class PagedResponseTest extends TestCase {
    public void testFromJson() throws JSONException {
        String jsonString = "{ 'message':'', 'code':200, 'result': [ { 'totalRows':24, 'pageSize':'16', 'currentPage':1, 'totalPages':2 } ] }";
        JSONObject json = new JSONObject(jsonString);
        PagedResponse response = new PagedResponse(json);

        assertEquals(200, response.getCode());
        assertEquals(24, response.getTotalRows());
        assertEquals(16, response.getPageSize());
        assertEquals(1, response.getCurrentPage());
        assertEquals(2, response.getTotalPages());
    }
}
