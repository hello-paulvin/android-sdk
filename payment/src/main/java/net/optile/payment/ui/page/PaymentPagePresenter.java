/*
 * Copyright (c) 2019 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.ui.page;

import java.net.URL;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import net.optile.payment.R;
import net.optile.payment.core.PaymentError;
import net.optile.payment.core.PaymentException;
import net.optile.payment.form.Operation;
import net.optile.payment.model.ErrorInfo;
import net.optile.payment.model.Interaction;
import net.optile.payment.model.InteractionCode;
import net.optile.payment.model.InteractionReason;
import net.optile.payment.model.ListResult;
import net.optile.payment.model.OperationResult;
import net.optile.payment.ui.PaymentResult;
import net.optile.payment.ui.PaymentUI;
import net.optile.payment.ui.dialog.MessageDialogFragment;
import net.optile.payment.ui.dialog.ThemedDialogFragment;
import net.optile.payment.ui.dialog.ThemedDialogFragment.ThemedDialogListener;
import net.optile.payment.ui.model.PaymentCard;
import net.optile.payment.ui.model.PaymentSession;
import net.optile.payment.ui.service.PaymentSessionListener;
import net.optile.payment.ui.service.PaymentSessionService;
import net.optile.payment.ui.widget.FormWidget;

/**
 * The PaymentPagePresenter implementing the presenter part of the MVP
 */
final class PaymentPagePresenter implements PaymentSessionListener {

    private final PaymentPageView view;
    private final PaymentSessionService service;

    private PaymentSession session;
    private String listUrl;
    private Interaction reloadInteraction;
    private Operation operation;
    
    /**
     * Create a new PaymentPagePresenter
     *
     * @param view The PaymentPageView displaying the payment list
     */
    PaymentPagePresenter(PaymentPageView view) {
        this.view = view;
        service = new PaymentSessionService();
        service.setListener(this);
    }

    /**
     * Load the PaymentSession from the Payment API. once loaded, populate the View with the newly loaded groups of payment methods.
     * If a previous session with the same listUrl is available then reuse the existing one.
     *
     * @param context context in which this presenter is running
     * @param chargePresetAccount if true charge the preset account after the payment session has been loaded
     */
    void onStart() {

        if (service.isActive()) {
            return;
        }
        this.listUrl = PaymentUI.getInstance().getListUrl();
        
        if (session != null && session.isListUrl(this.listUrl)) {
            view.showPaymentSession(session);
            return;
        }
        view.showProgress(true, ProgressView.LOAD);
        loadPaymentSession(this.listUrl);
    }
    
    /**
     * Notify this presenter that it should stop and cleanup its resources
     */
    void onStop() {
        service.stop();
    }

    /**
     * Let the Presenter handle the back press, i.e. if the presenter is currently performing an operation, the presenter may disable the back button press.
     *
     * @return true when this presenter handles the back press, false otherwise
     */
    boolean onBackPressed() {
        if (service.isPostingOperation()) {
            view.showSnackbar(view.getStringRes(R.string.pmsnackbar_operation_interrupted));
            return true;
        }
        return false;
    }


    /**
     * Notify this presenter that the user has clicked the action button in the PaymentCard.
     * The presenter will validate if the operation is supported and then post it to the Payment API.
     *
     * @param card the PaymentCard containing the operation URL
     * @param widgets containing the user input data
     */
    void onActionClicked(PaymentCard card, Map<String, FormWidget> widgets) {

        if (service.isActive()) {
            return;
        }
        if (session.getPresetCard() == card) {
            PaymentResult result = new PaymentResult("Same presetAccount selected");
            closeSessionWithOkCode(result);
            return;
        }
        switch (session.getOperationType()) {
            case Operation.CHARGE:
            case Operation.PRESET:
                postOperation(card, widgets);
                break;
            default:
                Log.w("pay_Presenter", "OperationType not supported");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPaymentSessionSuccess(PaymentSession session) {
        ListResult listResult = session.getListResult();
        Interaction interaction = listResult.getInteraction();

        switch (interaction.getCode()) {
            case InteractionCode.PROCEED:
                handleLoadInteractionProceed(session);
                break;
            default:
                PaymentResult result = new PaymentResult(listResult.getResultInfo(), interaction);
                closeSessionWithCanceledCode(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPaymentSessionError(Throwable cause) {
        if (cause instanceof PaymentException) {
            handleLoadPaymentError((PaymentException) cause);
            return;
        }
        closeSessionWithErrorCode(R.string.pmdialog_error_unknown, cause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOperationSuccess(OperationResult operation) {
        PaymentResult result = new PaymentResult(operation);

        switch (operation.getInteraction().getCode()) {
            case InteractionCode.PROCEED:
                closeSessionWithOkCode(result);
                break;
            default:
                handleOperationInteractionError(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOperationError(Throwable cause) {

        if (cause instanceof PaymentException) {
            handleOperationPaymentError((PaymentException) cause);
            return;
        }
        closeSessionWithErrorCode(R.string.pmdialog_error_unknown, cause);
    }

    private void handleLoadInteractionProceed(PaymentSession session) {
        this.session = session;

        if (reloadInteraction != null) {
            showInteractionMessage(reloadInteraction);
            reloadInteraction = null;
        }
        view.showPaymentSession(session);
    }

    private void handleLoadPaymentError(PaymentException cause) {
        PaymentError error = cause.error;
        ErrorInfo info = error.errorInfo;

        if (info != null) {
            closeSessionWithCanceledCode(new PaymentResult(info.getResultInfo(), info.getInteraction()));
        } else if (error.isError(PaymentError.CONN_ERROR)) {
            handleLoadConnError(cause);
        } else {
            closeSessionWithErrorCode(R.string.pmdialog_error_unknown, cause);
        }
    }

    private void postOperation(PaymentCard card, Map<String, FormWidget> widgets) {
        URL url = card.getOperationLink();
        Operation operation = new Operation(url);

        try {
            boolean error = false;
            for (FormWidget widget : widgets.values()) {

                if (widget.validate()) {
                    widget.putValue(operation);
                } else {
                    error = true;
                }
                widget.clearFocus();
            }
            if (!error) {
                postOperation(operation);
            }
        } catch (PaymentException e) {
            closeSessionWithErrorCode(R.string.pmdialog_error_unknown, e);
        }
    }

    private void reloadPaymentSession(PaymentResult result) {
        this.reloadInteraction = result.getInteraction();
        loadPaymentSession(this.listUrl);
    }

    private void handleOperationPaymentError(PaymentException cause) {
        PaymentError error = cause.error;
        ErrorInfo info = error.errorInfo;

        if (info != null) {
            handleOperationInteractionError(new PaymentResult(info.getResultInfo(), info.getInteraction()));
        } else if (error.isError(PaymentError.CONN_ERROR)) {
            handleOperationConnError();
        } else {
            closeSessionWithErrorCode(R.string.pmdialog_error_unknown, cause);
        }
    }

    private void handleOperationInteractionError(PaymentResult result) {
        Interaction interaction = result.getInteraction();

        switch (interaction.getCode()) {
            case InteractionCode.RELOAD:
            case InteractionCode.TRY_OTHER_NETWORK:
                reloadPaymentSession(result);
                break;
            case InteractionCode.RETRY:
                handleOperationInteractionRetry(result);
                break;
            case InteractionCode.TRY_OTHER_ACCOUNT:
                continueSessionWithWarning(result);
                break;
            case InteractionCode.ABORT:
                handleOperationInteractionAbort(result);
                break;
            default:
                closeSessionWithCanceledCode(result);
        }
    }

    private void handleOperationInteractionAbort(PaymentResult result) {
        Interaction interaction = result.getInteraction();

        switch (interaction.getReason()) {
            case InteractionReason.DUPLICATE_OPERATION:
                closeSessionWithOkCode(result);
                break;
            default:
                closeSessionWithCanceledCode(result);
        }
    }

    private void handleOperationInteractionRetry(PaymentResult result) {
        Interaction interaction = result.getInteraction();

        switch (interaction.getReason()) {
            case InteractionReason.EXPIRED_SESSION:
                closeSessionWithCanceledCode(result);
                break;
            default:
                continueSessionWithWarning(result);
        }
    }

    private void showInteractionMessage(Interaction interaction) {
        String msg = translateInteraction(interaction, null);

        if (!TextUtils.isEmpty(msg)) {
            showMessage(msg);
        }
    }

    private void continueSessionWithWarning(PaymentResult result) {
        view.showPaymentSession(this.session);
        showInteractionMessage(result.getInteraction());
    }

    private void closeSessionWithOkCode(PaymentResult result) {
        view.setPaymentResult(PaymentUI.RESULT_CODE_OK, result);
        view.closePage();
    }

    private void closeSessionWithCanceledCode(PaymentResult result) {
        String msg = translateInteraction(result.getInteraction(), view.getStringRes(R.string.pmdialog_error_unknown));
        view.setPaymentResult(PaymentUI.RESULT_CODE_CANCELED, result);
        closePageWithMessage(msg);
    }

    private void closeSessionWithErrorCode(int msgResId, Throwable cause) {
        PaymentResult result;

        if (cause instanceof PaymentException) {
            PaymentException pe = (PaymentException) cause;
            result = new PaymentResult(pe.getMessage(), pe.error);
        } else {
            String resultInfo = cause.toString();
            PaymentError error = new PaymentError("PaymentPage", PaymentError.INTERNAL_ERROR, resultInfo);
            result = new PaymentResult(resultInfo, error);
        }
        view.setPaymentResult(PaymentUI.RESULT_CODE_ERROR, result);
        closePageWithMessage(view.getStringRes(msgResId));
    }

    private void loadPaymentSession(final String listUrl) {
        this.session = null;
        view.clear();
        service.loadPaymentSession(listUrl, view.getContext());
    }

    private void postOperation(final Operation operation) {
        this.operation = operation;
        int progressType = operation.isType(Operation.PRESET) ? ProgressView.LOAD : ProgressView.SEND;
        view.showProgress(true, progressType);
        service.postOperation(operation);
    }

    private void handleLoadConnError(final PaymentException pe) {
        MessageDialogFragment dialog = createMessageDialog(view.getStringRes(R.string.pmdialog_error_connection), true);
        PaymentResult result = new PaymentResult(pe.getMessage(), pe.error);
        view.setPaymentResult(PaymentUI.RESULT_CODE_CANCELED, result);

        dialog.setListener(new ThemedDialogListener() {
            @Override
            public void onButtonClicked(ThemedDialogFragment dialog, int which) {
                switch (which) {
                    case ThemedDialogFragment.BUTTON_NEUTRAL:
                        view.closePage();
                        break;
                    case ThemedDialogFragment.BUTTON_POSITIVE:
                        loadPaymentSession(listUrl);
                }
            }

            @Override
            public void onDismissed(ThemedDialogFragment dialog) {
                view.closePage();
            }
        });
        view.showDialog(dialog);
    }

    private void handleOperationConnError() {
        view.showPaymentSession(this.session);
        MessageDialogFragment dialog = createMessageDialog(view.getStringRes(R.string.pmdialog_error_connection), true);

        dialog.setListener(new ThemedDialogListener() {
            @Override
            public void onButtonClicked(ThemedDialogFragment dialog, int which) {
                if (which == ThemedDialogFragment.BUTTON_POSITIVE) {
                    postOperation(operation);
                }
            }

            @Override
            public void onDismissed(ThemedDialogFragment dialog) {
            }
        });
        view.showDialog(dialog);
    }

    private String translateInteraction(Interaction interaction, String defMessage) {

        if (session == null || interaction == null) {
            return defMessage;
        }
        String msg = session.getLang().translateInteraction(interaction);
        return TextUtils.isEmpty(msg) ? defMessage : msg;
    }

    private void showMessage(String message) {
        view.showDialog(createMessageDialog(message, false));
    }

    private void closePageWithMessage(String message) {
        MessageDialogFragment dialog = createMessageDialog(message, false);
        dialog.setListener(new ThemedDialogListener() {
            @Override
            public void onButtonClicked(ThemedDialogFragment dialog, int which) {
                view.closePage();
            }

            @Override
            public void onDismissed(ThemedDialogFragment dialog) {
                view.closePage();
            }
        });
        view.showDialog(dialog);
    }

    private MessageDialogFragment createMessageDialog(String message, boolean hasRetry) {
        MessageDialogFragment dialog = new MessageDialogFragment();
        dialog.setMessage(message);
        dialog.setNeutralButton(view.getStringRes(R.string.pmdialog_cancel_button));

        if (hasRetry) {
            dialog.setPositiveButton(view.getStringRes(R.string.pmdialog_retry_button));
        }
        return dialog;
    }
}
