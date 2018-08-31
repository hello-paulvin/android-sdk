/**
 * Copyright(c) 2012-2018 optile GmbH. All Rights Reserved.
 * https://www.optile.net
 *
 * This software is the property of optile GmbH. Distribution  of  this
 * software without agreement in writing is strictly prohibited.
 *
 * This software may not be copied, used or distributed unless agreement
 * has been received in full.
 */

package net.optile.example.checkout;

import android.util.Log;

import com.btelligent.optile.pds.api.rest.model.payment.enterprise.extensible.List;

import java.net.URL;

import java.util.Map;
import java.util.concurrent.Callable;

import net.optile.payment.network.ListConnection;
import net.optile.payment.network.NetworkResponse;

import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * CheckoutPresenter responsible for communicating with the 
 * Payment SDK
 */
class CheckoutPresenter {

    public final static String TAG = "payment_CheckoutPresenter";

    private CheckoutView view;

    private Subscription subscription;

    /**
     * Construct a new CheckoutPresenter
     */
    CheckoutPresenter(CheckoutView view) {
        this.view = view;
    }

    /** 
     * Notify the presenter that it should be stopped.
     * Check if there are any pending subscriptions and unsubscribe if needed
     */
    public void onStop() {
        
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    /**
     * Is this presenter currently making a new list request
     *
     * @return true when active, false otherwise
     */
    boolean isCreateListSessionActive() {
        return subscription != null && !subscription.isUnsubscribed();
    }
    
    /** 
     * Create a new list session, normally mobile apps using the 
     * Android Payment SDK do not create new list sessions. 
     * Instead the merchant backend sends this request to 
     * the Payment API.
     *
     * @param url           The url to the Payment API end-point
     * @param authorization The authorization header for the list request
     * @param data          The data to be send in the list request
     */
    void createListSession(final String url, final String authorization, final String data) {

        if (isCreateListSessionActive()) {
            return;
        }

        Single<NetworkResponse> single = Single.fromCallable(new Callable<NetworkResponse>() {

                @Override
                public NetworkResponse call() throws CheckoutException {
                    return handleCreateListSession(url, authorization, data);
                }
            });
        
        this.subscription = single.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SingleSubscriber<NetworkResponse>() {

                    @Override
                    public void onSuccess(NetworkResponse response) {
                        onCreateListSessionSuccess(response);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.i(TAG, "onError: " + error);
                    }
                });
    }

    private void onCreateListSessionSuccess(NetworkResponse response) {
        Log.i(TAG, "onCreateListSessionSuccess: " + response);
    }
    
    private NetworkResponse handleCreateListSession(String url, String authorization, String data) throws CheckoutException {

        ListConnection conn = new ListConnection(url);
        NetworkResponse response = conn.createListSession(authorization, data);

        List list = response.getListSession();
        Map<String, URL> links = list.getLinks();
        URL selfURL = links.get("self");

        if (selfURL != null) {
            Log.i(TAG, "getListSession: " + selfURL);
            return conn.getListSession(selfURL);            
        } else {
            return response;
        }
    }
}
