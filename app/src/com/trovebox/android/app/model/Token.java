
package com.trovebox.android.app.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class representing a Token on Trovebox.
 * 
 * @author Eugene Popovich
 */
public class Token implements Parcelable
{
    protected String mId;
    protected String mType;
    protected String mData;
    protected Date mDateExpires;

    private Token()
    {
    }

    /**
     * Creates a Token object from json.
     * 
     * @param json JSONObject of the Album as received from the Trovebox API
     * @return Album as represented in the given json
     * @throws JSONException
     */
    public static Token fromJson(JSONObject json) throws JSONException
    {
        Token token = new Token();
        token.mId = json.optString("id");
        token.mType = json.optString("type");
        token.mData = json.optString("data");
        Long dateExpires = Long.parseLong(json.optString("dateExpires"));
        token.mDateExpires = new Date(dateExpires.longValue() * 1000L);
        return token;
    }

    public String getId()
    {
        return mId;
    }

    public String getType() {
        return mType;
    }

    public String getData() {
        return mData;
    }

    public Date getDateExpires() {
        return mDateExpires;
    }

    /*****************************
     * PARCELABLE IMPLEMENTATION *
     *****************************/
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mId);
        out.writeString(mType);
        out.writeString(mData);
        out.writeLong(mDateExpires.getTime());
    }

    public static final Parcelable.Creator<Token> CREATOR = new Parcelable.Creator<Token>() {
        @Override
        public Token createFromParcel(Parcel in) {
            return new Token(in);
        }

        @Override
        public Token[] newArray(int size) {
            return new Token[size];
        }
    };

    private Token(Parcel in) {
        mId = in.readString();
        mType = in.readString();
        mData = in.readString();
        mDateExpires = new Date(in.readLong());
    }

}
