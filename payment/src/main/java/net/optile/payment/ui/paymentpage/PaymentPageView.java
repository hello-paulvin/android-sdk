/*
 * Copyright(c) 2012-2018 optile GmbH. All Rights Reserved.
 * https://www.optile.net
 *
 * This software is the property of optile GmbH. Distribution  of  this
 * software without agreement in writing is strictly prohibited.
 *
 * This software may not be copied, used or distributed unless agreement
 * has been received in full.
 */

package net.optile.payment.ui.paymentpage;

import android.content.Context;
import java.util.List;

/**
 * The PaymentPageView interface is the View part of the MVP, this is implemented by the PaymentPageActivity
 */
public interface PaymentPageView {

    /** 
     * Is the view currently active
     * 
     * @return true when active, false otherwise 
     */
    boolean isActive();

    /** 
     * Get the Context from this view
     * 
     * @return context 
     */
    Context getContext();

    /** 
     * Set the list of PaymentListItems in the adapter 
     * 
     * @param items the items to be set 
     */
    void setItems(List<PaymentListItem> items);

    /** 
     * Abort the payment and notify the user of this SDK
     * 
     * @param code   the code indicating what went wrong
     * @param reason reason the reason why the payment has been aborted
     */
    void abortPayment(String code, String reason);
}
