/*
 * Copyright (c) 2020 Payoneer Germany GmbH
 * https://www.payoneer.com
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package com.payoneer.mrs.exampleshop.checkout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.payoneer.mrs.exampleshop.R;
import com.payoneer.mrs.exampleshop.confirm.ConfirmActivity;
import com.payoneer.mrs.exampleshop.shared.BaseActivity;
import com.payoneer.mrs.exampleshop.summary.SummaryActivity;
import com.payoneer.mrs.payment.ui.PaymentUI;

/**
 * Activity displaying the checkout page, this page will open the Android SDK payment page.
 */
public final class CheckoutActivity extends BaseActivity implements CheckoutView {
    private CheckoutPresenter presenter;

    /**
     * Create an Intent to launch this checkout activity
     *
     * @param context the context
     * @param listUrl url of the current list
     * @return the newly created intent
     */
    public static Intent createStartIntent(Context context, String listUrl) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (TextUtils.isEmpty(listUrl)) {
            throw new IllegalArgumentException("listUrl may not be null or empty");
        }
        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putExtra(EXTRA_LISTURL, listUrl);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        initToolbar();

        Button button = findViewById(R.id.button_checkout);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onButtonClicked();
            }
        });
        presenter = new CheckoutPresenter(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        if (sdkResult != null) {
            presenter.handleSdkResult(sdkResult);
            sdkResult = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showPaymentSummary() {
        if (!active) {
            return;
        }
        Intent intent = SummaryActivity.createStartIntent(this, listUrl);
        startActivity(intent);
        setResultHandledIdleState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showPaymentConfirmation() {
        if (!active) {
            return;
        }
        Intent intent = ConfirmActivity.createStartIntent(this);
        startActivity(intent);
        setResultHandledIdleState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopPaymentWithErrorMessage() {
        if (!active) {
            return;
        }
        showErrorDialog(R.string.dialog_error_message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onErrorDialogClosed() {
        supportFinishAfterTransition();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.checkout_collapsed_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        CollapsingToolbarLayout layout = findViewById(R.id.collapsing_toolbar);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.roboto_medium);
        layout.setCollapsedTitleTypeface(typeface);
        layout.setExpandedTitleTypeface(typeface);
    }

    private void onButtonClicked() {
        PaymentUI paymentUI = PaymentUI.getInstance();
        paymentUI.setListUrl(listUrl);
        paymentUI.showPaymentPage(this, PAYMENT_REQUEST_CODE);
    }
}
