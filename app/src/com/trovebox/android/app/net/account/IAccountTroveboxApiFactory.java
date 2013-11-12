package com.trovebox.android.app.net.account;

import android.content.Context;

import com.trovebox.android.app.TroveboxApplication;

/**
 * The factory to create IAccountTroveboxApi instance
 * 
 * @author Eugene Popovich
 */
public class IAccountTroveboxApiFactory {
    /**
     * Construct an instance of IAccountTroveboxApi implementation
     * 
     * @return
     */
    public static IAccountTroveboxApi getApi()
    {
        return getApi(TroveboxApplication.getContext());
    }

    /**
     * Construct an instance of IAccountTroveboxApi implementation
     * 
     * @param context
     * @return
     */
    public static IAccountTroveboxApi getApi(Context context)
    {
        return new FakeAccountTroveboxApi(context);
    }
}
