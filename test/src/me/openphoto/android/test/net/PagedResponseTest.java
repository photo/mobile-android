
package me.openphoto.android.test.net;

import junit.framework.TestCase;
import me.openphoto.android.app.net.PagedResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class PagedResponseTest extends TestCase {
    public void testFromJson() throws JSONException {
        String jsonString = "{ 'totalRows':24, 'pageSize':'16', 'currentPage':1, 'totalPages':2 }";
        JSONObject json = new JSONObject(jsonString);
        PagedResponse response = new PagedResponse();
        response.setPaging(json);

        assertEquals(24, response.getTotalRows());
        assertEquals(16, response.getPageSize());
        assertEquals(1, response.getCurrentPage());
        assertEquals(2, response.getTotalPages());
    }
}
