
package me.openphoto.android.app.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is the base for a paged response.
 * 
 * @author Patrick Boos
 */
public class PagedResponse extends OpenPhotoResponse {
    private int mTotalRows;
    private int mPageSize;
    private int mCurrentPage;
    private int mTotalPages;

    public PagedResponse(JSONObject json) throws JSONException {
        super(json);
        if (json.get("result") instanceof JSONArray) {
            JSONObject objectContainingPaging = json.getJSONArray("result").getJSONObject(0);
            mTotalRows = objectContainingPaging.optInt("totalRows");
            mPageSize = objectContainingPaging.optInt("pageSize");
            mCurrentPage = objectContainingPaging.optInt("currentPage");
            mTotalPages = objectContainingPaging.optInt("totalPages");
        }
    }

    /**
     * Returns the total number of rows/items in this response.
     * 
     * @return number of rows/items
     */
    public int getTotalRows() {
        return mTotalRows;
    }

    /**
     * @return size of a page
     */
    public int getPageSize() {
        return mPageSize;
    }

    /**
     * Returns the current page number. Page numbers start at 1.
     * 
     * @return current page
     */
    public int getCurrentPage() {
        return mCurrentPage;
    }

    /**
     * @return total pages
     */
    public int getTotalPages() {
        return mTotalPages;
    }
}
