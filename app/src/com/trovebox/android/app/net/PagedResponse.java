
package com.trovebox.android.app.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is the base for a paged response.
 * 
 * @author Patrick Boos
 */
public class PagedResponse extends TroveboxResponse {
    private int mTotalRows;
    private int mPageSize;
    private int mCurrentPage;
    private int mTotalPages;

    public PagedResponse(RequestType requestType, JSONObject json) throws JSONException {
        super(requestType, json);
        if (json.get("result") instanceof JSONArray) {
            JSONArray array = json.getJSONArray("result");
            if (array.length() > 0)
            {
                JSONObject objectContainingPaging = array.getJSONObject(0);
                mTotalRows = objectContainingPaging.optInt("totalRows");
                mPageSize = objectContainingPaging.optInt("pageSize");
                mCurrentPage = objectContainingPaging.optInt("currentPage");
                mTotalPages = objectContainingPaging.optInt("totalPages");
            }
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

    /**
     * Whether the current page is less than total pages
     * 
     * @return
     */
    public boolean hasNextPage()
    {
        return getCurrentPage() < getTotalPages();
    }
}
