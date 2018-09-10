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

package net.optile.payment.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * The InteractionCode test
 */
public class InteractionCodeTest {

    @Test
    public void isInteractionCode_invalidValue_false() {
        assertFalse(InteractionCode.isInteractionCode("foo"));
    }

    @Test
    public void isInteractionCode_validValue_true() {
        assertTrue(InteractionCode.isValid(InteractionCode.PROCEED));
        assertTrue(InteractionCode.isValid(InteractionCode.ABORT));
        assertTrue(InteractionCode.isValid(InteractionCode.TRY_OTHER_NETWORK));
        assertTrue(InteractionCode.isValid(InteractionCode.TRY_OTHER_ACCOUNT));
        assertTrue(InteractionCode.isValid(InteractionCode.RETRY));
        assertTrue(InteractionCode.isValid(InteractionCode.RELOAD));
    }
}
