
package com.trovebox.android.common.net;

import java.util.ArrayList;

public class ReturnSizes {

    private final ArrayList<ReturnSizes> mSizes = new ArrayList<ReturnSizes>();

    int width;
    int height;
    boolean cropped;
    boolean blackWhite;

    public ReturnSizes(ReturnSizes returnSizes) {
        this(returnSizes.getWidth(), returnSizes.getHeight(), returnSizes.isCropped(), returnSizes
                .isBlackWhite());
        for (ReturnSizes rs : returnSizes.mSizes)
        {
            mSizes.add(new ReturnSizes(rs));
        }
    }
    public ReturnSizes(int width, int height) {
        this(width, height, false);
    }

    public ReturnSizes(int width, int height, boolean cropped) {
        this(width, height, cropped, false);
    }

    public ReturnSizes(int width, int height, boolean cropped, boolean blackWhite) {
        this.width = width;
        this.height = height;
        this.cropped = cropped;
        this.blackWhite = blackWhite;
    }

    public void add(int width, int height) {
        add(width, height, false, false);
    }

    public void add(int width, int height, boolean cropped) {
        add(width, height, cropped, false);
    }

    public void add(int width, int height, boolean cropped, boolean blackWhite) {

        mSizes.add(new ReturnSizes(width, height, cropped, blackWhite));
    }

    public void add(ReturnSizes returnSizes)
    {
        mSizes.add(returnSizes);
    }
    static String getString(ReturnSizes returnSizes)
    {
        StringBuilder sb = new StringBuilder(returnSizes.width + "x" + returnSizes.height);
        if (returnSizes.cropped) {
            sb.append("xCR");
        }
        if (returnSizes.blackWhite) {
            sb.append("xBW");
        }
        String glue = ",";
        sb.append(join(returnSizes.mSizes, glue));
        return sb.toString();
    }

    /*
     * Returns the String representation of this parameter seperated by ",". For
     * example: 200x200, 200x200xCR or 200x200xCRxBW
     */
    @Override
    public String toString() {
        return getString(this);
    }

    public int size() {
        return mSizes.size();
    }

    static String join(ArrayList<ReturnSizes> returnSizes, String glue)
    {
        StringBuilder out = new StringBuilder();
        for (ReturnSizes rs : returnSizes)
        {
            out.append(glue).append(rs.toString());
        }
        return out.toString();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isCropped() {
        return cropped;
    }

    public boolean isBlackWhite() {
        return blackWhite;
    }
}
