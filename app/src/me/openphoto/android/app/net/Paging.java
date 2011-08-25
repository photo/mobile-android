
package me.openphoto.android.app.net;

public class Paging {
    private final int mPage;
    private final int mPageSize;

    public Paging(int page) {
        this(page, 0);
    }

    public Paging(int page, int pageSize) {
        mPage = pageSize;
        mPageSize = pageSize;
    }

    public boolean hasPage() {
        return mPage > 0;
    }

    public boolean hasPageSize() {
        return mPageSize > 0;
    }

    public int getPage() {
        return mPage;
    }

    public int getPageSize() {
        return mPageSize;
    }
}
