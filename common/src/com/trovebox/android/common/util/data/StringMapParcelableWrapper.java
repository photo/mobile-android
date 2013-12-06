
package com.trovebox.android.common.util.data;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Simple class to wrap string map as a {@link Parcelable} object
 * 
 * @author Eugene Popovich
 */
public class StringMapParcelableWrapper implements Parcelable {
    Map<String, String> map;

    public StringMapParcelableWrapper() {
        map = new HashMap<String, String>();
    }

    public StringMapParcelableWrapper(Map<String, String> map) {
        this.map = map;
    }

    public Map<String, String> getMap()
    {
        return map;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        final int N = map.size();
        dest.writeInt(N);
        if (N > 0) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                dest.writeString(entry.getKey());
                String dat = entry.getValue();
                dest.writeString(dat);
                // etc...
            }
        }
    }

    public static final Creator<StringMapParcelableWrapper> CREATOR = new Creator<StringMapParcelableWrapper>() {
        @Override
        public StringMapParcelableWrapper createFromParcel(Parcel source) {
            return new StringMapParcelableWrapper(source);
        }

        @Override
        public StringMapParcelableWrapper[] newArray(int size) {
            return new StringMapParcelableWrapper[size];
        }
    };

    private StringMapParcelableWrapper(Parcel source) {
        this();
        final int N = source.readInt();
        for (int i = 0; i < N; i++) {
            String key = source.readString();
            String dat = source.readString();
            // etc...
            map.put(key, dat);
        }
    }
}
