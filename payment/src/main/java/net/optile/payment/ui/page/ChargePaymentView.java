/*
 * Copyright (c) 2019 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.ui.page;

import android.app.Activity;
import net.optile.payment.ui.PaymentResult;
import net.optile.payment.ui.dialog.ThemedDialogFragment.ThemedDialogListener;

/**
 * The ChargePaymentView interface is the View part of the MVP, this is implemented by the ChargePaymentActivity
 */
interface ChargePaymentView {

    /**
     * Show the progress animation.
     */
    void showProgress();

    /**
     * Show a generic message to the user, notify the listener of events in this dialog.
     *
     * @param message the message to show in the dialog
     * @param listener to be notified of dialog events
     */
    void showMessageDialog(String message, ThemedDialogListener listener);

    /**
     * Show the connection error dialog to the user, notify the listener of events in this dialog.
     *
     * @param listener to be notified of dialog events
     */
    void showConnectionDialog(ThemedDialogListener listener);

    /**
     * Show a warning message to the user
     *
     * @param message The message to be shown
     */
    void showWarningMessage(String message);

    /**
     * Close this view and return to the parent who launched it
     */
    void close();

    /**
     * Set the current activity payment result, this is either PaymentUI.RESULT_CODE_OK,
     * PaymentUI.RESULT_CODE_CANCELED, PaymentUI.RESULT_CODE_ERROR
     *
     * @param resultCode the current resultCode
     * @param result containing the Payment result state
     */
    void setPaymentResult(int resultCode, PaymentResult result);

    /**
     * Get the Android Context of this view.
     *
     * @return the Context of this view
     */
    Activity getActivity();
}
