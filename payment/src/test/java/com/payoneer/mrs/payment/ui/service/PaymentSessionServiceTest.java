/*
 * Copyright (c) 2020 Payoneer Germany GmbH
 * https://www.payoneer.com
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package com.payoneer.mrs.payment.ui.service;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.payoneer.mrs.payment.model.NetworkOperationType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PaymentSessionServiceTest {

    @Test
    public void isSupportedOperationType() {
        Context context = ApplicationProvider.getApplicationContext();
        PaymentSessionService service = new PaymentSessionService(context);
        assertTrue(service.isSupportedNetworkOperationType(NetworkOperationType.CHARGE));
        assertTrue(service.isSupportedNetworkOperationType(NetworkOperationType.PRESET));
        assertFalse(service.isSupportedNetworkOperationType(NetworkOperationType.UPDATE));
        assertFalse(service.isSupportedNetworkOperationType(NetworkOperationType.ACTIVATION));
        assertFalse(service.isSupportedNetworkOperationType(NetworkOperationType.PAYOUT));
        assertFalse(service.isSupportedNetworkOperationType(null));
    }
}
