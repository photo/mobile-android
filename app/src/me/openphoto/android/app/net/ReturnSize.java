
package me.openphoto.android.app.net;

public class ReturnSize {

    private final int mWidth;
    private final int mHeight;
    private final boolean mCropped;
    private final boolean mBlackWhite;

    public ReturnSize(int width, int height) {
        this(width, height, false);
    }

    public ReturnSize(int width, int height, boolean cropped) {
        this(width, height, cropped, false);
    }

    public ReturnSize(int width, int height, boolean cropped, boolean blackWhite) {
        mWidth = width;
        mHeight = height;
        mCropped = cropped;
        mBlackWhite = blackWhite;
    }

    /*
     * Returns the String representation of this parameter. For example:
     * 200x200, 200x200xCR or 200x200xCRxBW
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mWidth + "x" + mHeight);
        if (mCropped) {
            sb.append("xCR");
        }
        if (mBlackWhite) {
            sb.append("xBW");
        }
        return sb.toString();
    }
}
