
package com.trovebox.android.app.net.account;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;

import com.trovebox.android.app.purchase.util.Purchase;

/**
 * @author Patrick Santana <patrick@trovebox.com>
 */
public interface IAccountTroveboxApi {

    /**
     * Create a user
     * 
     * @param user user
     * @param email email
     * @param password password
     * @return response with all details on it (credentials)
     */
    public AccountTroveboxResponse createNewUser(String user, String email,
            String password) throws IllegalStateException, IOException, NoSuchAlgorithmException,
            JSONException;

    /**
     * Sign in the user in the application
     * 
     * @param email email
     * @param password pwd
     * @return response with all details on it (credentials)
     */
    public AccountTroveboxResponse signIn(String email, String password)
            throws IllegalStateException, IOException, NoSuchAlgorithmException, JSONException;

    /**
     * Recover password for user based on email
     * 
     * @param email user's email
     * @return a message that we need to show to the user. this message could
     *         be, eg : password reset, please check your email or this email is
     *         not in our system
     */
    public String recoverPassword(String email);

    /**
     * Verify payment information
     * 
     * @param email user's email
     * @param purchase the in-app billing purchase information
     * @return
     * @throws IllegalStateException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws JSONException
     */
    public PaymentVerificationResponse verifyPayment(String email, Purchase purchase)
            throws IllegalStateException, IOException,
            NoSuchAlgorithmException,
            JSONException;
}
