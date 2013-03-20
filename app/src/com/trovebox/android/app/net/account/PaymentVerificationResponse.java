package com.trovebox.android.app.net.account;

import org.json.JSONException;
import org.json.JSONObject;

import com.trovebox.android.app.net.TroveboxResponse;

/**
 * The class to represent API payment verification json response
 * 
 * @author Eugene Popovich
 */
public class PaymentVerificationResponse extends TroveboxResponse {
    public PaymentVerificationResponse(JSONObject json) throws JSONException
    {
        super(RequestType.PAYMENT_VERIFICATION, json);
    }
}
