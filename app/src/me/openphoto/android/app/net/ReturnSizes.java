
package me.openphoto.android.app.net;

import java.util.ArrayList;

public class ReturnSizes {

    private final ArrayList<String> mSizes = new ArrayList<String>();

    public ReturnSizes(String size) {
        add(size);
    }

    public ReturnSizes(int width, int height) {
        this(width, height, false);
    }

    public ReturnSizes(int width, int height, boolean cropped) {
        this(width, height, cropped, false);
    }

    public ReturnSizes(int width, int height, boolean cropped, boolean blackWhite) {
        add(width, height, cropped, blackWhite);
    }

    public void add(int width, int height) {
        add(width, height, false, false);
    }

    public void add(int width, int height, boolean cropped) {
        add(width, height, cropped, false);
    }

    public void add(int width, int height, boolean cropped, boolean blackWhite) {
        StringBuilder sb = new StringBuilder(width + "x" + height);
        if (cropped) {
            sb.append("xCR");
        }
        if (blackWhite) {
            sb.append("xBW");
        }
        mSizes.add(sb.toString());
    }

    private void add(String size) {
        mSizes.add(size);
    }

    /*
     * Returns the String representation of this parameter seperated by ",". For
     * example: 200x200, 200x200xCR or 200x200xCRxBW
     */
    @Override
    public String toString() {
        return join(mSizes, ",");
    }

    public String get(int index) {
        return mSizes.get(index);
    }

    public int size() {
        return mSizes.size();
    }

    String join(ArrayList<String> strings, String glue)
    {
        int k = strings.size();
        if (k == 0)
            return null;
        StringBuilder out = new StringBuilder();
        out.append(strings.get(0));
        for (int x = 1; x < k; ++x)
            out.append(glue).append(strings.get(x));
        return out.toString();
    }
}
