package com.trovebox.android.app.net.account;

import android.content.Context;

/**
 * The factory to create IAccountTroveboxApi instance
 * 
 * @author Eugene Popovich
 */
public class IAccountTroveboxApiFactory {
    /**
     * Construct an instance of IAccountTroveboxApi implementation
     * 
     * @param context
     * @return
     */
    public static IAccountTroveboxApi getApi(Context context)
    {
        return new AccountTroveboxApi(context);
    }
}
